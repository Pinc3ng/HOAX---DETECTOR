"""Core data structures shared by the verification pipeline.

Honesty rules encoded here:

* ``EvidenceItem`` is a *candidate* retrieved from a provider. It is not yet filtered for relevance
  (embedding step) nor assigned a stance (NLI step).
* ``evidence_status`` is only ever ``candidates_found`` or ``insufficient_evidence``. There is no
  value that means "false" / "hoax": finding nothing is not a verdict on the claim.
* ``retrieval_health`` separates "we searched and found nothing" from "we could not search".
"""
from __future__ import annotations

from dataclasses import dataclass, field
from datetime import datetime, timezone
from enum import Enum
from typing import Any


def utcnow() -> datetime:
    return datetime.now(timezone.utc)


def _iso(dt: datetime | None) -> str | None:
    return dt.isoformat() if dt else None


class ProviderStatus(str, Enum):
    OK = "ok"                      # searched, items returned
    NO_RESULTS = "no_results"      # searched successfully, nothing matched
    DISABLED = "disabled"          # not configured (e.g. no API key); nothing was requested
    RATE_LIMITED = "rate_limited"  # provider asked us to slow down (or we are cooling down)
    TIMEOUT = "timeout"
    ERROR = "error"                # network / HTTP / parsing failure


class EvidenceStatus(str, Enum):
    CANDIDATES_FOUND = "candidates_found"
    INSUFFICIENT_EVIDENCE = "insufficient_evidence"


class RetrievalHealth(str, Enum):
    OK = "ok"                    # every enabled provider answered
    DEGRADED = "degraded"        # at least one provider failed / was throttled
    UNAVAILABLE = "unavailable"  # no provider could answer at all


@dataclass(frozen=True)
class Claim:
    text: str
    language: str = "id"


@dataclass
class EvidenceItem:
    title: str
    url: str
    domain: str
    provider: str
    retrieval_time: datetime
    published_at: datetime | None = None
    snippet: str = ""
    canonical_url: str = ""
    also_found_by: list[str] = field(default_factory=list)
    # Provider-specific metadata, e.g. fact-check ``textual_rating`` / ``reviewed_claim``.
    # The rating is *recorded only*; stance is decided later by NLI.
    extra: dict[str, Any] = field(default_factory=dict)

    def to_dict(self) -> dict:
        return {
            "title": self.title,
            "url": self.url,
            "canonical_url": self.canonical_url,
            "domain": self.domain,
            "provider": self.provider,
            "also_found_by": list(self.also_found_by),
            "published_at": _iso(self.published_at),
            "retrieval_time": _iso(self.retrieval_time),
            "snippet": self.snippet,
            "extra": dict(self.extra),
        }


@dataclass
class ProviderResult:
    provider: str
    status: ProviderStatus
    items: list[EvidenceItem] = field(default_factory=list)
    error: str | None = None
    notes: list[str] = field(default_factory=list)
    latency_ms: int = 0
    from_cache: bool = False

    @property
    def answered(self) -> bool:
        """True when the provider actually searched (even if it found nothing)."""
        return self.status in (ProviderStatus.OK, ProviderStatus.NO_RESULTS)

    def to_dict(self) -> dict:
        return {
            "provider": self.provider,
            "status": self.status.value,
            "n_items": len(self.items),
            "error": self.error,
            "notes": list(self.notes),
            "latency_ms": self.latency_ms,
            "from_cache": self.from_cache,
        }


@dataclass
class RetrievalResult:
    claim: Claim
    query: dict
    items: list[EvidenceItem]
    evidence_status: EvidenceStatus
    retrieval_health: RetrievalHealth
    provider_results: list[ProviderResult]
    retrieved_at: datetime
    notes: list[str] = field(default_factory=list)
    dropped_invalid: int = 0
    duplicates_merged: int = 0

    def to_dict(self) -> dict:
        return {
            "claim": {"text": self.claim.text, "language": self.claim.language},
            "query": self.query,
            "evidence_status": self.evidence_status.value,
            "retrieval_health": self.retrieval_health.value,
            "retrieved_at": _iso(self.retrieved_at),
            "n_candidates": len(self.items),
            "dropped_invalid": self.dropped_invalid,
            "duplicates_merged": self.duplicates_merged,
            "notes": list(self.notes),
            "providers": [p.to_dict() for p in self.provider_results],
            "items": [i.to_dict() for i in self.items],
        }


class ReliabilityTier(str, Enum):
    HIGH = "high"
    MEDIUM = "medium"
    LOW = "low"
    UNKNOWN = "unknown"
    BLACKLISTED = "blacklisted"

    @property
    def weight(self) -> float:
        # Default numeric weights for tiers
        mapping = {
            "high": 1.0,
            "medium": 0.75,
            "low": 0.25,
            "unknown": 0.5,
            "blacklisted": 0.05,
        }
        return mapping[self.value]


@dataclass(frozen=True)
class SourceReliability:
    domain: str
    tier: ReliabilityTier
    description: str = ""

    def to_dict(self) -> dict:
        return {
            "domain": self.domain,
            "tier": self.tier.value,
            "weight": self.tier.weight,
            "description": self.description,
        }


@dataclass
class AggregatedEvidence:
    support_score: float = 0.0
    contradiction_score: float = 0.0
    evidence_coverage: str = "INSUFFICIENT"  # e.g., INSUFFICIENT, LOW, MODERATE, HIGH
    independent_evidence_count: int = 0
    supporting_evidence: list[EvidenceItem] = field(default_factory=list)
    contradicting_evidence: list[EvidenceItem] = field(default_factory=list)
    neutral_evidence: list[EvidenceItem] = field(default_factory=list)
    conflicting_evidence: bool = False

    def to_dict(self) -> dict:
        return {
            "support_score": self.support_score,
            "contradiction_score": self.contradiction_score,
            "evidence_coverage": self.evidence_coverage,
            "independent_evidence_count": self.independent_evidence_count,
            "supporting_evidence": [i.to_dict() for i in self.supporting_evidence],
            "contradicting_evidence": [i.to_dict() for i in self.contradicting_evidence],
            "neutral_evidence": [i.to_dict() for i in self.neutral_evidence],
            "conflicting_evidence": self.conflicting_evidence,
        }
