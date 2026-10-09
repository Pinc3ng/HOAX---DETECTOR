"""Normalisation helpers: URLs, domains, HTML, titles, dates."""
from __future__ import annotations

import html
import re
import unicodedata
from datetime import datetime, timezone
from email.utils import parsedate_to_datetime
from html.parser import HTMLParser
from time import struct_time
from urllib.parse import parse_qsl, urlencode, urlsplit, urlunsplit

_TRACKING_PARAMS = {"fbclid", "gclid", "igshid", "mc_cid", "mc_eid", "ref", "ref_src", "source", "amp", "_ga"}
_WS = re.compile(r"\s+")
_NON_ALNUM = re.compile(r"[^0-9a-z\s]+")


class _TextExtractor(HTMLParser):
    def __init__(self) -> None:
        super().__init__(convert_charrefs=True)
        self.parts: list[str] = []
        self._skip = 0

    def handle_starttag(self, tag, attrs):
        if tag in ("script", "style"):
            self._skip += 1

    def handle_endtag(self, tag):
        if tag in ("script", "style") and self._skip:
            self._skip -= 1

    def handle_data(self, data):
        if not self._skip:
            self.parts.append(data)


def strip_html(value: str | None) -> str:
    """Remove tags/scripts, unescape entities, collapse whitespace."""
    if not value:
        return ""
    parser = _TextExtractor()
    try:
        parser.feed(value)
        parser.close()
        text = " ".join(parser.parts)
    except Exception:  # malformed markup: fall back to a crude strip
        text = re.sub(r"<[^>]+>", " ", value)
    return _WS.sub(" ", html.unescape(text)).strip()


def truncate(value: str, limit: int) -> str:
    if len(value) <= limit:
        return value
    cut = value[:limit].rsplit(" ", 1)[0] or value[:limit]
    return cut.rstrip(" ,.;:") + "…"


def canonical_url(url: str | None) -> str | None:
    """Normalised http(s) URL used for deduplication. Returns None for unusable URLs."""
    if not url:
        return None
    try:
        parts = urlsplit(url.strip())
    except ValueError:
        return None
    if parts.scheme.lower() not in ("http", "https") or not parts.hostname:
        return None
    host = parts.hostname.lower()
    port = parts.port
    netloc = host if port in (None, 80, 443) else f"{host}:{port}"
    query = [
        (k, v) for k, v in parse_qsl(parts.query, keep_blank_values=True)
        if not k.lower().startswith("utm_") and k.lower() not in _TRACKING_PARAMS
    ]
    path = parts.path or "/"
    if len(path) > 1:
        path = path.rstrip("/") or "/"
    return urlunsplit((parts.scheme.lower(), netloc, path, urlencode(query), ""))


def domain_of(url: str | None) -> str:
    """Hostname without a leading ``www.`` (reliability registry matches by suffix later)."""
    if not url:
        return ""
    try:
        host = urlsplit(url.strip()).hostname or ""
    except ValueError:
        return ""
    host = host.lower()
    return host[4:] if host.startswith("www.") else host


def normalize_title(title: str | None) -> str:
    t = unicodedata.normalize("NFKC", title or "").lower()
    return _WS.sub(" ", _NON_ALNUM.sub(" ", t)).strip()


def clean_title(title: str | None) -> str:
    return _WS.sub(" ", unicodedata.normalize("NFKC", strip_html(title))).strip()


def _as_utc(dt: datetime) -> datetime:
    return dt.replace(tzinfo=timezone.utc) if dt.tzinfo is None else dt.astimezone(timezone.utc)


def parse_datetime(value) -> datetime | None:
    """Best-effort parse of struct_time / datetime / ISO-8601 / RFC-822 / GDELT (``20260101T120000Z``)."""
    if value is None or value == "":
        return None
    if isinstance(value, datetime):
        return _as_utc(value)
    if isinstance(value, struct_time):
        try:
            return datetime(*value[:6], tzinfo=timezone.utc)
        except (TypeError, ValueError):
            return None
    s = str(value).strip()
    for fmt in ("%Y%m%dT%H%M%SZ", "%Y%m%d%H%M%S"):
        try:
            return datetime.strptime(s, fmt).replace(tzinfo=timezone.utc)
        except ValueError:
            pass
    try:
        return _as_utc(datetime.fromisoformat(s.replace("Z", "+00:00")))
    except ValueError:
        pass
    try:
        return _as_utc(parsedate_to_datetime(s))
    except (TypeError, ValueError, IndexError):
        return None
