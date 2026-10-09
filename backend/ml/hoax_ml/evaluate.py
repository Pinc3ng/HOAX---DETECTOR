"""Metrics, splits and repeated grouped cross-validation."""
from __future__ import annotations

import numpy as np
from sklearn.metrics import (
    accuracy_score,
    average_precision_score,
    brier_score_loss,
    confusion_matrix,
    f1_score,
    precision_score,
    recall_score,
    roc_auc_score,
)
from sklearn.model_selection import StratifiedGroupKFold

from .cleaning import clean_text
from .model import ModelParams, fit_model, predict_risk_proba

DEFAULT_THRESHOLD = 0.5
METRIC_KEYS = ("accuracy", "precision", "recall", "f1_positive", "macro_f1", "roc_auc", "pr_auc", "brier", "ece")


def clean_many(raw_texts, params: ModelParams) -> list[str]:
    return [
        clean_text(t, neutralize_markers=params.neutralize_markers, strip_dateline=params.strip_dateline)
        for t in raw_texts
    ]


def expected_calibration_error(y, p, n_bins: int = 5) -> float:
    y, p = np.asarray(y), np.asarray(p)
    edges = np.linspace(0, 1, n_bins + 1)
    ece, n = 0.0, len(y)
    for lo, hi in zip(edges[:-1], edges[1:]):
        m = (p >= lo) & ((p < hi) if hi < 1 else (p <= hi))
        if m.any():
            ece += m.sum() / n * abs(y[m].mean() - p[m].mean())
    return float(ece)


def reliability_table(y, p, n_bins: int = 5) -> list[dict]:
    y, p = np.asarray(y), np.asarray(p)
    edges = np.linspace(0, 1, n_bins + 1)
    rows = []
    for lo, hi in zip(edges[:-1], edges[1:]):
        m = (p >= lo) & ((p < hi) if hi < 1 else (p <= hi))
        rows.append(
            {
                "bin": f"{lo:.1f}-{hi:.1f}",
                "n": int(m.sum()),
                "mean_predicted_risk": round(float(p[m].mean()), 3) if m.any() else None,
                "observed_positive_rate": round(float(y[m].mean()), 3) if m.any() else None,
            }
        )
    return rows


def compute_metrics(y, p, threshold: float = DEFAULT_THRESHOLD) -> dict:
    y, p = np.asarray(y).astype(int), np.asarray(p, dtype=float)
    pred = (p >= threshold).astype(int)
    tn, fp, fn, tp = confusion_matrix(y, pred, labels=[0, 1]).ravel()
    both = len(set(y.tolist())) == 2
    return {
        "n": int(len(y)),
        "threshold": threshold,
        "accuracy": float(accuracy_score(y, pred)),
        "precision": float(precision_score(y, pred, zero_division=0)),
        "recall": float(recall_score(y, pred, zero_division=0)),
        "f1_positive": float(f1_score(y, pred, zero_division=0)),
        "macro_f1": float(f1_score(y, pred, average="macro", zero_division=0)),
        "roc_auc": float(roc_auc_score(y, p)) if both else None,
        "pr_auc": float(average_precision_score(y, p)) if both else None,
        "brier": float(brier_score_loss(y, p)),
        "ece": expected_calibration_error(y, p),
        "confusion_matrix": {"tn": int(tn), "fp": int(fp), "fn": int(fn), "tp": int(tp)},
        "positive_rate": float(y.mean()),
    }


def holdout_split(y, groups, test_size: float, seed: int):
    """Stratified, group-aware hold-out: first fold of a StratifiedGroupKFold."""
    n_splits = max(2, round(1 / test_size))
    cv = StratifiedGroupKFold(n_splits=n_splits, shuffle=True, random_state=seed)
    train_idx, test_idx = next(cv.split(np.zeros(len(y)), y, groups))
    return np.sort(train_idx), np.sort(test_idx)


def _majority_proba(y_train, n):
    return np.full(n, float(np.mean(y_train)))  # class prior: calibrated by construction


def repeated_grouped_cv(
    raw_texts,
    y,
    groups,
    params: ModelParams,
    kind: str = "svm",
    n_splits: int = 5,
    n_repeats: int = 3,
    seed: int = 42,
) -> dict:
    """Repeated stratified *group* K-fold. ``kind`` is ``svm`` or ``majority`` (baseline)."""
    raw_texts, y, groups = list(raw_texts), np.asarray(y), np.asarray(groups)
    texts = clean_many(raw_texts, params)
    folds = []
    for r in range(n_repeats):
        cv = StratifiedGroupKFold(n_splits=n_splits, shuffle=True, random_state=seed + r)
        for k, (tr, te) in enumerate(cv.split(np.zeros(len(y)), y, groups)):
            if kind == "majority":
                p = _majority_proba(y[tr], len(te))
            else:
                vec, clf = fit_model([texts[i] for i in tr], y[tr], groups[tr], params, seed + r)
                p = predict_risk_proba(vec, clf, [texts[i] for i in te])
            m = compute_metrics(y[te], p)
            m.update({"repeat": r, "fold": k})
            folds.append(m)
    return summarize_folds(folds)


def summarize_folds(folds: list[dict]) -> dict:
    out = {"n_folds": len(folds)}
    for key in METRIC_KEYS:
        vals = [f[key] for f in folds if f[key] is not None]
        out[key] = (
            {"mean": float(np.mean(vals)), "std": float(np.std(vals, ddof=1)) if len(vals) > 1 else 0.0}
            if vals
            else None
        )
    cm = {k: int(sum(f["confusion_matrix"][k] for f in folds)) for k in ("tn", "fp", "fn", "tp")}
    out["pooled_confusion_matrix"] = cm
    return out
