from __future__ import annotations

import asyncio
import json
from datetime import datetime, timezone

import httpx

from conftest import CLAIM, FakeClock, fixture_bytes, run
from hoax_verifier.evidence import (EvidenceProvider, EvidenceRetriever, FactCheckProvider, FeedConfig,
                                    GDELTProvider, RSSProvider)
from hoax_verifier.evidence.base import Found, ProviderDisabled, ProviderError
from hoax_verifier.evidence.pipeline import deduplicate, normalize_item
from hoax_verifier.schemas import (EvidenceItem, EvidenceStatus, ProviderResult, ProviderStatus,
                                   RetrievalHealth)

NOW = datetime(2026, 10, 6, tzinfo=timezone.utc)


def item(provider="rss", title="Judul berita yang cukup panjang", url="https://a.example/x", **kw) -> EvidenceItem:
    return EvidenceItem(title=title, url=url, domain=kw.pop("domain", "a.example"), provider=provider,
                        retrieval_time=NOW, **kw)


class Fake(EvidenceProvider):
    """Provider whose behaviour is scripted by the test."""

    def __init__(self, name, items=None, error: Exception | None = None, delay: float = 0.0):
        super().__init__(client=None)  # type: ignore[arg-type]
        self.name = name
        self._items, self._error, self._delay = items or [], error, delay
        self.calls = 0

    async def _search(self, query):
        self.calls += 1
        if self._delay:
            await asyncio.sleep(self._delay)
        if self._error:
            raise self._error
        return Found(list(self._items))


class Crashing(Fake):
    async def search(self, query):  # violates the never-raise contract on purpose
        raise RuntimeError("boom")


# ------------------------------------------------------------ normalisation / dedup
def test_normalize_item_drops_unusable_and_cleans_usable():
    assert normalize_item(item(url="ftp://x.example/a")) is None
    assert normalize_item(item(title="   ")) is None
    ok = normalize_item(item(title=" <b>Judul</b>  ", url="https://www.A.example/p/?utm_source=z", domain="www.A.example",
                             snippet="<p>isi</p>"))
    assert ok is not None
    assert ok.title == "Judul" and ok.domain == "a.example" and ok.snippet == "isi"
    assert ok.canonical_url == "https://www.a.example/p"


def test_dedup_merges_same_page_and_keeps_richest_with_provenance():
    a = normalize_item(item("gdelt", url="https://x.example/p?utm_source=a", domain="x.example"))
    b = normalize_item(item("factcheck", url="https://x.example/p", domain="x.example",
                            snippet="isi", extra={"textual_rating": "Salah"}))
    assert a is not None and b is not None
    out, merged = deduplicate([a, b])
    assert merged == 1 and len(out) == 1
    assert out[0].provider == "factcheck" and out[0].also_found_by == ["gdelt"]


def test_dedup_merges_same_domain_and_title_with_different_urls():
    a = normalize_item(item("rss", url="https://x.example/amp/1", title="Pemerintah umumkan kebijakan baru hari ini"))
    b = normalize_item(item("gdelt", url="https://x.example/read/1", title="Pemerintah Umumkan Kebijakan Baru Hari Ini!"))
    assert a is not None and b is not None
    out, merged = deduplicate([a, b])
    assert merged == 1 and len(out) == 1


def test_dedup_keeps_similar_stories_from_different_domains_and_short_titles():
    a = normalize_item(item(url="https://one.example/1", domain="one.example", title="Pemerintah umumkan kebijakan baru"))
    b = normalize_item(item(url="https://two.example/1", domain="two.example", title="Pemerintah umumkan kebijakan baru"))
    c = normalize_item(item(url="https://one.example/2", domain="one.example", title="Berita"))
    d = normalize_item(item(url="https://one.example/3", domain="one.example", title="Berita"))
    assert a is not None and b is not None and c is not None and d is not None
    out, merged = deduplicate([a, b, c, d])
    assert merged == 0 and len(out) == 4  # syndication clustering is a later step, not silent merging here


# ------------------------------------------------------------ status / health semantics
def test_nothing_found_everywhere_is_insufficient_evidence_with_ok_health():
    r = run(EvidenceRetriever([Fake("rss"), Fake("gdelt")]).retrieve(CLAIM))
    assert r.evidence_status is EvidenceStatus.INSUFFICIENT_EVIDENCE
    assert r.retrieval_health is RetrievalHealth.OK       # we searched; there was simply nothing
    assert r.items == [] and any("does NOT indicate" in n for n in r.notes)


def test_all_providers_failing_is_insufficient_evidence_and_unavailable():
    r = run(EvidenceRetriever([Fake("rss", error=ProviderError("HTTP 403")),
                               Fake("gdelt", error=httpx.ConnectError("x"))]).retrieve(CLAIM))
    assert r.evidence_status is EvidenceStatus.INSUFFICIENT_EVIDENCE
    assert r.retrieval_health is RetrievalHealth.UNAVAILABLE
    assert any("absence of evidence is uninformative" in n for n in r.notes)


def test_partial_failure_returns_candidates_and_degraded_health():
    r = run(EvidenceRetriever([Fake("rss", [item()]), Fake("gdelt", error=ProviderError("HTTP 429"))]).retrieve(CLAIM))
    assert r.evidence_status is EvidenceStatus.CANDIDATES_FOUND
    assert r.retrieval_health is RetrievalHealth.DEGRADED
    assert any(n.startswith("gdelt:") for n in r.notes)


