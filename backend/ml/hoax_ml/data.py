"""Loading, validating and preparing a dataset in the standard schema.

Nothing is dropped silently: every removal is counted and returned in the
``PrepareLog`` which ends up in the validation report and model metadata.
"""
from __future__ import annotations

import hashlib
from dataclasses import dataclass, field

import pandas as pd

from .cleaning import build_text, clean_text
from .schema import NEGATIVE_LABEL, POSITIVE_LABEL, DatasetConfig

MIN_ROWS_WARN = 500
MIN_CLASS_WARN = 100
IMBALANCE_WARN = 3.0
SHORT_TEXT_WORDS = 20


def file_sha256(path) -> str:
    h = hashlib.sha256()
    with open(path, "rb") as f:
        for chunk in iter(lambda: f.read(1 << 20), b""):
            h.update(chunk)
    return h.hexdigest()


def read_raw(cfg: DatasetConfig) -> pd.DataFrame:
    p = cfg.raw_path
    if not p.exists():
        raise FileNotFoundError(
            f"Raw dataset not found: {p}\n"
            f"Hint: python -m hoax_ml fetch --config {cfg.config_path} (public datasets) "
            f"or place the file there manually."
        )
    suffix = p.suffix.lower()
    if suffix == ".csv":
        return pd.read_csv(p)
    if suffix in (".json", ".jsonl"):
        return pd.read_json(p, lines=suffix == ".jsonl")
    if suffix == ".parquet":
        return pd.read_parquet(p)
    raise ValueError(f"Unsupported raw file type: {suffix}")


def load_standardized(cfg: DatasetConfig) -> pd.DataFrame:
    """Map source columns onto ``id, title, content, label`` (+ ``label_raw``, ``raw_text``)."""
    raw = read_raw(cfg)
    cols = cfg.columns
    missing = [src for src in cols.values() if src and src not in raw.columns]
    if missing:
        raise KeyError(f"Mapped columns not found in raw data: {missing}. Available: {list(raw.columns)}")

    out = pd.DataFrame(index=raw.index)
    out["id"] = raw[cols["id"]].astype(str) if cols.get("id") else raw.index.astype(str)
    out["title"] = raw[cols["title"]].fillna("").astype(str) if cols.get("title") else ""
    out["content"] = raw[cols["content"]].fillna("").astype(str)
    out["label_raw"] = raw[cols["label"]].astype(str).str.strip()
    lmap = {k.lower(): v for k, v in cfg.label_map.items()}
    out["label"] = out["label_raw"].str.lower().map(lmap)
    out["raw_text"] = [build_text(t, c) for t, c in zip(out["title"], out["content"])]
    return out.reset_index(drop=True)


def _norm_key(text: str) -> str:
    # Duplicate key independent of the cleaning options.
    return clean_text(text)


