from __future__ import annotations

import httpx

from conftest import CLAIM, FakeClock, fixture_bytes, run
from hoax_verifier.evidence import (FactCheckProvider, FeedConfig, GDELTProvider, RSSProvider, build_query)
from hoax_verifier.schemas import ProviderStatus

KEY = "SECRETKEY123"
Q = build_query(CLAIM)


# ---------------------------------------------------------------- FactCheck
def test_factcheck_disabled_without_key_makes_no_request(monkeypatch, make_client):
    monkeypatch.delenv("GOOGLE_FACTCHECK_API_KEY", raising=False)
    calls = []

    def handler(request):
        calls.append(request)
        return httpx.Response(200, json={})

    res = run(FactCheckProvider(make_client(handler)).search(Q))
    assert res.status is ProviderStatus.DISABLED
    assert calls == [] and res.items == []
    assert not res.answered  # "disabled" is not "searched and found nothing"


def test_factcheck_key_from_environment(monkeypatch, make_client):
    monkeypatch.setenv("GOOGLE_FACTCHECK_API_KEY", KEY)
    seen = {}

    def handler(request):
        seen["key"] = request.headers.get("x-goog-api-key")
        return httpx.Response(200, json={})

    res = run(FactCheckProvider(make_client(handler)).search(Q))
    assert seen["key"] == KEY
    assert res.status is ProviderStatus.NO_RESULTS


def test_factcheck_parses_reviews_and_keeps_key_out_of_url(make_client):
    seen = {}

    def handler(request):
        seen["url"] = str(request.url)
        seen["key"] = request.headers.get("x-goog-api-key")
        return httpx.Response(200, content=fixture_bytes("factcheck_response.json"))

    res = run(FactCheckProvider(make_client(handler), api_key=KEY).search(Q))
    assert res.status is ProviderStatus.OK and len(res.items) == 2
    assert KEY not in seen["url"] and seen["key"] == KEY
    first = res.items[0]
    assert first.provider == "factcheck" and first.domain == "turnbackhoax.id"
    assert first.extra["textual_rating"] == "Salah"
    assert first.extra["reviewed_claim"].startswith("Bawang putih")
    assert first.published_at is not None and first.retrieval_time is not None
    assert "Rating penerbit: Salah" in first.snippet


def test_factcheck_empty_answer_is_no_results_not_an_error(make_client):
    res = run(FactCheckProvider(make_client(lambda r: httpx.Response(200, json={})), api_key=KEY).search(Q))
    assert res.status is ProviderStatus.NO_RESULTS and res.answered and res.error is None


def test_factcheck_rejected_key_reports_error_without_leaking_key(make_client):
    res = run(FactCheckProvider(make_client(lambda r: httpx.Response(403, json={"error": KEY})), api_key=KEY).search(Q))
    assert res.status is ProviderStatus.ERROR
    assert "403" in res.error and KEY not in res.error


def test_factcheck_timeout(make_client):
    def handler(request):
        raise httpx.ReadTimeout("slow", request=request)

    res = run(FactCheckProvider(make_client(handler), api_key=KEY).search(Q))
    assert res.status is ProviderStatus.TIMEOUT


def test_factcheck_network_error_message_has_no_url_or_key(make_client):
    def handler(request):
        raise httpx.ConnectError(f"cannot connect to {request.url}", request=request)

    res = run(FactCheckProvider(make_client(handler), api_key=KEY).search(Q))
    assert res.status is ProviderStatus.ERROR
    assert KEY not in res.error and "http" not in res.error


def test_factcheck_cache(make_client):
    n = {"calls": 0}

    def handler(request):
        n["calls"] += 1
        return httpx.Response(200, content=fixture_bytes("factcheck_response.json"))

    async def scenario():
        p = FactCheckProvider(make_client(handler), api_key=KEY)
        return await p.search(Q), await p.search(Q)

    first, second = run(scenario())
    assert n["calls"] == 1 and not first.from_cache and second.from_cache and len(second.items) == 2


# ---------------------------------------------------------------- GDELT
def _gdelt(make_client, handler, clock=None, sleeps=None, **kw):
    clock = clock or FakeClock()
    sleeps = sleeps if sleeps is not None else []

    async def fake_sleep(s):
        sleeps.append(s)
        clock.t += s

    return GDELTProvider(make_client(handler), clock=clock, sleep=fake_sleep, **kw), clock, sleeps


