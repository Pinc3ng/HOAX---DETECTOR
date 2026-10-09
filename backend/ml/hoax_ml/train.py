"""Training orchestration: validate -> prepare -> group -> split -> ablate -> fit -> evaluate -> persist."""
from __future__ import annotations

import json
import platform
import subprocess
from dataclasses import replace
from datetime import datetime, timezone
from pathlib import Path

import joblib
import numpy as np
import pandas as pd
import scipy
import sklearn

from . import __version__
from .cleaning import PREPROCESSING_VERSION
from .data import file_sha256, load_standardized, prepare, validate
from .evaluate import (
    clean_many,
    compute_metrics,
    holdout_split,
    reliability_table,
    repeated_grouped_cv,
)
from .groups import group_summary, near_duplicate_groups
from .model import MODEL_TYPE, ModelParams, fit_model, predict_risk_proba
from .schema import MODELS_DIR, DatasetConfig

CHAR_NGRAM_MIN_GAIN = 0.01   # use char n-grams only if CV macro-F1 improves by >= this ...
                             # ... AND by more than one std of the word-only variant


def _git_info() -> dict:
    def run(*a):
        return subprocess.run(["git", *a], capture_output=True, text=True, timeout=10).stdout.strip()

    try:
        return {"commit": run("rev-parse", "HEAD") or None, "dirty": bool(run("status", "--porcelain"))}
    except Exception:  # noqa: BLE001
        return {"commit": None, "dirty": None}


def _dump(path: Path, obj) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2, ensure_ascii=False, default=str), encoding="utf-8")


def _fmt(m: dict | None) -> str:
    return "-" if not m else f"{m['mean']:.3f} ± {m['std']:.3f}"


def _single(m: dict, k: str) -> str:
    return "-" if m.get(k) is None else f"{m[k]:.3f}"


def run_ablation(raw, y, groups, base: ModelParams, mcfg: dict) -> tuple[dict, ModelParams, dict]:
    kw = dict(n_splits=mcfg.get("cv_splits", 5), n_repeats=mcfg.get("cv_repeats", 3), seed=mcfg.get("seed", 42))
    res = {
        "majority_baseline": repeated_grouped_cv(raw, y, groups, base, kind="majority", **kw),
        "word_tfidf_1_2": repeated_grouped_cv(raw, y, groups, base, **kw),
        "word_1_2_plus_char_3_5": repeated_grouped_cv(raw, y, groups, replace(base, use_char=True), **kw),
    }
    w, c = res["word_tfidf_1_2"]["macro_f1"], res["word_1_2_plus_char_3_5"]["macro_f1"]
    gain = c["mean"] - w["mean"]
    use_char = gain >= CHAR_NGRAM_MIN_GAIN and gain > w["std"]
    decision = {
        "rule": f"use char n-grams only if macro-F1 gain >= {CHAR_NGRAM_MIN_GAIN} and > std of word-only",
        "gain": round(gain, 4),
        "word_only_std": round(w["std"], 4),
        "use_char": bool(use_char),
    }
    return res, replace(base, use_char=bool(use_char)), decision


