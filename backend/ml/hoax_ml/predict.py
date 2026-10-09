"""Inference API for the backend.

    from hoax_ml.predict import predict_content_risk
    result = predict_content_risk(text)

The result is a *content / linguistic risk signal* (``ml_risk``) intended as ONE input of the
fuzzy decision layer. It is not a verdict about the truth of the text and deliberately has no
``is_hoax`` / ``is_true`` field.
"""
from __future__ import annotations

import json
import os
import warnings
from functools import lru_cache
from pathlib import Path

import joblib
import sklearn

from .cleaning import build_text, clean_text
from .data import file_sha256
from .model import ModelParams, predict_risk_proba
from .schema import MODELS_DIR

MIN_WORDS = 15
NOTICE = ("Linguistic risk signal from a baseline model. It does not determine whether the content is true; "
          "verify against evidence.")


class ModelNotFoundError(FileNotFoundError):
    pass


def resolve_model_dir(model_dir: str | Path | None = None) -> Path:
    if model_dir is None:
        model_dir = os.environ.get("HOAX_ML_MODEL_DIR")
    if model_dir is None:
        latest = MODELS_DIR / "latest.json"
        if not latest.exists():
            raise ModelNotFoundError("No trained model found. Run: python -m hoax_ml train --config <config>")
        model_dir = json.loads(latest.read_text(encoding="utf-8"))["model_dir"]
    p = Path(model_dir)
    return p if p.is_absolute() else MODELS_DIR / p


@lru_cache(maxsize=4)
def _load(model_dir: str):
    d = Path(model_dir)
    meta_path = d / "model_metadata.json"
    if not meta_path.exists():
        raise ModelNotFoundError(f"model_metadata.json missing in {d}; refusing to load artifacts without metadata")
    meta = json.loads(meta_path.read_text(encoding="utf-8"))
    for name, expected in meta["artifacts"].items():
        if file_sha256(d / name) != expected:
            raise RuntimeError(f"Artifact {name} does not match its recorded sha256 (modified or corrupted).")
    trained_with = meta["environment"]["scikit_learn"]
    if trained_with != sklearn.__version__:
        warnings.warn(f"Model trained with scikit-learn {trained_with}, running {sklearn.__version__}.", stacklevel=2)
    vec = joblib.load(d / "tfidf_vectorizer.joblib")
    clf = joblib.load(d / "calibrated_svm.joblib")
    return vec, clf, meta, ModelParams.from_dict(meta["params"])


def _label(score: float, thr: dict) -> str:
    if score < thr["low_below"]:
        return "low"
    if score >= thr["high_from"]:
        return "high"
    return "medium"


def predict_content_risk(text: str, title: str | None = None, model_dir: str | Path | None = None) -> dict:
    vec, clf, meta, params = _load(str(resolve_model_dir(model_dir)))
    base = {
        "signal": "ml_risk",
        "model_version": meta["model_version"],
        "model_reliability": meta.get("model_reliability", "baseline"),
        "training_dataset_status": meta["dataset"]["status"],
        "notice": NOTICE,
    }
    raw = build_text(title, text)
    n_words = len(raw.split())
    if n_words < MIN_WORDS:
        return {**base, "risk_label": "insufficient_text", "risk_score": None,
                "reason": f"Text has {n_words} words; at least {MIN_WORDS} are needed for a meaningful signal."}
    cleaned = clean_text(raw, neutralize_markers=params.neutralize_markers, strip_dateline=params.strip_dateline)
    score = float(predict_risk_proba(vec, clf, [cleaned])[0])
    return {**base, "risk_label": _label(score, meta["risk_thresholds"]), "risk_score": round(score, 4)}
