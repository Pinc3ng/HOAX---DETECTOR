import pytest
from hoax_verifier.reliability import SourceReliabilityRegistry
from hoax_verifier.schemas import ReliabilityTier

def test_reliability_registry_loads_csv():
    registry = SourceReliabilityRegistry()
    
    # Test HIGH reliability domain
    rel_kompas = registry.get_reliability("kompas.com")
    assert rel_kompas.tier == ReliabilityTier.HIGH
    assert rel_kompas.tier.weight == 1.0

    # Test normalization (www.)
    rel_tempo = registry.get_reliability("WwW.TEMPO.co")
    assert rel_tempo.tier == ReliabilityTier.HIGH

    # Test MEDIUM reliability domain
    rel_tribun = registry.get_reliability("tribunnews.com")
    assert rel_tribun.tier == ReliabilityTier.MEDIUM
    assert rel_tribun.tier.weight == 0.75

    # Test BLACKLISTED domain
    rel_bad = registry.get_reliability("nusanews.com")
    assert rel_bad.tier == ReliabilityTier.BLACKLISTED
    assert rel_bad.tier.weight == 0.05

def test_reliability_registry_unknown():
    registry = SourceReliabilityRegistry()
    
    rel_unknown = registry.get_reliability("random-blog-123.com")
    assert rel_unknown.tier == ReliabilityTier.UNKNOWN
    assert rel_unknown.tier.weight == 0.5
