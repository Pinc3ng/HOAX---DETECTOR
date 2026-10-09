"""EvidenceProvider abstraction.

Contract: ``provider.search(query)`` NEVER raises (except task cancellation). Every outcome is a
``ProviderResult`` with an explicit status, so the pipeline can tell "searched, nothing found"
apart from "could not search" - and neither is ever interpreted as "the claim is a hoax".

Politeness rules enforced here for every provider:
* identifiable User-Agent, bounded response size, bounded timeouts;
* HTTP 403/401 is reported as an error - there is NO retry, header spoofing or other bypass;
* HTTP 429 is reported as ``rate_limited`` and the provider backs off.
"""
from __future__ import annotations

import abc
import asyncio
import logging
import os
import time
from dataclasses import dataclass, field

import httpx

from ..schemas import EvidenceItem, ProviderResult, ProviderStatus
from .query import Query

log = logging.getLogger("hoax_verifier.evidence")

DEFAULT_MAX_BYTES = 2_000_000
USER_AGENT = "Smart-News-Verifier/0.1 (academic decision-support project; +respects robots/ToS)"


class ProviderError(Exception):
    status = ProviderStatus.ERROR


class ProviderDisabled(ProviderError):
    status = ProviderStatus.DISABLED


class ProviderRateLimited(ProviderError):
    status = ProviderStatus.RATE_LIMITED

    def __init__(self, message: str, retry_after: float | None = None):
        super().__init__(message)
        self.retry_after = retry_after


class ProviderHTTPError(ProviderError):
    def __init__(self, status_code: int, message: str | None = None):
        super().__init__(message or f"HTTP {status_code}")
        self.status_code = status_code


@dataclass
class Found:
    """What a concrete provider returns from ``_search``."""
    items: list[EvidenceItem]
    notes: list[str] = field(default_factory=list)
    from_cache: bool = False


@dataclass
class Fetched:
    status_code: int
    content: bytes
    headers: httpx.Headers


def make_client(timeout: float = 15.0) -> httpx.AsyncClient:
    contact = os.environ.get("HOAX_VERIFIER_CONTACT")
    ua = f"{USER_AGENT} {contact}" if contact else USER_AGENT
    return httpx.AsyncClient(
        headers={"User-Agent": ua, "Accept": "application/json, application/rss+xml, application/xml;q=0.9, */*;q=0.5"},
        timeout=httpx.Timeout(timeout, connect=min(5.0, timeout)),
        follow_redirects=True,
        max_redirects=3,
    )


def _retry_after(headers: httpx.Headers) -> float | None:
    raw = headers.get("retry-after")
    try:
        return float(raw) if raw is not None else None
    except ValueError:
        return None


class EvidenceProvider(abc.ABC):
    name: str = "provider"

    def __init__(self, client: httpx.AsyncClient):
        self.client = client

    # -- to implement ---------------------------------------------------------------------
    @abc.abstractmethod
    async def _search(self, query: Query) -> Found: ...

    def _secrets(self) -> list[str]:
        """Strings that must never appear in logs / error messages (API keys)."""
        return []

    # -- shared ----------------------------------------------------------------------------
    async def search(self, query: Query) -> ProviderResult:
        start = time.perf_counter()

        def done(status, items=None, error=None, notes=None, from_cache=False) -> ProviderResult:
            return ProviderResult(
                provider=self.name, status=status, items=items or [], error=error,
                notes=notes or [], latency_ms=int((time.perf_counter() - start) * 1000), from_cache=from_cache,
            )

        try:
            found = await self._search(query)
        except ProviderError as e:
            return done(e.status, error=self._redact(str(e)))
        except (httpx.TimeoutException, asyncio.TimeoutError):
            return done(ProviderStatus.TIMEOUT, error="request timed out")
        except httpx.HTTPError as e:
            # Only the exception class is reported: httpx messages can embed the full URL.
            return done(ProviderStatus.ERROR, error=f"network error ({type(e).__name__})")
        except asyncio.CancelledError:
            raise
        except Exception as e:  # a provider bug must never take the pipeline down
            log.exception("provider %s crashed", self.name)
            return done(ProviderStatus.ERROR, error=f"unexpected error ({type(e).__name__})")
        status = ProviderStatus.OK if found.items else ProviderStatus.NO_RESULTS
        return done(status, items=found.items, notes=found.notes, from_cache=found.from_cache)

    def _redact(self, message: str) -> str:
        for secret in self._secrets():
            if secret:
                message = message.replace(secret, "***")
        return message

    async def _get(self, url: str, *, params: dict | None = None, headers: dict | None = None,
                   max_bytes: int = DEFAULT_MAX_BYTES) -> Fetched:
        """GET with a hard size cap. Non-2xx responses raise (429 -> rate-limited, others -> HTTP error)."""
        async with self.client.stream("GET", url, params=params, headers=headers) as resp:
            if resp.status_code == 429:
                raise ProviderRateLimited("HTTP 429 Too Many Requests", _retry_after(resp.headers))
            if resp.status_code >= 400:
                raise ProviderHTTPError(resp.status_code)
            buf = bytearray()
            async for chunk in resp.aiter_bytes():
                buf.extend(chunk)
                if len(buf) > max_bytes:
                    raise ProviderError(f"response larger than {max_bytes} bytes; aborted")
            return Fetched(resp.status_code, bytes(buf), resp.headers)