def train(cfg: DatasetConfig, fit_on: str = "train", skip_ablation: bool = False) -> dict:
    mcfg = cfg.model
    seed = int(mcfg.get("seed", 42))
    test_size = float(mcfg.get("test_size", 0.2))
    near_thr = float(mcfg.get("near_duplicate_threshold", 0.9))
    out_reports = cfg.reports_dir
    out_reports.mkdir(parents=True, exist_ok=True)

    # 1-2. load, validate, prepare (all removals logged)
    std = load_standardized(cfg)
    val = validate(std, cfg)
    df, plog = prepare(std, cfg)
    val["prepare_log"] = plog.as_dict()
    _dump(out_reports / "validation_report.json", val)
    if df["label"].nunique() < 2 or len(df) < 20:
        raise SystemExit("Not enough usable rows / classes after preparation. See validation_report.json")

    raw, y = df["raw_text"].tolist(), df["label"].to_numpy()

    # 3. near-duplicate groups -> leakage-safe splits
    groups = near_duplicate_groups(raw, near_thr)
    gsum = group_summary(groups, y)

    # 4. hold-out split
    tr, te = holdout_split(y, groups, test_size, seed)
    assert not set(groups[tr]) & set(groups[te]), "near-duplicate group straddles train/test"

    # 5. ablation (word vs word+char, majority baseline) on the WHOLE dataset via grouped CV
    base = ModelParams()
    if skip_ablation:
        ablation, params, decision = {}, base, {"skipped": True}
    else:
        ablation, params, decision = run_ablation(raw, y, groups, base, mcfg)

    # 6. fit on train split, evaluate on hold-out test
    texts = clean_many(raw, params)
    vec, clf = fit_model([texts[i] for i in tr], y[tr], groups[tr], params, seed)
    p_tr = predict_risk_proba(vec, clf, [texts[i] for i in tr])
    p_te = predict_risk_proba(vec, clf, [texts[i] for i in te])
    holdout = {
        "test": compute_metrics(y[te], p_te),
        "train": compute_metrics(y[tr], p_tr),
        "majority_baseline_test": compute_metrics(y[te], np.full(len(te), float(y[tr].mean()))),
        "reliability_test": reliability_table(y[te], p_te),
        "n_train": int(len(tr)),
        "n_test": int(len(te)),
    }
    cv_final = ablation.get("word_1_2_plus_char_3_5" if params.use_char else "word_tfidf_1_2")

    # 7. optionally refit on all data (then hold-out metrics no longer describe the artifact)
    if fit_on == "all":
        vec, clf = fit_model(texts, y, groups, params, seed)

    # 8. persist artifact + metadata
    out_dir = MODELS_DIR / cfg.dataset_id
    out_dir.mkdir(parents=True, exist_ok=True)
    vec_path, clf_path = out_dir / "tfidf_vectorizer.joblib", out_dir / "calibrated_svm.joblib"
    joblib.dump(vec, vec_path)
    joblib.dump(clf, clf_path)
    trained_at = datetime.now(timezone.utc)
    clf_sha = file_sha256(clf_path)
    model_version = f"{cfg.dataset_id}-{trained_at:%Y%m%d}-{clf_sha[:8]}"

    metadata = {
        "model_version": model_version,
        "signal": "ml_risk",
        "purpose": "Linguistic/content-risk signal only. NOT a truth detector; never decides the final status.",
        "model_reliability": "baseline",
        "model_type": MODEL_TYPE,
        "dataset": {
            "dataset_id": cfg.dataset_id,
            "version": cfg.version,
            "status": cfg.status,
            "is_temporary": cfg.is_temporary,
            "source": cfg.source,
            "raw_file": cfg.raw_path.name,
            "raw_file_sha256": file_sha256(cfg.raw_path),
            "config_file": cfg.config_path.name if cfg.config_path else None,
            "label_map": cfg.label_map,
            "positive_label_meaning": "dataset label flagged as problematic (1); not an assertion of falsity",
            "rows_raw": plog.rows_in,
            "rows_used": int(len(df)),
            "class_counts": {"1": int((y == 1).sum()), "0": int((y == 0).sum())},
            "prepare_log": {k: v for k, v in plog.as_dict().items() if k != "removed_ids"},
            "near_duplicate_groups": {"threshold": near_thr, **gsum},
        },
        "preprocessing": {
            "version": PREPROCESSING_VERSION,
            "neutralize_markers": params.neutralize_markers,
            "strip_dateline": params.strip_dateline,
        },
        "params": params.to_dict(),
        "char_ngram_decision": decision,
        "tfidf": {
            "n_features": int(len(vec.get_feature_names_out())),
            "word_ngram": list(params.word_ngram),
            "min_df": params.min_df,
            "max_df": params.max_df,
            "sublinear_tf": params.sublinear_tf,
            "max_features": params.max_features,
            "use_char": params.use_char,
        },
        "split": {
            "strategy": "StratifiedGroupKFold hold-out (group = near-duplicate cluster)",
            "test_size": test_size,
            "fit_on": fit_on,
            "n_train": int(len(tr)),
            "n_test": int(len(te)),
        },
        "seed": seed,
        "risk_thresholds": {
            "low_below": float(mcfg.get("risk_threshold_low", 0.35)),
            "high_from": float(mcfg.get("risk_threshold_high", 0.65)),
            "tuned": False,
            "note": "provisional; tune on validation data of the final dataset",
        },
        "metrics": {
            "holdout_test": holdout["test"],
            "holdout_majority_baseline": holdout["majority_baseline_test"],
            "cv_grouped_repeated": cv_final,
            "note": "hold-out metrics describe the artifact only when fit_on='train'",
        },
        "trained_at_utc": trained_at.isoformat(timespec="seconds"),
        "artifacts": {
            "tfidf_vectorizer.joblib": file_sha256(vec_path),
            "calibrated_svm.joblib": clf_sha,
        },
        "environment": {
            "python": platform.python_version(),
            "scikit_learn": sklearn.__version__,
            "numpy": np.__version__,
            "scipy": scipy.__version__,
            "pandas": pd.__version__,
            "joblib": joblib.__version__,
            "hoax_ml": __version__,
        },
        "git": _git_info(),
    }
    _dump(out_dir / "model_metadata.json", metadata)
    _dump(MODELS_DIR / "latest.json", {"model_dir": cfg.dataset_id, "model_version": model_version})

    # 9. evaluation report
    result = {"holdout": holdout, "ablation": ablation, "char_decision": decision, "validation": val,
              "group_summary": gsum, "model_version": model_version}
    _dump(out_reports / "evaluation_report.json", result)
    (out_reports / "evaluation_report.md").write_text(render_eval_md(cfg, result, metadata), encoding="utf-8")
    print(f"Model saved -> {out_dir}  ({model_version})")
    print(f"Report      -> {out_reports / 'evaluation_report.md'}")
    return result