def test_disabled_optional_provider_does_not_degrade_health():
    r = run(EvidenceRetriever([Fake("factcheck", error=ProviderDisabled("no key")), Fake("rss", [item()])]).retrieve(CLAIM))
    assert r.retrieval_health is RetrievalHealth.OK
    assert any("factcheck: disabled" in n for n in r.notes)
    assert [p.status for p in r.provider_results][0] is ProviderStatus.DISABLED


def test_only_disabled_providers_means_unavailable():
    r = run(EvidenceRetriever([Fake("factcheck", error=ProviderDisabled("no key"))]).retrieve(CLAIM))
    assert r.retrieval_health is RetrievalHealth.UNAVAILABLE
    assert r.evidence_status is EvidenceStatus.INSUFFICIENT_EVIDENCE


def test_slow_provider_times_out_without_blocking_the_others():
    slow, fast = Fake("gdelt", [item("gdelt")], delay=2.0), Fake("rss", [item("rss", url="https://b.example/y", domain="b.example")])
    r = run(EvidenceRetriever([slow, fast], provider_timeout=0.05).retrieve(CLAIM))
    by = {p.provider: p for p in r.provider_results}
    assert by["gdelt"].status is ProviderStatus.TIMEOUT and by["rss"].status is ProviderStatus.OK
    assert len(r.items) == 1 and r.retrieval_health is RetrievalHealth.DEGRADED


def test_provider_that_breaks_the_contract_cannot_crash_the_pipeline():
    r = run(EvidenceRetriever([Crashing("rss"), Fake("gdelt", [item("gdelt")])]).retrieve(CLAIM))
    by = {p.provider: p for p in r.provider_results}
    assert by["rss"].status is ProviderStatus.ERROR and "RuntimeError" in by["rss"].error
    assert len(r.items) == 1


def test_claim_without_keywords_queries_no_provider():
    p = Fake("rss", [item()])
    r = run(EvidenceRetriever([p]).retrieve("yang dan di ke"))
    assert p.calls == 0
    assert r.evidence_status is EvidenceStatus.INSUFFICIENT_EVIDENCE and r.items == []


def test_invalid_items_are_counted_and_max_candidates_enforced():
    items = [item(url=f"https://s{i}.example/a", domain=f"s{i}.example", title=f"Judul unik nomor {i} panjang sekali")
             for i in range(10)] + [item(url="ftp://bad.example/x")]
    r = run(EvidenceRetriever([Fake("rss", items)], max_candidates=5).retrieve(CLAIM))
    assert r.dropped_invalid == 1 and len(r.items) == 5


def test_result_is_json_serialisable_and_has_no_verdict_fields():
    r = run(EvidenceRetriever([Fake("rss", [item()])]).retrieve(CLAIM))
    text = json.dumps(r.to_dict(), ensure_ascii=False)
    for forbidden in ("is_hoax", "is_true", "trust_score", "hoax_probability", "verdict"):
        assert forbidden not in text
    assert "evidence_status" in text and "retrieved_at" in text and "retrieval_time" in text


# ------------------------------------------------------------ end-to-end with the real providers (mock HTTP)
def test_end_to_end_all_three_real_providers_merge_the_same_fact_check_page(make_client):
    feeds = [FeedConfig("antara-test", "https://antara.test/rss.xml"),
             FeedConfig("tbh-test", "https://tbh.test/feed/", "factcheck")]

    def handler(request):
        host = request.url.host
        if host == "factchecktools.googleapis.com":
            return httpx.Response(200, content=fixture_bytes("factcheck_response.json"))
        if host == "api.gdeltproject.org":
            return httpx.Response(200, content=fixture_bytes("gdelt_response.json"))
        return httpx.Response(200, content=fixture_bytes("news_rss.xml" if host == "antara.test" else "factcheck_rss.xml"))

    client = make_client(handler)
    clock = FakeClock()
    retriever = EvidenceRetriever([
        FactCheckProvider(client, api_key="SECRETKEY123"),
        RSSProvider(client, feeds),
        GDELTProvider(client, clock=clock, min_interval=0),
    ])
    r = run(retriever.retrieve(CLAIM))

    assert r.evidence_status is EvidenceStatus.CANDIDATES_FOUND
    assert r.retrieval_health is RetrievalHealth.OK
    assert [p.status for p in r.provider_results] == [ProviderStatus.OK] * 3

    tbh = [i for i in r.items if i.canonical_url == "https://turnbackhoax.id/2020/03/12/salah-bawang-putih-sembuhkan-covid-19"]
    assert len(tbh) == 1                                       # same page seen by three providers -> one item
    assert tbh[0].provider == "factcheck" and set(tbh[0].also_found_by) == {"rss", "gdelt"}
    assert tbh[0].extra["textual_rating"] == "Salah"           # recorded as metadata, NOT interpreted as stance
    assert r.duplicates_merged == 2 and r.dropped_invalid == 0
    assert any(i.domain == "kompas.com" for i in r.items)       # GDELT-only article kept
    assert any(i.domain == "example-factcheck.org" for i in r.items)  # "www." normalised away
    assert all(i.retrieval_time is not None and i.canonical_url for i in r.items)
    assert r.items[0].provider == "factcheck"                  # stable provider-priority order, not a relevance claim
    assert "SECRETKEY123" not in json.dumps(r.to_dict())
