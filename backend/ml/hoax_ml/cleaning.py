"""Text cleaning (preprocessing version ``clean_v1``).

Design rules
------------
* ``clean_v1`` is deliberately light: Unicode normalisation, wrapping-quote
  removal, URL -> ``urltoken``, lower-casing, whitespace collapsing. No stemming
  or stop-word removal (not needed for this baseline; can be ablated later).
* URLs are *always* replaced by a token so the model cannot memorise domains.
  The leakage report measures URL presence on the RAW text, so this is not hidden.
* Neutralising label-shortcut markers is an EXPLICIT, opt-in switch. It is never
  applied silently; the leakage report quantifies its effect.
"""
from __future__ import annotations

import re
import unicodedata

PREPROCESSING_VERSION = "clean_v1"

_FLAGS = re.IGNORECASE

# Groups of markers that can act as label shortcuts.
MARKER_GROUPS: dict[str, re.Pattern] = {
    # Explicit verdict language (fact-check boilerplate leaking into the body).
    "verdict_words": re.compile(
        r"\bho(?:ax|aks)\b|\bfake\b|\bpalsu\b|\bsalah\b(?!\s+satu)|\bkeliru\b|\bmenyesatkan\b"
        r"|\bdisinformasi\b|\bmisinformasi\b|\bfakta\b|\bklarifikasi\b|\bpenjelasan\b"
        r"|\bkesimpulan\b|\bnarasi\b",
        _FLAGS,
    ),
    # Fact-check organisations / sites.
    "factcheck_sites": re.compile(
        r"turnbackhoax|mafindo|saber\s*hoaks|klinik\s*hoaks|cekfakta|kominfo|komdigi|stophoax",
        _FLAGS,
    ),
    # Outlet signatures (bare common words such as "antara"/"detik" are excluded on purpose).
    "outlets": re.compile(
        r"kompas(?:\.com|iana|\.id)?|detik(?:\.com|com|news|finance|health|inet|sport|food|travel|hot|oto)"
        r"|cnn(?:\s?indonesia)?|cnbc|liputan6|tribun\w*|okezone|antaranews|vivanews|viva\.co\.id"
        r"|merdeka\.com|tempo\.co|republika(?:\.co\.id)?|suara\.com|kumparan|jawapos|sindonews"
        r"|\bbbc\b|jpnn|beritasatu|inilah\.com|tirto|metrotvnews|kontan|bisnis\.com",
        _FLAGS,
    ),
    "bracket_tags": re.compile(
        r"\[\s*(?:hoax|hoaks|salah|fakta|disinformasi|misinformasi|klarifikasi|benar|valid)[^\]]{0,40}\]",
        _FLAGS,
    ),
    # Genuine linguistic cues of chain messages: measured, but NOT neutralised.
    "forward_phrases": re.compile(
        r"\bsebarkan\b|\bshare\b|\bviral\b|\bditeruskan\b|\bbroadcast\b|\bberantai\b|\bberedar\b",
        _FLAGS,
    ),
    "url": re.compile(r"https?://\S+|www\.\S+", _FLAGS),
}

# Only groups that are *label/source shortcuts* are neutralised when the switch is on.
NEUTRALIZE_GROUPS = ("verdict_words", "factcheck_sites", "outlets", "bracket_tags")

URL_RE = MARKER_GROUPS["url"]
DATELINE_RE = re.compile(
    r"^\s*[\"“]?\s*(?:[A-Z][\w.]*(?:\s[A-Z][\w.]*){0,2})(?:\.com|\.co\.id)?\s*(?:,|–|—|-|:)\s+"
)
_WS_RE = re.compile(r"\s+")
_QUOTES = "\"'“”‘’"


def build_text(title: str | None, content: str | None) -> str:
    """Combine title and content into the raw text used by the model."""
    title = (title or "").strip()
    content = (content or "").strip()
    if title and content:
        return f"{title}. {content}"
    return title or content


def clean_text(text: str, neutralize_markers: bool = False, strip_dateline: bool = False) -> str:
    t = unicodedata.normalize("NFKC", "" if text is None else str(text))
    if strip_dateline:
        t = DATELINE_RE.sub("", t, count=1)
    t = t.strip().strip(_QUOTES).strip()
    t = URL_RE.sub(" urltoken ", t)
    if neutralize_markers:
        for name in NEUTRALIZE_GROUPS:
            t = MARKER_GROUPS[name].sub(" ", t)
    t = t.lower()
    return _WS_RE.sub(" ", t).strip()
