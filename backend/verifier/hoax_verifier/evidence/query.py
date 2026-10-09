"""Query builder: claim -> search terms.

The query is built from the *claim only* (never from the ML score). Negation words ("tidak",
"bukan") are dropped on purpose: retrieval should surface articles on BOTH sides of a claim;
deciding which side an article takes is the NLI step's job.

The light Indonesian stemming below is used ONLY for recall when matching keywords against
feed items. It is not linguistic analysis, and precision is handled later by embedding ranking.
"""
from __future__ import annotations

import math
import re
import unicodedata
from dataclasses import dataclass

from ..schemas import Claim

STOPWORDS = frozenset("""
yang dan di ke dari untuk pada dengan ini itu adalah ialah dapat bisa akan sudah telah sedang masih
sebagai oleh atau juga karena agar bahwa ada tidak bukan tak belum jangan saya aku kami kita kamu
anda mereka ia dia nya lah kah pun lagi lebih sangat amat sekali hanya saja para suatu sebuah
seorang dalam antara setelah sebelum ketika saat jika kalau maka sehingga namun tetapi tapi tersebut
seperti yaitu yakni dll dsb dst the and of to in for is are was were
""".split())

# Different spellings of the same concept collapse into one token ("covid").
_ALIAS = re.compile(r"covid[\s\-]?19|coronavirus|korona|corona|sars[\s\-]?cov[\s\-]?2", re.IGNORECASE)
_TOKEN = re.compile(r"[a-z0-9]+")

_PREFIXES = ("meny", "meng", "mem", "men", "me", "peny", "peng", "pem", "pen", "pe", "ber", "ter", "di", "ke", "se")
_SUFFIXES = ("kan", "nya", "lah", "kah", "an", "i")
_MIN_STEM = 5  # derived stems shorter than this are discarded (too many accidental collisions)


def stem_variants(token: str) -> frozenset[str]:
    """Token plus a few crude affix-stripped forms (recall aid, intentionally over-generating)."""
    cands = {token}
    for s in _SUFFIXES:
        if token.endswith(s) and len(token) - len(s) >= _MIN_STEM:
            cands.add(token[: -len(s)])
    for c in list(cands):
        for p in _PREFIXES:
            if c.startswith(p) and len(c) - len(p) >= _MIN_STEM - 1:
                rest = c[len(p):]
                if len(rest) >= _MIN_STEM:
                    cands.add(rest)
                # nasal-assimilation restores the dropped initial consonant: meny+sembuh -> sembuh
                restored = {"meny": "s", "peny": "s", "mem": "p", "pem": "p", "men": "t", "pen": "t",
                            "meng": "k", "peng": "k"}.get(p)
                if restored and len(rest) + 1 >= _MIN_STEM:
                    cands.add(restored + rest)
    return frozenset(cands)


def tokenize(text: str) -> list[str]:
    """Lower-cased alphanumeric tokens, with COVID/corona aliases collapsed."""
    t = unicodedata.normalize("NFKC", text or "").lower()
    t = _ALIAS.sub(" covid ", t)
    return _TOKEN.findall(t)


def token_forms(text: str) -> frozenset[str]:
    """All tokens of ``text`` plus their stem variants (used to match feed items)."""
    forms: set[str] = set()
    for tok in tokenize(text):
        forms.update(stem_variants(tok))
    return frozenset(forms)


@dataclass(frozen=True)
class Keyword:
    term: str
    forms: frozenset[str]


@dataclass(frozen=True)
class Query:
    claim_text: str
    keywords: tuple[Keyword, ...]

    @property
    def terms(self) -> list[str]:
        return [k.term for k in self.keywords]

    def min_hits(self) -> int:
        """How many keywords an item must contain to count as a candidate (recall-oriented)."""
        n = len(self.keywords)
        if n == 0:
            return 0
        if n <= 2:
            return n
        return max(2, math.ceil(n / 2))

    def search_terms(self, limit: int = 4) -> list[str]:
        """Up to ``limit`` terms for AND-style search APIs: longest terms first, original order kept."""
        if len(self.keywords) <= limit:
            return self.terms
        keep = sorted(range(len(self.keywords)), key=lambda i: (-len(self.keywords[i].term), i))[:limit]
        return [self.keywords[i].term for i in sorted(keep)]

    def keyword_query(self, limit: int = 4) -> str:
        return " ".join(self.search_terms(limit))

    def to_dict(self) -> dict:
        return {"claim_text": self.claim_text, "keywords": self.terms, "min_hits": self.min_hits()}


def build_query(claim: Claim | str) -> Query:
    text = claim.text if isinstance(claim, Claim) else str(claim)
    seen: set[str] = set()
    keywords: list[Keyword] = []
    for tok in tokenize(text):
        if len(tok) < 3 or tok in STOPWORDS or tok in seen:
            continue
        seen.add(tok)
        keywords.append(Keyword(tok, stem_variants(tok)))
    return Query(claim_text=" ".join(text.split()), keywords=tuple(keywords))
