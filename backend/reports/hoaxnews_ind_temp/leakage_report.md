# Leakage report — `hoaxnews_ind_temp`

> **TEMPORARY DATASET — pipeline testing only. Not a final result.**

## Findings

| Severity | Code | Finding | Impact |
|---|---|---|---|
| high | `near_duplicates` | 47 rows belong to 21 near-duplicate groups (cosine >= 0.9); conflicting-label groups: 4. | Handled by group-aware splits so near-duplicates never straddle train/test. |
| warn | `marker_verdict_words` | 'verdict_words' appears in 26% of positive vs 48% of negative rows (Fisher p=0.000772). | The classifier can use this as a shortcut instead of linguistic risk patterns; scores will not generalise to articles without this marker. |
| warn | `marker_outlets` | 'outlets' appears in 11% of positive vs 23% of negative rows (Fisher p=0.0184). | The classifier can use this as a shortcut instead of linguistic risk patterns; scores will not generalise to articles without this marker. |
| warn | `length_alone_predicts` | Article length alone gives macro-F1=0.499 (majority baseline 0.389). | Length is a collection artefact, not a hoax signal. |

## Shortcut probes (repeated grouped CV, macro-F1 mean ± std)

| Variant | Macro-F1 | ROC-AUC |
|---|---|---|
| majority_baseline | 0.389 ± 0.003 | 0.500 |
| full_text | 0.723 ± 0.057 | 0.795 |
| markers_neutralized | 0.723 ± 0.057 | 0.795 |
| markers_neutralized_and_dateline_stripped | 0.723 ± 0.057 | 0.795 |
| first_20_words_only | 0.558 ± 0.075 | 0.680 |
| last_20_words_only | 0.520 ± 0.084 | 0.615 |
| length_only_logreg | 0.499 ± 0.086 | - |

## Marker prevalence (share of rows containing the marker)

| Marker group | positive | negative | gap | Fisher p | severity | top matches (pos / neg) |
|---|---|---|---|---|---|---|
| verdict_words | 26% | 48% | 0.22 | 0.00077 | warn | [('klarifikasi', 11), ('salah', 10), ('fakta', 6)] / [('hoax', 61), ('klarifikasi', 27), ('salah', 22)] |
| factcheck_sites | 1% | 0% | 0.01 | 0.36 | info | [('kominfo', 1)] / [] |
| outlets | 11% | 23% | 0.12 | 0.018 | warn | [('cnn indonesia', 3), ('tribunnews', 2), ('tirto', 2)] / [('detikhealth', 7), ('cnbc', 5), ('detikcom', 5)] |
| bracket_tags | 0% | 0% | 0.00 | 1 | info | [] / [] |
| forward_phrases | 36% | 52% | 0.15 | 0.025 | warn | [('beredar', 32), ('viral', 9), ('broadcast', 4)] / [('beredar', 123), ('viral', 22), ('broadcast', 6)] |
| url | 0% | 2% | 0.02 | 0.56 | info | [] / [('http://www.amazon.com/fuchs-toothbrushes-natural-bristle-assorted/dp/b018iwrz9k/ref=sr_1_3?s=beauty&ie=utf8&qid=1455426484&sr=1-3&keywords=boar+bristle+toothbrush)', 1), ('http://www.amazon.com/dp/b0152bo0f2?psc=1.', 1), ('www.pom.go.id', 1)] |

## Single-feature style shortcuts (ROC-AUC of the feature alone)

| Feature | AUC | median pos | median neg | severity |
|---|---|---|---|---|
| word_count | 0.546 | 315 | 279 | info |
| upper_ratio | 0.541 | 0.0363 | 0.035 | info |
| digit_ratio | 0.438 | 0.00551 | 0.00688 | info |
| exclamation_count | 0.567 | 0 | 0 | info |
| quote_count | 0.464 | 6 | 10 | info |
| avg_word_len | 0.441 | 6.08 | 6.17 | info |
| starts_with_quote | 0.495 | 1 | 1 | info |
| starts_with_dateline | 0.465 | 0 | 0 | info |

## Most discriminative tokens (smoothed log-odds)

- Positive class: mengoles, kue, presiden, bitungnews, merias, salon, bela, lukis, acting, alun, kaayu, pembersih, lantai, tahukah, leher, tuntutan, tradisional, suntik, pantauan, parkir
- Negative class: martinus, arab, empat, bnn, hurairah, setyo, bohong, narkotika, jemaah, yordania, gereja, mainan, ramadhan, risiko, chu, niantic, seribu, mabes, area, bbpom
- Flagged as source/verdict markers: none

_Tokens are ranked by smoothed log-odds on the full dataset; they describe the data, not the truth of any article._

## Near-duplicates

Threshold 0.9: 21 multi-row groups (47 rows), conflicting-label groups: 4.
