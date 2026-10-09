from hoax_ml.cleaning import build_text, clean_text


def test_clean_basic_lowercases_and_collapses_whitespace():
    assert clean_text('  "Jakarta,   BERITA   Hari Ini"  ') == "jakarta, berita hari ini"


def test_urls_always_become_token():
    out = clean_text("Baca di https://turnbackhoax.id/abc dan www.kompas.com/x sekarang")
    assert "http" not in out and "www" not in out
    assert out.count("urltoken") == 2


def test_markers_are_kept_by_default_never_removed_silently():
    out = clean_text("Ini adalah HOAKS menurut Kompas.com")
    assert "hoaks" in out and "kompas" in out


def test_markers_removed_only_when_explicitly_requested():
    out = clean_text("Ini adalah HOAKS menurut Kompas.com [SALAH] fakta", neutralize_markers=True)
    for w in ("hoaks", "kompas", "salah", "fakta"):
        assert w not in out


def test_forward_phrases_are_not_neutralised():
    # genuine linguistic cue of chain messages: measured in leakage report, not removed
    assert "sebarkan" in clean_text("Tolong SEBARKAN ke semua grup", neutralize_markers=True)


def test_salah_satu_is_not_a_verdict_marker():
    assert "salah satu" in clean_text("Ia salah satu menteri", neutralize_markers=True)


def test_dateline_strip_is_opt_in():
    assert clean_text("JAKARTA, Presiden hadir").startswith("jakarta")
    assert clean_text("JAKARTA, Presiden hadir", strip_dateline=True).startswith("presiden")


def test_build_text_combines_title_and_content():
    assert build_text("Judul", "Isi") == "Judul. Isi"
    assert build_text("", "Isi") == "Isi"
    assert build_text(None, None) == ""
