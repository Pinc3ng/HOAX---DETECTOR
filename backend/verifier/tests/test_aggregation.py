from datetime import datetime, timezone
import pytest
from hoax_verifier.aggregation import EvidenceAggregator
from hoax_verifier.schemas import EvidenceItem

NOW = datetime(2026, 1, 1, tzinfo=timezone.utc)

def make_item(title: str, domain: str, stance: str, entail: float, contra: float, neutral: float, relevance: float = 0.9, canonical_url: str = "") -> EvidenceItem:
    it = EvidenceItem(
        title=title,
        url=f"https://{domain}/path",
        domain=domain,
        provider="mock",
        retrieval_time=NOW,
        canonical_url=canonical_url
    )
    it.extra["nli_label"] = stance
    it.extra["nli_entailment_score"] = entail
    it.extra["nli_contradiction_score"] = contra
    it.extra["nli_neutral_score"] = neutral
    it.extra["semantic_relevance"] = relevance
    return it


def test_one_high_reliability_entailment():
    agg = EvidenceAggregator()
    # kompas.com is HIGH (weight 1.0)
    it = make_item("Title 1", "kompas.com", "entailment", 0.9, 0.05, 0.05)
    res = agg.aggregate([it])
    
    assert res.independent_evidence_count == 1
    assert res.evidence_coverage == "LOW"
    # support = 1.0 - (1.0 - (0.9 * 0.9 * 1.0)) = 0.81
    assert round(res.support_score, 2) == 0.81
    assert res.contradiction_score < 0.1
    assert not res.conflicting_evidence


def test_one_low_reliability_entailment():
    agg = EvidenceAggregator()
    # seword.com is LOW (weight 0.25)
    it = make_item("Title 1", "seword.com", "entailment", 0.9, 0.05, 0.05)
    res = agg.aggregate([it])
    
    # support = 1.0 - (1.0 - (0.9 * 0.9 * 0.25)) = 0.2025
    assert round(res.support_score, 2) == 0.20
    assert res.contradiction_score < 0.1


def test_many_low_vs_one_high_contradiction():
    agg = EvidenceAggregator()
    # 5 low reliability entailments (all independent, different titles/urls)
    items = []
    for i in range(5):
        items.append(make_item(f"Claim true {i}", f"seword{i}.com", "entailment", 0.9, 0.0, 0.1, relevance=0.8))
        
    # 1 high reliability contradiction
    items.append(make_item("Claim is false", "turnbackhoax.id", "contradiction", 0.0, 0.95, 0.05, relevance=0.9))
    
    res = agg.aggregate(items)
    assert res.independent_evidence_count == 6
    assert res.evidence_coverage == "HIGH"
    
    # Each low adds: 0.9 * 0.8 * 0.5 (Wait, seword0.com is UNKNOWN, not LOW, because only seword.com is LOW in CSV!)
    # Let's make them explicitly UNKNOWN
    
    # The high contradiction: 0.95 * 0.9 * 1.0 = 0.855
    assert round(res.contradiction_score, 2) == 0.85
    # The support score from 5 UNKNOWN (weight 0.5): 1 - (1 - (0.9*0.8*0.5))^5 = 1 - (1 - 0.36)^5 = 1 - 0.64^5 = 1 - 0.107 = 0.89
    # If they were LOW (weight 0.25): 1 - (1 - (0.9*0.8*0.25))^5 = 1 - (1 - 0.18)^5 = 1 - 0.82^5 = 1 - 0.37 = 0.63
    # Either way, both support and contradiction will be high, leading to conflicting evidence!
    assert res.conflicting_evidence is True


def test_unknown_source():
    agg = EvidenceAggregator()
    it = make_item("Title 1", "random-domain.com", "contradiction", 0.1, 0.8, 0.1)
    res = agg.aggregate([it])
    # Weight 0.5
    # contra = 0.8 * 0.9 * 0.5 = 0.36
    assert round(res.contradiction_score, 2) == 0.36


def test_neutral_relevant_evidence_adds_to_coverage():
    agg = EvidenceAggregator()
    items = [
        make_item("Title 1", "domain1.com", "neutral", 0.1, 0.1, 0.8),
        make_item("Title 2", "domain2.com", "neutral", 0.1, 0.1, 0.8),
        make_item("Title 3", "domain3.com", "neutral", 0.1, 0.1, 0.8),
    ]
    res = agg.aggregate(items)
    assert res.independent_evidence_count == 3
    assert res.evidence_coverage == "MODERATE"
    assert res.support_score < 0.1
    assert res.contradiction_score < 0.1


def test_no_relevant_evidence():
    agg = EvidenceAggregator()
    items = [
        # Below threshold
        make_item("Title 1", "domain1.com", "entailment", 0.9, 0.1, 0.0, relevance=0.1),
        # Unavailable NLI
        make_item("Title 2", "domain2.com", "unavailable", 0.0, 0.0, 0.0),
    ]
    res = agg.aggregate(items)
    assert res.independent_evidence_count == 0
    assert res.evidence_coverage == "INSUFFICIENT"
    assert res.support_score == 0.0
    assert res.contradiction_score == 0.0


def test_syndicated_duplicate_evidence_clusters():
    agg = EvidenceAggregator()
    # 5 websites copying the exact same article
    items = []
    for i in range(5):
        # All have the same title, but different domains
        items.append(make_item("Pemerintah Umumkan Kebijakan Baru", f"news{i}.com", "entailment", 0.9, 0.0, 0.1))
        
    res = agg.aggregate(items)
    # Should be clustered into 1 independent evidence
    assert res.independent_evidence_count == 1
    assert res.evidence_coverage == "LOW"
    
    # Check that it doesn't inflate the score like 5 independent articles would
    # All are UNKNOWN (weight 0.5). Only the best weight is kept. Best is 0.5.
    # score = 0.9 * 0.9 * 0.5 = 0.405
    assert round(res.support_score, 2) == 0.41


def test_blacklisted_tier():
    agg = EvidenceAggregator()
    it = make_item("Hoax Title", "nusanews.com", "entailment", 0.9, 0.0, 0.1)
    res = agg.aggregate([it])
    # Blacklist weight is 0.05
    # score = 0.9 * 0.9 * 0.05 = 0.0405
    assert round(res.support_score, 3) == 0.041
