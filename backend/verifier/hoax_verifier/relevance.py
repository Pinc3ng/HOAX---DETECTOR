"""Semantic relevance ranking module.

This module implements STEP 4 (Embedding).
It takes candidates retrieved by the EvidenceRetriever and ranks them based on their
semantic similarity to the claim using a pre-trained sentence transformer.

Rules:
- Executed on CPU only (as per constraints).
- Embeddings are used ONLY for ranking candidates, NOT for determining truth/hoax.
- Stance classification happens later (NLI, STEP 5).
"""
from __future__ import annotations

import logging
from typing import Sequence

from .schemas import Claim, EvidenceItem

log = logging.getLogger("hoax_verifier.relevance")

try:
    from sentence_transformers import SentenceTransformer, util
    _HAS_SBERT = True
except ImportError:
    _HAS_SBERT = False


class SemanticRanker:
    """Ranks evidence candidates by their semantic similarity to the claim."""

    def __init__(self, model_name: str = "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2"):
        if not _HAS_SBERT:
            raise ImportError("sentence-transformers is required for semantic ranking. Install with `pip install .[nlp]`.")
        
        self.model_name = model_name
        self.device = "cpu"  # Explicitly constrained to CPU
        self._model = None

    def _load_model(self) -> None:
        if self._model is None:
            log.info("Loading embedding model %s on %s...", self.model_name, self.device)
            self._model = SentenceTransformer(self.model_name, device=self.device)

    def rank(self, claim: Claim, items: Sequence[EvidenceItem]) -> list[EvidenceItem]:
        """Rank the items by semantic relevance to the claim and populate extra['semantic_relevance']."""
        if not items:
            return list(items)

        self._load_model()
        assert self._model is not None

        claim_text = claim.text
        # Combine title and snippet to represent the article's context
        item_texts = [f"{item.title}. {item.snippet}" for item in items]

        # Convert to tensor and compute cosine similarities
        claim_embedding = self._model.encode(claim_text, convert_to_tensor=True, show_progress_bar=False)
        item_embeddings = self._model.encode(item_texts, convert_to_tensor=True, show_progress_bar=False)

        cosine_scores = util.cos_sim(claim_embedding, item_embeddings)[0]

        scored_items = []
        for i, item in enumerate(items):
            score = cosine_scores[i].item()
            # Store in extra as specified
            item.extra["semantic_relevance"] = score
            scored_items.append((score, item))

        # Sort descending by score
        scored_items.sort(key=lambda x: x[0], reverse=True)
        return [item for _, item in scored_items]