def _leakage_summary(cfg: DatasetConfig) -> list[str]:
    p = cfg.reports_dir / "leakage_report.json"
    if not p.exists():
        return ["_Leakage report not generated yet: run `python -m hoax_ml leakage --config ...`._"]
    rep = json.loads(p.read_text(encoding="utf-8"))
    lines = ["| Severity | Finding |", "|---|---|"]
    lines += [f"| {f['severity']} | {f['message']} |" for f in rep["findings"][:8]]
    lines.append(f"\nFull details: `reports/{cfg.dataset_id}/leakage_report.md`")
    return lines


def render_eval_md(cfg: DatasetConfig, r: dict, meta: dict) -> str:
    h, val = r["holdout"], r["validation"]
    t, mj = h["test"], h["majority_baseline_test"]
    cm = t["confusion_matrix"]
    L = [f"# Evaluation report — `{cfg.dataset_id}`", ""]
    if cfg.is_temporary:
        L += ["> **TEMPORARY DATASET — pipeline smoke test only.** Numbers below validate that the pipeline "
              "works; they say nothing about real-world performance. The final model must be retrained on "
              "the audited BRIN/MAFINDO dataset.", ""]
    L += [f"- Model version: `{r['model_version']}`",
          f"- Dataset: `{cfg.dataset_id}` v`{cfg.version}` — {val['rows']} raw rows, {meta['dataset']['rows_used']} used",
          f"- Signal: `ml_risk` = P(dataset label = 1) — a **content/linguistic risk signal**, not a truth verdict",
          f"- Seed: {meta['seed']} · preprocessing `{meta['preprocessing']['version']}` · "
          f"char n-grams: {meta['params']['use_char']}", ""]

    L += ["## 1. Data and class balance", "",
          f"- Positive (1) = {val['label_counts']['positive_1']}, negative (0) = {val['label_counts']['negative_0']} "
          f"(imbalance {val['imbalance_ratio']}:1).",
          f"- **Majority-class baseline accuracy = {val['majority_class_rate']:.3f}** — accuracy must be read against this number.",
          f"- Removed during preparation: {json.dumps({k: v for k, v in val['prepare_log'].items() if k.startswith('removed') and v})}",
          f"- Near-duplicate groups (cosine ≥ {meta['dataset']['near_duplicate_groups']['threshold']}): "
          f"{r['group_summary']['groups_with_more_than_one_row']} multi-row groups.", ""]
    for i in val["issues"]:
        L.append(f"- **{i['severity']}** `{i['code']}`: {i['message']}")
    L.append("")

    L += ["## 2. Hold-out test (single split — high variance on small data)", "",
          f"Train {h['n_train']} / test {h['n_test']} rows, stratified, near-duplicate-group aware. Threshold 0.5.", "",
          "| Metric | Calibrated SVM | Majority baseline |", "|---|---|---|"]
    for k, lab in (("accuracy", "Accuracy"), ("precision", "Precision (positive)"), ("recall", "Recall (positive)"),
                   ("f1_positive", "F1 (positive)"), ("macro_f1", "**Macro-F1**"), ("roc_auc", "ROC-AUC"),
                   ("pr_auc", "PR-AUC"), ("brier", "Brier score ↓"), ("ece", "ECE ↓ (5 bins)")):
        L.append(f"| {lab} | {_single(t, k)} | {_single(mj, k)} |")
    L += ["", "Confusion matrix (test; rows = actual, cols = predicted):", "",
          "| | pred 0 | pred 1 |", "|---|---|---|",
          f"| actual 0 | {cm['tn']} | {cm['fp']} |", f"| actual 1 | {cm['fn']} | {cm['tp']} |", "",
          f"One test sample = {100 / h['n_test']:.1f} accuracy points. Train macro-F1 = {h['train']['macro_f1']:.3f} "
          f"(gap vs test = {h['train']['macro_f1'] - t['macro_f1']:+.3f}).", ""]

    L += ["## 3. Repeated grouped cross-validation (more reliable than a single split)", "",
          "| Variant | Macro-F1 | F1 (pos) | Precision | Recall | ROC-AUC | PR-AUC | Brier | ECE |", "|---|---|---|---|---|---|---|---|---|"]
    for name, s in r["ablation"].items():
        L.append(f"| {name} | {_fmt(s['macro_f1'])} | {_fmt(s['f1_positive'])} | {_fmt(s['precision'])} | "
                 f"{_fmt(s['recall'])} | {_fmt(s['roc_auc'])} | {_fmt(s['pr_auc'])} | {_fmt(s['brier'])} | {_fmt(s['ece'])} |")
    d = r["char_decision"]
    L += ["", f"Char n-gram decision: `use_char={d.get('use_char')}` (gain {d.get('gain')}, word-only std {d.get('word_only_std')}). "
          f"Rule: {d.get('rule', 'n/a')}.", ""]

    L += ["## 4. Calibration (hold-out)", "",
          "| Bin | n | mean predicted risk | observed positive rate |", "|---|---|---|---|"]
    for b in h["reliability_test"]:
        L.append(f"| {b['bin']} | {b['n']} | {b['mean_predicted_risk']} | {b['observed_positive_rate']} |")
    L += ["", "Bins with few samples are unreliable; use CV Brier/ECE above as the main calibration evidence.", ""]

    L += ["## 5. Leakage check", ""] + _leakage_summary(cfg) + [""]

    L += ["## 6. Is this good enough as a baseline?", "", *_verdict(cfg, r, val), ""]
    return "\n".join(L)


def _verdict(cfg, r, val) -> list[str]:
    cvm = r["ablation"].get("word_1_2_plus_char_3_5" if r["char_decision"].get("use_char") else "word_tfidf_1_2")
    maj = r["ablation"].get("majority_baseline")
    out = []
    if cvm and maj:
        gain = cvm["macro_f1"]["mean"] - maj["macro_f1"]["mean"]
        out.append(f"- CV macro-F1 {cvm['macro_f1']['mean']:.3f} vs majority {maj['macro_f1']['mean']:.3f} "
                   f"(gain {gain:+.3f}).")
    if cfg.is_temporary:
        out.append("- Dataset is temporary and small: **valid only as a pipeline smoke test**, not as evidence of "
                   "hoax-detection quality.")
    out.append("- Treat any strong score together with the leakage findings above: a high score explained by "
               "markers/source style is not evidence of useful linguistic-risk detection.")
    out.append("- Next step: audit the BRIN/MAFINDO dataset, retrain with the same pipeline, re-run the leakage check, "
               "then tune risk thresholds on validation data.")
    return out