def test_gdelt_parses_articles_and_drops_items_without_url(make_client):
    seen = {}

    def handler(request):
        seen["params"] = dict(request.url.params)
        return httpx.Response(200, content=fixture_bytes("gdelt_response.json"))

    p, _, _ = _gdelt(make_client, handler)
    res = run(p.search(Q))
    assert res.status is ProviderStatus.OK and len(res.items) == 2
    assert "sourcelang:indonesian" in seen["params"]["query"]
    assert "bawang" in seen["params"]["query"] and seen["params"]["mode"] == "artlist"
    assert res.items[0].published_at.year == 2026 and res.items[0].domain == "kompas.com"
    assert any("last 3 months" in n for n in res.notes)


def test_gdelt_throttles_consecutive_requests(make_client):
    n = {"calls": 0}

    def handler(request):
        n["calls"] += 1
        return httpx.Response(200, json={"articles": []})

    p, _, sleeps = _gdelt(make_client, handler, min_interval=5.5)

    async def scenario():
        await p.search(build_query("vaksin flu aman"))
        await p.search(build_query("harga beras naik"))

    run(scenario())
    assert n["calls"] == 2
    assert len(sleeps) == 1 and abs(sleeps[0] - 5.5) < 1e-6


def test_gdelt_caches_successful_answers(make_client):
    n = {"calls": 0}

    def handler(request):
        n["calls"] += 1
        return httpx.Response(200, content=fixture_bytes("gdelt_response.json"))

    p, _, _ = _gdelt(make_client, handler)

    async def scenario():
        return await p.search(Q), await p.search(Q)

    first, second = run(scenario())
    assert n["calls"] == 1 and second.from_cache and len(second.items) == len(first.items)


def test_gdelt_429_triggers_cooldown_without_further_requests(make_client):
    n = {"calls": 0, "mode": 429}

    def handler(request):
        n["calls"] += 1
        if n["mode"] == 429:
            return httpx.Response(429, text="slow down")
        return httpx.Response(200, content=fixture_bytes("gdelt_response.json"))

    p, clock, _ = _gdelt(make_client, handler, cooldown=60.0)

    async def scenario():
        first = await p.search(Q)
        second = await p.search(build_query("harga beras naik"))   # still cooling down
        n["mode"] = 200
        clock.t += 61
        third = await p.search(build_query("harga beras naik"))   # cooldown over
        return first, second, third

    first, second, third = run(scenario())
    assert first.status is ProviderStatus.RATE_LIMITED
    assert second.status is ProviderStatus.RATE_LIMITED and "cooling down" in second.error
    assert n["calls"] == 2  # the second search did not touch the network
    assert third.status is ProviderStatus.OK


def test_gdelt_non_json_answer_is_an_error_and_not_cached(make_client):
    n = {"calls": 0}

    def handler(request):
        n["calls"] += 1
        return httpx.Response(200, text="Your query was too short or too long.")

    p, _, _ = _gdelt(make_client, handler)

    async def scenario():
        return await p.search(Q), await p.search(Q)

    first, second = run(scenario())
    assert first.status is ProviderStatus.ERROR and "non-JSON" in first.error
    assert n["calls"] == 2  # errors are never cached


def test_gdelt_empty_body_is_no_results(make_client):
    p, _, _ = _gdelt(make_client, lambda r: httpx.Response(200, content=b""))
    res = run(p.search(Q))
    assert res.status is ProviderStatus.NO_RESULTS and res.answered


def test_gdelt_without_keywords_makes_no_request(make_client):
    calls = []
    p, _, _ = _gdelt(make_client, lambda r: calls.append(r) or httpx.Response(200, json={}))
    res = run(p.search(build_query("yang dan di")))
    assert res.status is ProviderStatus.NO_RESULTS and calls == []


# ---------------------------------------------------------------- RSS
FEEDS = [FeedConfig("antara-test", "https://antara.test/rss.xml"),
         FeedConfig("tbh-test", "https://tbh.test/feed/", "factcheck")]