def validate(df: pd.DataFrame, cfg: DatasetConfig) -> dict:
    issues: list[dict] = []

    def add(sev: str, code: str, msg: str):
        issues.append({"severity": sev, "code": code, "message": msg})

    n = len(df)
    unmapped = df.loc[df["label"].isna(), "label_raw"].value_counts().to_dict()
    if unmapped:
        add("error", "unmapped_labels", f"Labels not in label_map (these rows cannot be used): {unmapped}")

    ok = df.dropna(subset=["label"])
    counts = ok["label"].astype(int).value_counts().to_dict()
    pos, neg = counts.get(POSITIVE_LABEL, 0), counts.get(NEGATIVE_LABEL, 0)
    imbalance = round(max(pos, neg) / max(min(pos, neg), 1), 2)
    majority_rate = round(max(pos, neg) / max(pos + neg, 1), 4)

    empty = int((df["raw_text"].str.strip() == "").sum())
    if empty:
        add("warn", "empty_text", f"{empty} rows have empty text.")
    words = df["raw_text"].str.split().str.len()
    short = int((words < SHORT_TEXT_WORDS).sum())
    if short:
        add("info", "short_text", f"{short} rows have fewer than {SHORT_TEXT_WORDS} words.")

    keys = df["raw_text"].map(_norm_key)
    dup_mask = keys.duplicated(keep=False)
    conflict_groups = 0
    if dup_mask.any():
        g = df[dup_mask].assign(_k=keys[dup_mask]).groupby("_k")["label"].nunique()
        conflict_groups = int((g > 1).sum())
    n_dup_extra = int(keys.duplicated().sum())
    if n_dup_extra:
        add("warn", "exact_duplicates", f"{n_dup_extra} exact-duplicate rows (after normalisation).")
    if conflict_groups:
        add("high", "conflicting_duplicates", f"{conflict_groups} duplicate groups carry conflicting labels.")

    if n < MIN_ROWS_WARN:
        add("warn", "small_dataset", f"Only {n} rows (< {MIN_ROWS_WARN}). Metrics will have high variance.")
    if min(pos, neg) < MIN_CLASS_WARN:
        add("warn", "small_minority", f"Minority class has {min(pos, neg)} rows (< {MIN_CLASS_WARN}).")
    if imbalance > IMBALANCE_WARN:
        add("warn", "class_imbalance", f"Imbalance ratio {imbalance}:1 (> {IMBALANCE_WARN}:1).")

    if cfg.is_temporary:
        add("warn", "temporary_dataset", "Dataset is marked TEMPORARY: results are for pipeline testing only.")

    return {
        "dataset_id": cfg.dataset_id,
        "version": cfg.version,
        "status": cfg.status,
        "rows": n,
        "label_counts": {"positive_1": int(pos), "negative_0": int(neg)},
        "imbalance_ratio": imbalance,
        "majority_class_rate": majority_rate,
        "empty_text": empty,
        "words": words.describe().round(1).to_dict(),
        "exact_duplicate_extra_rows": n_dup_extra,
        "duplicate_groups_with_conflicting_labels": conflict_groups,
        "issues": issues,
        "has_blocking_error": any(i["severity"] == "error" for i in issues),
    }


@dataclass
class PrepareLog:
    rows_in: int = 0
    removed_unmapped_label: int = 0
    removed_empty: int = 0
    removed_conflicting_duplicates: int = 0
    removed_exact_duplicates: int = 0
    rows_out: int = 0
    removed_ids: dict = field(default_factory=dict)

    def as_dict(self) -> dict:
        return {k: v for k, v in self.__dict__.items()}


def prepare(df: pd.DataFrame, cfg: DatasetConfig) -> tuple[pd.DataFrame, PrepareLog]:
    """Apply the (config-controlled) row filters and record exactly what was removed."""
    c = cfg.cleaning
    log = PrepareLog(rows_in=len(df))
    cur = df.copy()

    m = cur["label"].isna()
    log.removed_unmapped_label = int(m.sum())
    log.removed_ids["unmapped_label"] = cur.loc[m, "id"].tolist()
    cur = cur[~m]

    if c.get("drop_empty", True):
        m = cur["raw_text"].str.strip() == ""
        log.removed_empty = int(m.sum())
        log.removed_ids["empty"] = cur.loc[m, "id"].tolist()
        cur = cur[~m]

    keys = cur["raw_text"].map(_norm_key)
    if c.get("drop_conflicting_duplicates", True):
        nun = cur.assign(_k=keys).groupby("_k")["label"].transform("nunique")
        m = nun > 1
        log.removed_conflicting_duplicates = int(m.sum())
        log.removed_ids["conflicting_duplicates"] = cur.loc[m, "id"].tolist()
        cur, keys = cur[~m], keys[~m]

    if c.get("dedupe_exact", True):
        m = keys.duplicated(keep="first")
        log.removed_exact_duplicates = int(m.sum())
        log.removed_ids["exact_duplicates"] = cur.loc[m, "id"].tolist()
        cur = cur[~m]

    cur = cur.reset_index(drop=True)
    cur["label"] = cur["label"].astype(int)
    log.rows_out = len(cur)
    return cur, log
