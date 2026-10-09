from hoax_verifier.evidence.query import build_query, stem_variants, tokenize


def test_keywords_from_claim():
    q = build_query("Bawang putih dapat menyembuhkan COVID-19.")
    assert q.terms == ["bawang", "putih", "menyembuhkan", "covid"]
    assert q.min_hits() == 2


def test_covid_aliases_collapse_into_one_token():
    toks = tokenize("Virus Corona dan covid 19 dan Covid-19 dan SARS-CoV-2")
    assert toks.count("covid") == 4


def test_stem_variants_recover_root_for_recall():
    assert "sembuh" in stem_variants("menyembuhkan")
    assert "sembuh" in stem_variants("sembuhkan")
    assert "menyembuhkan" in stem_variants("menyembuhkan")  # original is always kept


def test_negation_is_dropped_so_both_sides_are_retrieved():
    assert build_query("Vaksin tidak aman").terms == ["vaksin", "aman"]


def test_claim_with_only_stopwords_has_no_keywords():
    q = build_query("yang dan di ke")
    assert q.keywords == ()
    assert q.min_hits() == 0


def test_duplicate_keywords_removed_and_short_tokens_ignored():
    assert build_query("garam garam di ke 19").terms == ["garam"]


def test_search_terms_limit_keeps_original_order():
    q = build_query("pemerintah menaikkan harga bahan bakar minyak subsidi nasional")
    picked = q.search_terms(4)
    assert len(picked) == 4
    assert picked == [t for t in q.terms if t in picked]


def test_min_hits_scales_with_keyword_count():
    def hits(text):
        return build_query(text).min_hits()
    assert hits("garam") == 1
    assert hits("garam laut") == 2
    assert hits("garam laut dapur") == 2
    assert hits("garam laut dapur halus kasar") == 3