def _rss_handler(counter=None, fail_hosts=(), body_override=None):
    def handler(request):
        if counter is not None:
            counter[request.url.host] = counter.get(request.url.host, 0) + 1
        if request.url.host in fail_hosts:
            return httpx.Response(403, text="forbidden")
        if body_override is not None:
            return httpx.Response(200, content=body_override)
        name = "news_rss.xml" if request.url.host == "antara.test" else "factcheck_rss.xml"
        return httpx.Response(200, content=fixture_bytes(name))
    return handler


def test_rss_keeps_matching_entries_ranked_by_keyword_hits_and_declares_it_is_not_historical(make_client):
    res = run(RSSProvider(make_client(_rss_handler()), FEEDS).search(Q))
    assert res.status is ProviderStatus.OK
    titles = [i.title for i in res.items]
    assert titles[0].startswith(("Kemenkes", "[SALAH]"))
    assert not any("emas" in t or "baju putih" in t for t in titles)          # unrelated / 1-keyword hits dropped
    assert any("Harga bawang putih naik" in t for t in titles)                 # weak 2-keyword match passes the filter...
    weak = next(i for i in res.items if "Harga bawang putih" in i.title)
    strong = next(i for i in res.items if i.title.startswith("Kemenkes"))
    assert strong.extra["keyword_hits"] == 4 and weak.extra["keyword_hits"] == 2   # ...but is ranked below
    assert titles.index(strong.title) < titles.index(weak.title)
    assert any("NOT a historical search" in n for n in res.notes)
    kemenkes = strong
    assert kemenkes.domain == "antaranews.com" and kemenkes.published_at.hour == 3  # 10:00 +07 -> 03:00 UTC
    assert "<b>" not in kemenkes.snippet and "belum ada bukti ilmiah" in kemenkes.snippet


def test_rss_matches_morphological_variants_and_aliases(make_client):
    # "corona" -> covid alias; "menyembuhkan" in the claim vs "sembuhkan" in the title.
    res = run(RSSProvider(make_client(_rss_handler()), FEEDS).search(build_query("Bawang putih menyembuhkan virus corona")))
    assert any(i.title.startswith("[SALAH]") for i in res.items)


def test_rss_one_failing_feed_does_not_stop_the_others(make_client):
    res = run(RSSProvider(make_client(_rss_handler(fail_hosts=("tbh.test",))), FEEDS).search(Q))
    assert res.status is ProviderStatus.OK and res.items
    assert any("tbh-test: HTTP 403" in n for n in res.notes)
    assert all(i.extra["feed"] == "antara-test" for i in res.items)


def test_rss_all_feeds_failing_is_an_error_not_no_results(make_client):
    res = run(RSSProvider(make_client(_rss_handler(fail_hosts=("antara.test", "tbh.test"))), FEEDS).search(Q))
    assert res.status is ProviderStatus.ERROR and "all feeds failed" in res.error
    assert not res.answered


def test_rss_unparseable_feed_is_reported(make_client):
    res = run(RSSProvider(make_client(_rss_handler(body_override=b"<html>not a feed</html>")), FEEDS).search(Q))
    assert res.status is ProviderStatus.ERROR and "could not be parsed" in res.error


def test_rss_oversized_response_is_aborted(make_client):
    res = run(RSSProvider(make_client(_rss_handler(body_override=b"x" * 2_100_000)), FEEDS).search(Q))
    assert res.status is ProviderStatus.ERROR and "larger than" in res.error


def test_rss_feed_cache_avoids_refetching(make_client):
    counter = {}

    async def scenario():
        p = RSSProvider(make_client(_rss_handler(counter)), FEEDS)
        return await p.search(Q), await p.search(build_query("harga emas turun"))

    first, second = run(scenario())
    assert counter == {"antara.test": 1, "tbh.test": 1}
    assert not first.from_cache and second.from_cache


def test_rss_without_keywords_makes_no_request(make_client):
    counter = {}
    res = run(RSSProvider(make_client(_rss_handler(counter)), FEEDS).search(build_query("yang dan di")))
    assert counter == {} and res.items == []


def test_default_feed_registry_loads_and_excludes_blocked_publishers():
    from hoax_verifier.evidence import load_default_feeds
    feeds = load_default_feeds()
    assert len(feeds) >= 3
    assert all(f.url.startswith("https://") for f in feeds)
    assert not any("tempo.co" in f.url for f in feeds)  # returned 403; we do not work around it
