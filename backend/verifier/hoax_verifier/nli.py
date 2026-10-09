"""Natural Language Inference (NLI) module.

This module implements STEP 5 (NLI Stance).
It determines the stance of each candidate evidence relative to the claim
by passing them through an NLI model.

Rules:
- Executed on CPU only.
- Output is ONLY the stance (entailment/contradiction/neutral) and its probabilities.
- It is NOT a truth detector and does not output a trust_score.
- Missing evidence text is handled gracefully.
"""
from __future__ import annotations

import logging
import time
from typing import Sequence

from .schemas import Claim, EvidenceItem

log = logging.getLogger("hoax_verifier.nli")

try:
    from transformers import pipeline
    _HAS_TRANSFORMERS = True
except ImportError:
    _HAS_TRANSFORMERS = False


class StanceClassifier:
    """Classifies the stance of evidence relative to a claim using NLI."""

    def __init__(self, model_name: str = "MoritzLaurer/mDeBERTa-v3-base-xnli-multilingual-nli-2mil7"):
        if not _HAS_TRANSFORMERS:
            raise ImportError("transformers is required for NLI. Install with `pip install .[nlp]`.")
        
        self.model_name = model_name
        self.device = -1  # Explicitly constrained to CPU
        self._pipeline = None

    def _load_model(self) -> None:
        if self._pipeline is None:
            log.info("Loading NLI model %s on CPU...", self.model_name)
            t0 = time.time()
            # `top_k=None` returns probabilities for all classes (entailment, neutral, contradiction)
            self._pipeline = pipeline(
                "text-classification",
                model=self.model_name,
                device=self.device,
                top_k=None
            )
            log.info("NLI model loaded in %.2fs", time.time() - t0)

    def classify(self, claim: Claim, items: Sequence[EvidenceItem]) -> list[EvidenceItem]:
        """Classify stance for each item and populate nli_* fields in item.extra."""
        if not items:
            return list(items)

        self._load_model()
        assert self._pipeline is not None

        hypothesis = claim.text

        for item in items:
            # Combine title and snippet for the premise. 
            # If both are empty or just dots/whitespace, we cannot determine stance.
            raw_premise = f"{item.title} {item.snippet}"
            premise = raw_premise.strip(" .\t\n\r")
            
            if not premise:
                item.extra["nli_label"] = "unavailable"
                item.extra["nli_entailment_score"] = 0.0
                item.extra["nli_contradiction_score"] = 0.0
                item.extra["nli_neutral_score"] = 0.0
                continue

            try:
                # Format required by transformers for NLI pairs
                result = self._pipeline({"text": raw_premise, "text_pair": hypothesis})
                
                # result is a list of dicts like: [{'label': 'entailment', 'score': 0.99}, ...]
                scores = {pred["label"]: pred["score"] for pred in result}
                
                # The label with the highest score is the primary label
                primary_label = max(scores.items(), key=lambda x: x[1])[0]

                item.extra["nli_label"] = primary_label
                item.extra["nli_entailment_score"] = scores.get("entailment", 0.0)
                item.extra["nli_contradiction_score"] = scores.get("contradiction", 0.0)
                item.extra["nli_neutral_score"] = scores.get("neutral", 0.0)
            except Exception as e:
                log.error(f"NLI classification failed for item {item.url}: {e}")
                item.extra["nli_label"] = "error"
                item.extra["nli_entailment_score"] = 0.0
                item.extra["nli_contradiction_score"] = 0.0
                item.extra["nli_neutral_score"] = 0.0

        return list(items)
