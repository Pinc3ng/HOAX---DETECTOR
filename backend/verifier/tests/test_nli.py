from __future__ import annotations

import pytest
from datetime import datetime, timezone

from hoax_verifier.schemas import Claim, EvidenceItem
from hoax_verifier.nli import StanceClassifier

NOW = datetime(2026, 10, 6, tzinfo=timezone.utc)

def item(title="", url="https://a.example/x", snippet="", **kw) -> EvidenceItem:
    return EvidenceItem(title=title, url=url, domain=kw.pop("domain", "a.example"), provider="mock",
                        retrieval_time=NOW, snippet=snippet, **kw)


def test_nli_stance_classification():
    classifier = StanceClassifier()
    claim = Claim(text="Bawang putih dapat menyembuhkan COVID-19.")
    
    item_supporting = item(
        title="Bawang putih mentah terbukti ampuh membunuh virus corona",
        snippet="dapat menyembuhkan COVID-19 secara total jika dikonsumsi rutin."
    )
    item_contradicting = item(
        title="WHO bantah khasiat bawang putih",
        snippet="WHO menegaskan bahwa tidak ada bukti medis yang menunjukkan bahwa makan bawang putih dapat menyembuhkan penderita COVID-19."
    )
    item_neutral = item(
        title="Resep Nasi Goreng",
        snippet="Gunakan 3 siung bawang putih dan kecap manis."
    )
    
    items = [item_supporting, item_contradicting, item_neutral]
    result = classifier.classify(claim, items)
    
    assert len(result) == 3
    
    # Check supporting
    assert result[0].extra["nli_label"] == "entailment"
    assert result[0].extra["nli_entailment_score"] > 0.5
    
    # Check contradicting
    assert result[1].extra["nli_label"] == "contradiction"
    assert result[1].extra["nli_contradiction_score"] > 0.5
    
    # Check neutral
    assert result[2].extra["nli_label"] == "neutral"
    assert result[2].extra["nli_neutral_score"] > 0.5


def test_nli_empty_or_short_evidence():
    classifier = StanceClassifier()
    claim = Claim(text="Bawang putih dapat menyembuhkan COVID-19.")
    
    item_empty = item(title="", snippet="")
    item_dot = item(title=".", snippet=".")
    
    items = [item_empty, item_dot]
    result = classifier.classify(claim, items)
    
    for r in result:
        assert r.extra["nli_label"] == "unavailable"
        assert r.extra["nli_entailment_score"] == 0.0
        assert r.extra["nli_contradiction_score"] == 0.0
        assert r.extra["nli_neutral_score"] == 0.0


def test_nli_empty_claim():
    classifier = StanceClassifier()
    claim = Claim(text="")
    
    item_valid = item(title="Valid Title", snippet="Valid snippet")
    
    # It should not crash, huggingface pipeline will likely return an error or some default
    # If the pipeline returns an error, our try-except handles it. 
    # If it predicts something anyway, we just ensure it doesn't crash.
    result = classifier.classify(claim, [item_valid])
    assert result[0].extra["nli_label"] in ("entailment", "contradiction", "neutral", "error")
