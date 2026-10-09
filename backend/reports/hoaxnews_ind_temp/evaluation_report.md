# Evaluation report — `hoaxnews_ind_temp`

> **TEMPORARY DATASET — pipeline smoke test only.** Numbers below validate that the pipeline works; they say nothing about real-world performance. The final model must be retrained on the audited BRIN/MAFINDO dataset.

- Model version: `hoaxnews_ind_temp-20261003-eae3246a`
- Dataset: `hoaxnews_ind_temp` v`kornwtp-250` — 250 raw rows, 250 used
- Signal: `ml_risk` = P(dataset label = 1) — a **content/linguistic risk signal**, not a truth verdict
- Seed: 42 · preprocessing `clean_v1` · char n-grams: False

## 1. Data and class balance

- Positive (1) = 91, negative (0) = 159 (imbalance 1.75:1).
- **Majority-class baseline accuracy = 0.636** — accuracy must be read against this number.
- Removed during preparation: {"removed_ids": {"unmapped_label": [], "empty": [], "conflicting_duplicates": [], "exact_duplicates": []}}
- Near-duplicate groups (cosine ≥ 0.9): 21 multi-row groups.

- **warn** `small_dataset`: Only 250 rows (< 500). Metrics will have high variance.
- **warn** `small_minority`: Minority class has 91 rows (< 100).
- **warn** `temporary_dataset`: Dataset is marked TEMPORARY: results are for pipeline testing only.

## 2. Hold-out test (single split — high variance on small data)

Train 199 / test 51 rows, stratified, near-duplicate-group aware. Threshold 0.5.

| Metric | Calibrated SVM | Majority baseline |
|---|---|---|
| Accuracy | 0.765 | 0.627 |
| Precision (positive) | 1.000 | 0.000 |
| Recall (positive) | 0.368 | 0.000 |
| F1 (positive) | 0.538 | 0.000 |
| **Macro-F1** | 0.690 | 0.386 |
| ROC-AUC | 0.719 | 0.500 |
| PR-AUC | 0.735 | 0.373 |
| Brier score ↓ | 0.192 | 0.234 |
| ECE ↓ (5 bins) | 0.052 | 0.011 |

Confusion matrix (test; rows = actual, cols = predicted):

| | pred 0 | pred 1 |
|---|---|---|
| actual 0 | 32 | 0 |
| actual 1 | 12 | 7 |

One test sample = 2.0 accuracy points. Train macro-F1 = 0.984 (gap vs test = +0.293).

## 3. Repeated grouped cross-validation (more reliable than a single split)

| Variant | Macro-F1 | F1 (pos) | Precision | Recall | ROC-AUC | PR-AUC | Brier | ECE |
|---|---|---|---|---|---|---|---|---|
| majority_baseline | 0.389 ± 0.003 | 0.000 ± 0.000 | 0.000 ± 0.000 | 0.000 ± 0.000 | 0.500 ± 0.000 | 0.364 ± 0.007 | 0.232 ± 0.002 | 0.008 ± 0.004 |
| word_tfidf_1_2 | 0.723 ± 0.057 | 0.609 ± 0.086 | 0.813 ± 0.132 | 0.503 ± 0.111 | 0.795 ± 0.044 | 0.749 ± 0.067 | 0.172 ± 0.018 | 0.089 ± 0.031 |
| word_1_2_plus_char_3_5 | 0.714 ± 0.060 | 0.600 ± 0.093 | 0.806 ± 0.156 | 0.503 ± 0.127 | 0.800 ± 0.052 | 0.752 ± 0.070 | 0.172 ± 0.019 | 0.083 ± 0.028 |

Char n-gram decision: `use_char=False` (gain -0.0086, word-only std 0.0572). Rule: use char n-grams only if macro-F1 gain >= 0.01 and > std of word-only.

## 4. Calibration (hold-out)

| Bin | n | mean predicted risk | observed positive rate |
|---|---|---|---|
| 0.0-0.2 | 9 | 0.15 | 0.111 |
| 0.2-0.4 | 25 | 0.286 | 0.28 |
| 0.4-0.6 | 13 | 0.462 | 0.538 |
| 0.6-0.8 | 3 | 0.665 | 1.0 |
| 0.8-1.0 | 1 | 0.847 | 1.0 |

Bins with few samples are unreliable; use CV Brier/ECE above as the main calibration evidence.

## 5. Leakage check

| Severity | Finding |
|---|---|
| high | 47 rows belong to 21 near-duplicate groups (cosine >= 0.9); conflicting-label groups: 4. |
| warn | 'verdict_words' appears in 26% of positive vs 48% of negative rows (Fisher p=0.000772). |
| warn | 'outlets' appears in 11% of positive vs 23% of negative rows (Fisher p=0.0184). |
| warn | Article length alone gives macro-F1=0.499 (majority baseline 0.389). |

Full details: `reports/hoaxnews_ind_temp/leakage_report.md`

## 6. Is this good enough as a baseline?

- CV macro-F1 0.723 vs majority 0.389 (gain +0.334).
- Dataset is temporary and small: **valid only as a pipeline smoke test**, not as evidence of hoax-detection quality.
- Treat any strong score together with the leakage findings above: a high score explained by markers/source style is not evidence of useful linguistic-risk detection.
- Next step: audit the BRIN/MAFINDO dataset, retrain with the same pipeline, re-run the leakage check, then tune risk thresholds on validation data.
