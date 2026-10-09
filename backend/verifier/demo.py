import asyncio
from hoax_verifier.schemas import Claim
from hoax_verifier.evidence import EvidenceRetriever
from hoax_verifier.relevance import SemanticRanker
from hoax_verifier.nli import StanceClassifier
from hoax_verifier.aggregation import EvidenceAggregator

async def main():
    # 1. Tentukan klaim yang ingin diuji
    claim_text = "Bawang putih dapat menyembuhkan COVID-19."
    print(f"Menguji Klaim: '{claim_text}'\n")
    claim = Claim(text=claim_text)

    # 2. STEP 3: Evidence Retrieval (Cari kandidat berita)
    print("[1/4] Mencari evidence dari berbagai sumber (RSS, GDELT)...")
    async with EvidenceRetriever.default() as retriever:
        retrieval_result = await retriever.retrieve(claim)
    print(f"-> Ditemukan {len(retrieval_result.items)} kandidat dari {len(retrieval_result.provider_results)} provider.")

    if not retrieval_result.items:
        print("-> Tidak ada evidence yang ditemukan. Proses berhenti.")
        return

    # 3. STEP 4: Semantic Relevance (Ranking kandidat)
    print("\n[2/4] Menghitung relevansi semantik (CPU Embedding)...")
    ranker = SemanticRanker()
    ranked_items = ranker.rank(claim, retrieval_result.items)
    
    # Ambil top 10 teratas untuk dihemat (agar NLI tidak perlu cek artikel irrelevan)
    top_items = ranked_items[:10]

    # 4. STEP 5: NLI Stance (Mencari stance tiap artikel)
    print("\n[3/4] Menganalisis stance/posisi evidence (mDeBERTa di CPU)...")
    nli = StanceClassifier()
    analyzed_items = nli.classify(claim, top_items)

    # 5. STEP 6: Source Reliability & Aggregation
    print("\n[4/4] Mengagregasi evidence dan bobot sumber...")
    aggregator = EvidenceAggregator(relevance_threshold=0.2)
    final_result = aggregator.aggregate(analyzed_items)

    # 6. Tampilkan Hasil
    print("\n==================================================")
    print("HASIL AGREGASI (BUKAN KESIMPULAN FINAL HOAKS/TIDAK)")
    print("==================================================")
    print(f"Coverage Level      : {final_result.evidence_coverage} ({final_result.independent_evidence_count} klaster independen)")
    print(f"Support Score       : {final_result.support_score:.2f}")
    print(f"Contradiction Score : {final_result.contradiction_score:.2f}")
    print(f"Conflicting Evidence: {final_result.conflicting_evidence}\n")

    print("Top Supporting Evidence:")
    for it in final_result.supporting_evidence[:2]:
        print(f"  - [{it.extra.get('source_reliability_tier')}] {it.domain}: {it.title}")

    print("\nTop Contradicting Evidence:")
    for it in final_result.contradicting_evidence[:2]:
        print(f"  - [{it.extra.get('source_reliability_tier')}] {it.domain}: {it.title}")

    print("\nTop Neutral Evidence:")
    for it in final_result.neutral_evidence[:2]:
        print(f"  - [{it.extra.get('source_reliability_tier')}] {it.domain}: {it.title}")


if __name__ == "__main__":
    asyncio.run(main())
