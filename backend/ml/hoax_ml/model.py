"""TF-IDF + calibrated LinearSVC.

The vectorizer and the calibrated classifier are kept as two separate objects so they can be
persisted as ``tfidf_vectorizer.joblib`` and ``calibrated_svm.joblib``.
"""
from __future__ import annotations

from dataclasses import asdict, dataclass

import numpy as np
from sklearn.calibration import CalibratedClassifierCV
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.model_selection import StratifiedGroupKFold
from sklearn.pipeline import FeatureUnion
from sklearn.svm import LinearSVC

MODEL_TYPE = "TfidfVectorizer + LinearSVC + CalibratedClassifierCV"


@dataclass(frozen=True)
class ModelParams:
    # --- text preprocessing (applied before vectorising) ---
    neutralize_markers: bool = False   # explicit opt-in, see cleaning.py
    strip_dateline: bool = False
    # --- TF-IDF ---
    word_ngram: tuple = (1, 2)
    min_df: int = 2
    max_df: float = 0.95
    sublinear_tf: bool = True
    max_features: int | None = 50000
    use_char: bool = False
    char_ngram: tuple = (3, 5)
    char_max_features: int | None = 50000
    # --- LinearSVC ---
    C: float = 1.0
    class_weight: str | None = "balanced"
    max_iter: int = 10000
    # --- calibration ---
    calibration_method: str = "sigmoid"   # Platt scaling: safer than isotonic on small data
    calibration_cv: int = 5

    def to_dict(self) -> dict:
        d = asdict(self)
        d["word_ngram"] = list(self.word_ngram)
        d["char_ngram"] = list(self.char_ngram)
        return d

    @classmethod
    def from_dict(cls, d: dict) -> "ModelParams":
        d = dict(d)
        for k in ("word_ngram", "char_ngram"):
            if k in d:
                d[k] = tuple(d[k])
        known = {f for f in cls.__dataclass_fields__}
        return cls(**{k: v for k, v in d.items() if k in known})


def build_vectorizer(p: ModelParams):
    word = TfidfVectorizer(
        analyzer="word",
        ngram_range=p.word_ngram,
        min_df=p.min_df,
        max_df=p.max_df,
        sublinear_tf=p.sublinear_tf,
        max_features=p.max_features,
        token_pattern=r"(?u)\b\w\w+\b",
        lowercase=False,  # cleaning already lower-cases
    )
    if not p.use_char:
        return word
    char = TfidfVectorizer(
        analyzer="char_wb",
        ngram_range=p.char_ngram,
        min_df=p.min_df,
        sublinear_tf=p.sublinear_tf,
        max_features=p.char_max_features,
        lowercase=False,
    )
    return FeatureUnion([("word", word), ("char", char)])


def calibration_splits(y: np.ndarray, groups: np.ndarray, n_splits: int, seed: int):
    """Group-aware splits for the calibration CV (near-duplicates stay together)."""
    n_splits = max(2, min(n_splits, int(np.bincount(y).min())))
    cv = StratifiedGroupKFold(n_splits=n_splits, shuffle=True, random_state=seed)
    return list(cv.split(np.zeros(len(y)), y, groups))


def fit_model(texts, y, groups, params: ModelParams, seed: int):
    """Fit vectorizer + calibrated SVM on already-cleaned ``texts``."""
    y = np.asarray(y)
    vec = build_vectorizer(params)
    X = vec.fit_transform(texts)
    base = LinearSVC(
        C=params.C, class_weight=params.class_weight, max_iter=params.max_iter, random_state=seed
    )
    cv = calibration_splits(y, np.asarray(groups), params.calibration_cv, seed)
    clf = CalibratedClassifierCV(base, method=params.calibration_method, cv=cv)
    clf.fit(X, y)
    return vec, clf


def predict_risk_proba(vec, clf, texts) -> np.ndarray:
    """P(class 1) = ``ml_risk`` signal in [0, 1]."""
    X = vec.transform(texts)
    proba = clf.predict_proba(X)
    idx = list(clf.classes_).index(1)
    return proba[:, idx]
