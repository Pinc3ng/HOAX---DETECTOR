"""Evidence Aggregation module.

This module implements STEP 6 (Source Reliability + Evidence Aggregation).
It groups independent evidence, applies reliability weights, and computes
overall support/contradiction scores.

Rules:
- Reliability is a weight, not a truth verdict.
- Threshold limits irrelevant evidence.
- Scores are normalized to [0,1].
"""
import logging
from typing import Sequence

from .schemas import AggregatedEvidence, EvidenceItem
from .reliability import SourceReliabilityRegistry

log = logging.getLogger("hoax_verifier.aggregation")


class EvidenceAggregator:
    def __init__(self, relevance_threshold: float = 0.2):
        self.registry = SourceReliabilityRegistry()
        self.relevance_threshold = relevance_threshold

    def _cluster_key(self, item: EvidenceItem) -> str:
        """
        Simple clustering heuristic to prevent double-counting syndicated articles.
        Limitations: This relies on exact title match or exact canonical URL. 
        It will fail to cluster heavily re-written syndicated articles, but is safe 
        enough for the baseline.
        """
        if item.canonical_url:
            return f"url:{item.canonical_url.lower()}"
        # Fallback to normalized title
        # Strip common punctuation and lowercase
        norm_title = "".join(c for c in item.title.lower() if c.isalnum() or c.isspace()).strip()
        if not norm_title:
            # If title is completely empty after normalization, fallback to domain to at least cluster by site
            return f"domain:{item.domain}"
        return f"title:{norm_title}"

    def aggregate(self, items: Sequence[EvidenceItem]) -> AggregatedEvidence:
        clusters: dict[str, list[EvidenceItem]] = {}
        
        # 1. Filter and Cluster
        for item in items:
            relevance = item.extra.get("semantic_relevance", 0.0)
            stance = item.extra.get("nli_label", "unavailable")
            
            # Skip irrelevant or unavailable stance
            if relevance < self.relevance_threshold or stance == "unavailable":
                continue
                
            key = self._cluster_key(item)
            clusters.setdefault(key, []).append(item)

        if not clusters:
            return AggregatedEvidence(
                support_score=0.0,
                contradiction_score=0.0,
                evidence_coverage="INSUFFICIENT",
                independent_evidence_count=0
            )

        # 2. Select cluster representative and compute scores
        # Using probabilistic sum: 1 - prod(1 - x) to keep score in [0,1]
        prod_non_support = 1.0
        prod_non_contradict = 1.0
        
        supporting = []
        contradicting = []
        neutral = []

        for key, cluster_items in clusters.items():
            # Find the most reliable item in the cluster to represent it
            best_item = None
            best_weight = -1.0
            best_reliability = None
            
            for it in cluster_items:
                rel = self.registry.get_reliability(it.domain)
                if rel.tier.weight > best_weight:
                    best_weight = rel.tier.weight
                    best_item = it
                    best_reliability = rel

            assert best_item is not None
            assert best_reliability is not None
            
            # Record the reliability for explainability later
            best_item.extra["source_reliability_tier"] = best_reliability.tier.value
            best_item.extra["source_reliability_weight"] = best_weight
            
            stance = best_item.extra.get("nli_label")
            relevance = best_item.extra.get("semantic_relevance", 0.0)
            entail_score = best_item.extra.get("nli_entailment_score", 0.0)
            contra_score = best_item.extra.get("nli_contradiction_score", 0.0)
            
            # Weighted contribution to prevent single high-score from blowing up the sum
            # NLI confidence * relevance * source reliability
            # Neutral evidence only adds to coverage, not to support/contradiction
            if stance == "entailment":
                cluster_support_contribution = entail_score * relevance * best_weight
                prod_non_support *= (1.0 - cluster_support_contribution)
                supporting.append(best_item)
            elif stance == "contradiction":
                cluster_contra_contribution = contra_score * relevance * best_weight
                prod_non_contradict *= (1.0 - cluster_contra_contribution)
                contradicting.append(best_item)
            elif stance == "neutral":
                neutral.append(best_item)

        # 3. Finalize scores
        final_support = 1.0 - prod_non_support
        final_contradict = 1.0 - prod_non_contradict

        # Determine coverage qualitatively
        n_clusters = len(clusters)
        if n_clusters == 0:
            coverage = "INSUFFICIENT"
        elif n_clusters <= 2:
            coverage = "LOW"
        elif n_clusters <= 5:
            coverage = "MODERATE"
        else:
            coverage = "HIGH"

        # Determine conflict
        # If both support and contradiction are notable (e.g. > 0.3), they conflict
        is_conflicting = (final_support > 0.3 and final_contradict > 0.3)

        return AggregatedEvidence(
            support_score=round(final_support, 4),
            contradiction_score=round(final_contradict, 4),
            evidence_coverage=coverage,
            independent_evidence_count=n_clusters,
            supporting_evidence=supporting,
            contradicting_evidence=contradicting,
            neutral_evidence=neutral,
            conflicting_evidence=is_conflicting
        )
