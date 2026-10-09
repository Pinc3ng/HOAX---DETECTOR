"""GDELT DOC 2.0 provider (news search, ~last 3 months, title-level metadata only).

GDELT is heavily rate limited (HTTP 429 observed in practice), so this provider has:
* a minimum interval between requests (throttle),
* a cooldown after any 429 during which NO request is made,
* an in-memory TTL cache for successful answers (including "no results"); errors are never cached.
"""
from __future__ import annotations

import asyncio
import json
import time
from typing import Awaitable, Callable

import httpx

from ..schemas import EvidenceItem, utcnow
from .base import EvidenceProvider, Found, ProviderError, ProviderRateLimited
from .cache import TTLCache
from .query import Query
from .text import canonical_url, clean_title, domain_of, parse_datetime

ENDPOINT = "https://api.gdeltproject.org/api/v2/doc/doc"


class GDELTProvider(EvidenceProvider):
    name = "gdelt"

    def __init__(self, client: httpx.AsyncClient, *, min_interval: float = 5.5, cooldown: float = 60.0,
                 cache_ttl: float = 900.0, max_records: int = 15, language: str = "indonesian",
                 clock: Callable[[], float] = time.monotonic,
                 sleep: Callable[[float], Awaitable[None]] = asyncio.sleep):
        super().__init__(client)
        self.min_interval = min_interval
        self.cooldown = cooldown
        self.max_records = max_records
        self.language = language
        self._clock = clock
        self._sleep = sleep
        self._cache: TTLCache[list[EvidenceItem]] = TTLCache(cache_ttl, clock=clock)
        self._lock = asyncio.Lock()
        self._last_request: float | None = None
        self._blocked_until: float = 0.0

    async def _search(self, query: Query) -> Found:
        kw = query.keyword_query(limit=4)
        if len(kw) < 3:
            return Found([], ["no searchable keywords in claim"])
        search = f"{kw} sourcelang:{self.language}"
        notes = ["GDELT DOC covers roughly the last 3 months and returns titles only (no article text)."]

        cached = self._cache.get(search)
        if cached is not None:
            return Found(list(cached), notes, from_cache=True)

        async with self._lock:  # serialises requests so the throttle is global to this provider
            cached = self._cache.get(search)  # another waiter may have filled it meanwhile
            if cached is not None:
                return Found(list(cached), notes, from_cache=True)

            now = self._clock()
            if now < self._blocked_until:
                raise ProviderRateLimited(
                    f"cooling down after HTTP 429 for another {self._blocked_until - now:.0f}s (no request made)")
            if self._last_request is not None:
                wait = self.min_interval - (now - self._last_request)
                if wait > 0:
                    await self._sleep(wait)
            self._last_request = self._clock()
            try:
                resp = await self._get(ENDPOINT, params={
                    "query": search, "mode": "artlist", "format": "json",
                    "maxrecords": self.max_records, "sort": "hybridrel",
                })
            except ProviderRateLimited as e:
                self._blocked_until = self._clock() + max(self.cooldown, e.retry_after or 0.0)
                raise

        body = resp.content.strip()
        if not body:
            articles = []
        else:
            try:
                articles = (json.loads(body) or {}).get("articles") or []
            except ValueError:
                snippet = body[:120].decode("utf-8", "replace").replace("\n", " ")
                raise ProviderError(f"GDELT returned a non-JSON answer (query may have been rejected): {snippet!r}") from None

        retrieved = utcnow()
        items: list[EvidenceItem] = []
        for a in articles:
            url = a.get("url") or ""
            title = clean_title(a.get("title"))
            if not url or not title:
                continue
            items.append(EvidenceItem(
                title=title, url=url, domain=a.get("domain") or domain_of(url), provider=self.name,
                retrieval_time=retrieved, published_at=parse_datetime(a.get("seendate")), snippet="",
                canonical_url=canonical_url(url) or url,
                extra={"language": a.get("language"), "source_country": a.get("sourcecountry"),
                       "matched_query": search},
            ))
        self._cache.set(search, list(items))
        return Found(items, notes)
