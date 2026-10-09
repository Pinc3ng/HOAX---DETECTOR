"""RSS provider: the LATEST items of a configured set of public feeds, filtered locally by keyword.

This is NOT a search engine and NOT a historical search: a feed only exposes its most recent
entries (typically tens of items). An empty result here says nothing about whether older
coverage exists. Its value is catching *breaking / very recent* reporting.

Feeds are fetched concurrently and cached (default 10 min) so repeated verifications do not
hammer publishers. A failing feed (403, timeout, bad XML) is reported but does not stop the others;
there is no workaround for a 403.
"""
from __future__ import annotations

import asyncio
import json
import time
from dataclasses import dataclass
from datetime import datetime
from importlib import resources
from typing import Callable

import feedparser
import httpx

from ..schemas import EvidenceItem, utcnow
from .base import EvidenceProvider, Found, ProviderError
from .cache import TTLCache
from .query import Query, token_forms
from .text import canonical_url, clean_title, domain_of, parse_datetime, strip_html, truncate


@dataclass(frozen=True)
class FeedConfig:
    name: str
    url: str
    category: str = "news"   # "news" | "factcheck" (informational; reliability is a later step)


def load_default_feeds() -> list[FeedConfig]:
    raw = resources.files("hoax_verifier").joinpath("data/rss_feeds.json").read_text(encoding="utf-8")
    return [FeedConfig(**d) for d in json.loads(raw)]


@dataclass
class _Entry:
    title: str
    link: str
    summary: str
    published: datetime | None
    forms: frozenset[str]


@dataclass
class _ParsedFeed:
    entries: list[_Entry]
    fetched_at: datetime


class RSSProvider(EvidenceProvider):
    name = "rss"

    def __init__(self, client: httpx.AsyncClient, feeds: list[FeedConfig] | None = None, *,
                 cache_ttl: float = 600.0, max_results: int = 20,
                 clock: Callable[[], float] = time.monotonic):
        super().__init__(client)
        self.feeds = feeds if feeds is not None else load_default_feeds()
        self.max_results = max_results
        self._cache: TTLCache[_ParsedFeed] = TTLCache(cache_ttl, clock=clock)

    async def _load_feed(self, feed: FeedConfig) -> tuple[_ParsedFeed, bool]:
        cached = self._cache.get(feed.url)
        if cached is not None:
            return cached, True
        resp = await self._get(feed.url)
        parsed = feedparser.parse(resp.content)
        if not parsed.entries:
            raise ProviderError("feed could not be parsed or has no entries")
        entries = []
        for e in parsed.entries:
            title = clean_title(e.get("title"))
            link = e.get("link") or ""
            if not title or not link:
                continue
            summary = truncate(strip_html(e.get("summary") or e.get("description") or ""), 600)
            published = parse_datetime(e.get("published_parsed") or e.get("updated_parsed")
                                       or e.get("published") or e.get("updated"))
            entries.append(_Entry(title, link, summary, published, token_forms(f"{title} {summary}")))
        result = _ParsedFeed(entries, utcnow())
        self._cache.set(feed.url, result)
        return result, False

    async def _search(self, query: Query) -> Found:
        notes = ["RSS only exposes the most recent items of each feed; it is NOT a historical search."]
        if not query.keywords:
            return Found([], notes + ["no searchable keywords in claim"])

        outcomes = await asyncio.gather(*(self._load_feed(f) for f in self.feeds), return_exceptions=True)
        loaded: list[tuple[FeedConfig, _ParsedFeed, bool]] = []
        failures: list[str] = []
        for feed, out in zip(self.feeds, outcomes):
            if isinstance(out, asyncio.CancelledError):
                raise out
            if isinstance(out, ProviderError):
                failures.append(f"{feed.name}: {self._redact(str(out))}")
            elif isinstance(out, httpx.TimeoutException):
                failures.append(f"{feed.name}: timed out")
            elif isinstance(out, httpx.HTTPError):
                failures.append(f"{feed.name}: network error ({type(out).__name__})")
            elif isinstance(out, BaseException):
                failures.append(f"{feed.name}: unexpected error ({type(out).__name__})")
            else:
                loaded.append((feed, out[0], out[1]))
        if not loaded:
            raise ProviderError("all feeds failed -> " + "; ".join(failures))
        if failures:
            notes.append("feeds failed: " + "; ".join(failures))

        need = query.min_hits()
        scored: list[tuple[int, datetime | None, EvidenceItem]] = []
        for feed, parsed, _ in loaded:
            for entry in parsed.entries:
                matched = [k.term for k in query.keywords if k.forms & entry.forms]
                if len(matched) < need:
                    continue
                scored.append((len(matched), entry.published, EvidenceItem(
                    title=entry.title, url=entry.link, domain=domain_of(entry.link), provider=self.name,
                    retrieval_time=parsed.fetched_at, published_at=entry.published, snippet=entry.summary,
                    canonical_url=canonical_url(entry.link) or entry.link,
                    extra={"feed": feed.name, "feed_category": feed.category,
                           "keyword_hits": len(matched), "keyword_total": len(query.keywords), "matched": matched},
                )))
        scored.sort(key=lambda t: (-t[0], -(t[1].timestamp() if t[1] else 0.0)))
        items = [s[2] for s in scored[: self.max_results]]
        notes.append(f"scanned {sum(len(p.entries) for _, p, _ in loaded)} recent entries "
                     f"from {len(loaded)}/{len(self.feeds)} feeds")
        return Found(items, notes, from_cache=all(c for _, _, c in loaded))
