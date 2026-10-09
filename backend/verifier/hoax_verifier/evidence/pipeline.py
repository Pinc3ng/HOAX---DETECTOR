"""Evidence retrieval pipeline.

claim -> query -> provider fan-out -> normalisation -> deduplication -> candidates

What this stage does NOT do (later steps): relevance ranking (embedding), stance (NLI),
source reliability, independent-source clustering, scoring. Its output is a list of
*candidates* plus an honest account of what each provider could and could not do.

``insufficient_evidence`` means "no candidate evidence was retrieved". It is never a verdict on
the claim, and ``retrieval_health`` tells apart "searched, found nothing" from "could not search".
"""
from __future__ import annotations

import asyncio
import logging
from typing import Sequence

import httpx

from ..schemas import (Claim, EvidenceItem, EvidenceStatus, ProviderResult, ProviderStatus,
                       RetrievalHealth, RetrievalResult, utcnow)
from .base import EvidenceProvider, make_client
from .factcheck import FactCheckProvider
from .gdelt import GDELTProvider
from .query import Query, build_query
from .rss import RSSProvider
from .text import canonical_url, clean_title, domain_of, normalize_title, strip_html, truncate

log = logging.getLogger("hoax_verifier.evidence")

PROVIDER_ORDER = {"factcheck": 0, "rss": 1, "gdelt": 2}
_NOT_ANSWERED = (ProviderStatus.ERROR, ProviderStatus.RATE_LIMITED, ProviderStatus.TIMEOUT)
INSUFFICIENT_NOTE = ("No candidate evidence was retrieved. This does NOT indicate that the claim is false "
                     "or a hoax; it only means the available sources returned nothing usable.")


def _rank(provider: str) -> int:
    return PROVIDER_ORDER.get(provider, 99)


def _richness(item: EvidenceItem) -> tuple:
    """Which duplicate to keep: fact-check metadata first, then longer snippet, then provider priority."""
    return (item.extra.get("textual_rating") is not None, len(item.snippet), -_rank(item.provider))


def normalize_item(item: EvidenceItem) -> EvidenceItem | None:
    """Clean one candidate; returns None when it is unusable (no valid http(s) URL or no title)."""
    canon = canonical_url(item.url)
    title = clean_title(item.title)
    if not canon or not title:
        return None
    item.canonical_url = canon
    item.title = title
    item.domain = (item.domain or domain_of(item.url)).lower().removeprefix("www.")
    item.snippet = truncate(strip_html(item.snippet), 600)
    return item


def deduplicate(items: Sequence[EvidenceItem]) -> tuple[list[EvidenceItem], int]:
    """Merge items with the same canonical URL, or the same domain + (long enough) normalised title.

    Merging only collapses *the same page seen twice* (e.g. found by two providers). Different
    outlets publishing similar stories are kept; independent-source clustering is a later step.
    """
    out: list[EvidenceItem] = []
    by_url: dict[str, int] = {}
    by_title: dict[tuple[str, str], int] = {}
    merged = 0
    for item in items:
        tkey = (item.domain, normalize_title(item.title))
        idx = by_url.get(item.canonical_url)
        if idx is None and len(tkey[1]) >= 15:
            idx = by_title.get(tkey)
        if idx is None:
            out.append(item)
            idx = len(out) - 1
        else:
            merged += 1
            kept = out[idx]
            winner, loser = (item, kept) if _richness(item) > _richness(kept) else (kept, item)
            seen = list(dict.fromkeys([*winner.also_found_by, loser.provider, *loser.also_found_by]))
            winner.also_found_by = [p for p in seen if p != winner.provider]
            out[idx] = winner
        by_url[item.canonical_url] = idx
        if len(tkey[1]) >= 15:
            by_title[tkey] = idx
    return out, merged


def _health(results: Sequence[ProviderResult]) -> RetrievalHealth:
    active = [r for r in results if r.status != ProviderStatus.DISABLED]
    if not active or all(r.status in _NOT_ANSWERED for r in active):
        return RetrievalHealth.UNAVAILABLE
    if any(r.status in _NOT_ANSWERED for r in active):
        return RetrievalHealth.DEGRADED
    return RetrievalHealth.OK


