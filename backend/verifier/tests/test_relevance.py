from __future__ import annotations

from datetime import datetime, timezone
import pytest

from hoax_verifier.schemas import Claim, EvidenceItem
from hoax_verifier.relevance import SemanticRanker

NOW = datetime(2026, 10, 6, tzinfo=timezone.utc)


def item(title="Judul berita yang cukup panjang", url="https://a.example/x", snippet="", **kw) -> EvidenceItem:
    return EvidenceItem(title=title, url=url, domain=kw.pop("domain", "a.example"), provider="mock",
                        retrieval_time=NOW, snippet=snippet, **kw)


def test_semantic_ranker_relevance_sorting():
    ranker = SemanticRanker()
    claim = Claim(text="Bawang putih dapat menyembuhkan COVID-19.")
    
    # Highly relevant article (even if the claim is false, the article talks about it)
    item_relevant = item(
        title="Mitos atau Fakta: Bawang Putih Bisa Sembuhkan COVID-19?",
        snippet="Beredar kabar bahwa mengonsumsi bawang putih mentah dapat menyembuhkan virus corona atau COVID-19."
    )
    # Somewhat relevant (about COVID but not garlic)
    item_semi = item(
        title="Update Kasus COVID-19 Hari Ini",
        snippet="Jumlah pasien yang sembuh dari COVID-19 terus bertambah seiring berjalannya waktu."
    )
    # Completely irrelevant article
    item_irrelevant = item(
        title="Hasil Pertandingan Sepak Bola Liga Champions",
        snippet="Tim jagoan berhasil mencetak tiga gol pada pertandingan semalam melawan rival sengitnya."
    )

    items = [item_irrelevant, item_relevant, item_semi]
    
    ranked = ranker.rank(claim, items)
    
    # Check that they are sorted by relevance (highest first)
    assert len(ranked) == 3
    assert ranked[0] == item_relevant
    assert ranked[1] == item_semi
    assert ranked[2] == item_irrelevant
    
    # Check that the semantic_relevance score is populated
    assert "semantic_relevance" in ranked[0].extra
    assert isinstance(ranked[0].extra["semantic_relevance"], float)
    
    # Ensure scores are strictly decreasing
    assert ranked[0].extra["semantic_relevance"] > ranked[1].extra["semantic_relevance"]
    assert ranked[1].extra["semantic_relevance"] > ranked[2].extra["semantic_relevance"]

def test_semantic_ranker_empty_items():
    ranker = SemanticRanker()
    claim = Claim(text="Bawang putih dapat menyembuhkan COVID-19.")
    assert ranker.rank(claim, []) == []
