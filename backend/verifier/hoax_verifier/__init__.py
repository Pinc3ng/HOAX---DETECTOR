"""Evidence-based verification engine (decision-support; not a truth detector)."""
__version__ = "0.1.0"

from .relevance import SemanticRanker
from .nli import StanceClassifier
from .reliability import SourceReliabilityRegistry
from .aggregation import EvidenceAggregator

__all__ = ["SemanticRanker", "StanceClassifier", "SourceReliabilityRegistry", "EvidenceAggregator"]
