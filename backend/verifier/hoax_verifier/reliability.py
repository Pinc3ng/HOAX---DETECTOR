import csv
import logging
from pathlib import Path

from .schemas import ReliabilityTier, SourceReliability

log = logging.getLogger("hoax_verifier.reliability")

class SourceReliabilityRegistry:
    def __init__(self, csv_path: str | Path | None = None):
        if csv_path is None:
            csv_path = Path(__file__).parent / "data" / "source_reliability.csv"
        
        self.csv_path = Path(csv_path)
        self.registry: dict[str, SourceReliability] = {}
        self._load()

    def _normalize_domain(self, domain: str) -> str:
        """Strip www. and lowercase the domain for consistent lookup."""
        d = domain.lower().strip()
        if d.startswith("www."):
            d = d[4:]
        return d

    def _load(self):
        if not self.csv_path.exists():
            log.warning(f"Reliability CSV not found at {self.csv_path}. Using empty registry.")
            return

        with open(self.csv_path, "r", encoding="utf-8") as f:
            reader = csv.DictReader(f)
            for row in reader:
                domain = self._normalize_domain(row.get("domain", ""))
                tier_str = row.get("tier", "").upper()
                desc = row.get("description", "")
                
                if not domain:
                    continue
                
                try:
                    tier = ReliabilityTier[tier_str]
                except KeyError:
                    log.warning(f"Invalid tier {tier_str} for domain {domain}, defaulting to UNKNOWN")
                    tier = ReliabilityTier.UNKNOWN
                
                self.registry[domain] = SourceReliability(domain=domain, tier=tier, description=desc)

    def get_reliability(self, domain: str) -> SourceReliability:
        norm_domain = self._normalize_domain(domain)
        if norm_domain in self.registry:
            return self.registry[norm_domain]
        
        # Default fallback
        return SourceReliability(
            domain=norm_domain, 
            tier=ReliabilityTier.UNKNOWN, 
            description="Domain not found in reliability registry."
        )