class EvidenceRetriever:
    def __init__(self, providers: Sequence[EvidenceProvider], *, provider_timeout: float = 25.0,
                 max_candidates: int = 50, client: httpx.AsyncClient | None = None):
        self.providers = list(providers)
        self.provider_timeout = provider_timeout
        self.max_candidates = max_candidates
        self._owned_client = client  # closed by aclose() when we created it

    @classmethod
    def default(cls, *, factcheck_api_key: str | None = None, **kwargs) -> "EvidenceRetriever":
        """FactCheck (optional key) + RSS + GDELT sharing one HTTP client."""
        client = make_client()
        providers = [FactCheckProvider(client, api_key=factcheck_api_key), RSSProvider(client), GDELTProvider(client)]
        return cls(providers, client=client, **kwargs)

    async def aclose(self) -> None:
        if self._owned_client is not None:
            await self._owned_client.aclose()
            self._owned_client = None

    async def __aenter__(self) -> "EvidenceRetriever":
        return self

    async def __aexit__(self, *exc) -> None:
        await self.aclose()

    async def _run(self, provider: EvidenceProvider, query: Query) -> ProviderResult:
        try:
            return await asyncio.wait_for(provider.search(query), timeout=self.provider_timeout)
        except asyncio.TimeoutError:
            return ProviderResult(provider.name, ProviderStatus.TIMEOUT,
                                  error=f"no answer within {self.provider_timeout:.0f}s",
                                  latency_ms=int(self.provider_timeout * 1000))
        except asyncio.CancelledError:
            raise
        except Exception as e:  # providers should never raise; this is a safety net
            log.exception("provider %s violated the never-raise contract", provider.name)
            return ProviderResult(provider.name, ProviderStatus.ERROR, error=f"unexpected error ({type(e).__name__})")

    async def retrieve(self, claim: Claim | str) -> RetrievalResult:
        claim = claim if isinstance(claim, Claim) else Claim(text=str(claim))
        query = build_query(claim)
        retrieved_at = utcnow()
        notes: list[str] = []

        if not query.keywords:
            notes.append("The claim has no searchable keywords after stop-word removal; no provider was queried.")
            notes.append(INSUFFICIENT_NOTE)
            return RetrievalResult(claim, query.to_dict(), [], EvidenceStatus.INSUFFICIENT_EVIDENCE,
                                   RetrievalHealth.UNAVAILABLE, [], retrieved_at, notes)

        results = list(await asyncio.gather(*(self._run(p, query) for p in self.providers)))

        raw = [i for r in results for i in r.items]
        normalized = [n for n in (normalize_item(i) for i in raw) if n is not None]
        dropped = len(raw) - len(normalized)
        # Stable order: provider priority, then newest first. NOT a relevance ranking (STEP 4).
        normalized.sort(key=lambda i: (_rank(i.provider), -(i.published_at.timestamp() if i.published_at else 0.0)))
        items, merged = deduplicate(normalized)
        items = items[: self.max_candidates]

        for r in results:
            if r.status == ProviderStatus.DISABLED:
                notes.append(f"{r.provider}: disabled ({r.error}).")
            elif r.status in _NOT_ANSWERED:
                notes.append(f"{r.provider}: {r.status.value} ({r.error}); its coverage is missing from this result.")
        health = _health(results)
        status = EvidenceStatus.CANDIDATES_FOUND if items else EvidenceStatus.INSUFFICIENT_EVIDENCE
        if status is EvidenceStatus.INSUFFICIENT_EVIDENCE:
            notes.append(INSUFFICIENT_NOTE)
        if health is RetrievalHealth.UNAVAILABLE:
            notes.append("No provider could be reached or enabled, so absence of evidence is uninformative.")
        return RetrievalResult(claim, query.to_dict(), items, status, health, results, retrieved_at,
                               notes, dropped_invalid=dropped, duplicates_merged=merged)
