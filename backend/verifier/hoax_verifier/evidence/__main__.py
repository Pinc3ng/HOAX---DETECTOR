"""Live smoke test:  python -m hoax_verifier.evidence "Bawang putih dapat menyembuhkan COVID-19."

Hits the real public endpoints once (respecting throttle/cache). Use ``--json`` for the full result.
"""
from __future__ import annotations

import argparse
import asyncio
import json
import sys

from .pipeline import EvidenceRetriever


async def _main(claim: str, as_json: bool) -> int:
    async with EvidenceRetriever.default() as retriever:
        result = await retriever.retrieve(claim)
    if as_json:
        print(json.dumps(result.to_dict(), ensure_ascii=False, indent=2))
        return 0
    print(f"Claim            : {result.claim.text}")
    print(f"Keywords         : {', '.join(result.query['keywords'])} (min_hits={result.query['min_hits']})")
    print(f"evidence_status  : {result.evidence_status.value}")
    print(f"retrieval_health : {result.retrieval_health.value}")
    print("Providers:")
    for p in result.provider_results:
        extra = f" error={p.error}" if p.error else ""
        print(f"  - {p.provider:<9} {p.status.value:<13} items={len(p.items):<3} {p.latency_ms:>5} ms"
              f"{' (cache)' if p.from_cache else ''}{extra}")
    print(f"Candidates: {len(result.items)}  (dropped_invalid={result.dropped_invalid}, "
          f"duplicates_merged={result.duplicates_merged})")
    for i, it in enumerate(result.items[:15], 1):
        when = it.published_at.date().isoformat() if it.published_at else "n/a"
        print(f"  {i:>2}. [{it.provider}/{it.domain}] {when}  {it.title[:100]}")
        print(f"      {it.url}")
    for n in result.notes:
        print(f"NOTE: {n}")
    return 0


def main() -> None:
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("claim")
    ap.add_argument("--json", action="store_true")
    args = ap.parse_args()
    sys.exit(asyncio.run(_main(args.claim, args.json)))


if __name__ == "__main__":
    main()
