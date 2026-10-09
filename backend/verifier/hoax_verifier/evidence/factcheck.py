"""Google Fact Check Tools provider (optional: needs ``GOOGLE_FACTCHECK_API_KEY``).

Without a key the provider reports ``disabled`` and performs NO request. A missing key, or an
empty answer, says nothing about the claim itself.

Each ``claimReview`` becomes one evidence candidate. The publisher's rating (``textual_rating``)
and the reviewed claim text are stored in ``extra`` for the later steps; they are NOT converted
into a stance here. NLI first has to establish that the reviewed claim is actually the user's
claim, only then can a rating be meaningful.
"""
from __future__ import annotations

import json
import os

import httpx

from ..schemas import EvidenceItem, utcnow
from .base import EvidenceProvider, Found, ProviderDisabled, ProviderError
from .cache import TTLCache
from .query import Query
from .text import canonical_url, clean_title, domain_of, parse_datetime, strip_html, truncate

ENDPOINT = "https://factchecktools.googleapis.com/v1alpha1/claims:search"
ENV_KEY = "GOOGLE_FACTCHECK_API_KEY"


class FactCheckProvider(EvidenceProvider):
    name = "factcheck"

    def __init__(self, client: httpx.AsyncClient, api_key: str | None = None, *, language: str = "id",
                 page_size: int = 10, cache_ttl: float = 3600.0):
        super().__init__(client)
        self._api_key = api_key
        self.language = language
        self.page_size = page_size
        self._cache: TTLCache[list[EvidenceItem]] = TTLCache(cache_ttl)

    @property
    def api_key(self) -> str | None:
        return self._api_key or os.environ.get(ENV_KEY) or None

    def _secrets(self) -> list[str]:
        return [self.api_key or ""]

    async def _search(self, query: Query) -> Found:
        key = self.api_key
        if not key:
            raise ProviderDisabled(f"{ENV_KEY} is not set (optional provider; nothing was requested)")
        text = query.claim_text[:200].strip()
        if not text:
            return Found([], ["empty claim text"])

        cache_key = (text.lower(), self.language)
        cached = self._cache.get(cache_key)
        if cached is not None:
            return Found(list(cached), from_cache=True)

        # Key goes in a header, not the URL, so it can never leak through URLs in logs/errors.
        try:
            resp = await self._get(
                ENDPOINT,
                params={"query": text, "languageCode": self.language, "pageSize": self.page_size},
                headers={"x-goog-api-key": key},
            )
        except ProviderError as e:
            status = getattr(e, "status_code", None)
            if status in (400, 401, 403):
                raise ProviderError(f"Fact Check API rejected the request (HTTP {status}); check the API key") from None
            raise
        try:
            payload = json.loads(resp.content or b"{}")
        except ValueError:
            raise ProviderError("Fact Check API returned a non-JSON response") from None

        retrieved = utcnow()
        items: list[EvidenceItem] = []
        for claim in payload.get("claims") or []:
            reviewed_claim = (claim.get("text") or "").strip()
            for review in claim.get("claimReview") or []:
                url = review.get("url") or ""
                publisher = review.get("publisher") or {}
                title = clean_title(review.get("title")) or reviewed_claim
                if not url or not title:
                    continue
                rating = (review.get("textualRating") or "").strip()
                snippet = truncate(strip_html(
                    f'Klaim yang ditinjau: "{reviewed_claim}". Rating penerbit: {rating or "tidak disebutkan"}.'
                ), 600)
                items.append(EvidenceItem(
                    title=title, url=url, domain=publisher.get("site") or domain_of(url), provider=self.name,
                    retrieval_time=retrieved,
                    published_at=parse_datetime(review.get("reviewDate") or claim.get("claimDate")),
                    snippet=snippet, canonical_url=canonical_url(url) or url,
                    extra={
                        "reviewed_claim": reviewed_claim,
                        "claimant": claim.get("claimant"),
                        "textual_rating": rating or None,
                        "publisher": publisher.get("name"),
                        "review_language": review.get("languageCode"),
                    },
                ))
        self._cache.set(cache_key, list(items))
        notes = ["Publisher ratings are recorded as metadata only; stance is decided by NLI in a later step."]
        return Found(items, notes)
