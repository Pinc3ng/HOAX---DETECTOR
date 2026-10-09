from datetime import datetime, timezone

from hoax_verifier.evidence.text import (canonical_url, clean_title, domain_of, normalize_title,
                                         parse_datetime, strip_html, truncate)


def test_canonical_url_strips_tracking_fragment_default_port_and_trailing_slash():
    assert (canonical_url("HTTPS://WWW.Example.com:443/a/b/?utm_source=x&id=3&fbclid=z#frag")
            == "https://www.example.com/a/b?id=3")


def test_canonical_url_rejects_unusable_urls():
    for bad in ("", None, "javascript:alert(1)", "ftp://x.com/a", "not a url", "http://"):
        assert canonical_url(bad) is None


def test_domain_of_drops_www():
    assert domain_of("https://www.kompas.com/x") == "kompas.com"
    assert domain_of("https://news.detik.com/x") == "news.detik.com"
    assert domain_of(None) == ""


def test_strip_html_removes_tags_scripts_and_entities():
    assert strip_html("<p>Halo&nbsp;<b>dunia</b><script>evil()</script> &amp; semua</p>") == "Halo dunia & semua"


def test_truncate_on_word_boundary():
    out = truncate("satu dua tiga empat lima", 12)
    assert out.endswith("…") and len(out) <= 13


def test_normalize_title_and_clean_title():
    assert normalize_title("[SALAH]  Bawang putih!") == "salah bawang putih"
    assert clean_title("  <b>Judul</b>   berita ") == "Judul berita"


def test_parse_datetime_formats_become_utc():
    assert parse_datetime("20260110T031500Z") == datetime(2026, 1, 10, 3, 15, tzinfo=timezone.utc)
    assert parse_datetime("Tue, 06 Oct 2026 10:00:00 +0700") == datetime(2026, 10, 6, 3, 0, tzinfo=timezone.utc)
    assert parse_datetime("2020-03-15") == datetime(2020, 3, 15, tzinfo=timezone.utc)
    assert parse_datetime("2020-03-12T08:00:00Z") == datetime(2020, 3, 12, 8, tzinfo=timezone.utc)
    assert parse_datetime("garbage") is None
    assert parse_datetime(None) is None
