"""Dataset audit for Indonesian hoax datasets.

Usage (from repo root):
    python ml/audit/audit_dataset.py --hf nlp-brin-id/fakenews-mafindo --name mafindo \
        --label-col Label --text-col Content --title-col Title \
        --fact-col Fact --ref-col References
    python ml/audit/audit_dataset.py --file data/raw/some.csv --name some --label-col label --text-col text

Outputs reports/<name>/audit.json, audit.md and label_review_sample.csv.
The audit only *measures*; the decision of which dataset to use is documented
manually in docs/dataset_audit.md.
"""
from __future__ import annotations

import argparse
import json
import re
from collections import Counter
from pathlib import Path

import numpy as np
import pandas as pd

# Markers that often leak the label (fact-check boilerplate, site names, ...).
LEAK_PATTERNS = {
    "hoax_word": r"\bho(?:a|ak)?x\b|\bhoaks\b|\bhoax\b",
    "salah_word": r"\bsalah\b|\bmenyesatkan\b|\bdisinformasi\b|\bmisinformasi\b",
    "fakta_word": r"\bfakta\b|\bpenjelasan\b|\bkesimpulan\b|\bnarasi\b",
    "tag_bracket": r"\[(?:hoax|hoaks|salah|fakta|disinformasi|misinformasi|klarifikasi)[^\]]*\]",
    "site_name": r"turnbackhoax|mafindo|saber\s*hoaks|klinik\s*hoaks|cekfakta|kominfo|komdigi",
    "url": r"https?://\S+",
    "forward_phrase": r"sebarkan|share(?:\s+ke)?\s+grup|viral|tolong\s+diteruskan",
}


def normalize(text: str) -> str:
    text = str(text).lower()
    text = re.sub(r"\s+", " ", text)
    return text.strip()


def load(args) -> pd.DataFrame:
    if args.hf:
        from datasets import load_dataset  # type: ignore

        ds = load_dataset(args.hf)
        frames = []
        for split, d in ds.items():
            df = d.to_pandas()
            df["_split"] = split
            frames.append(df)
        return pd.concat(frames, ignore_index=True)
    if args.file.endswith(".csv"):
        return pd.read_csv(args.file)
    if args.file.endswith((".json", ".jsonl")):
        return pd.read_json(args.file, lines=args.file.endswith(".jsonl"))
    raise SystemExit("Unsupported file type")


def label_distribution(df: pd.DataFrame, label_col: str) -> dict:
    vc = df[label_col].astype(str).value_counts(dropna=False)
    return {
        "counts": vc.to_dict(),
        "ratio": (vc / vc.sum()).round(4).to_dict(),
        "imbalance_ratio": round(float(vc.max() / max(vc.min(), 1)), 2),
    }


def structure(df: pd.DataFrame, text_col: str) -> dict:
    lens = df[text_col].astype(str).str.split().str.len()
    return {
        "rows": int(len(df)),
        "columns": {c: str(t) for c, t in df.dtypes.items()},
        "nulls": df.isna().sum().to_dict(),
        "empty_text": int((df[text_col].astype(str).str.strip() == "").sum()),
        "text_len_words": lens.describe().round(1).to_dict(),
    }


def exact_duplicates(df: pd.DataFrame, text_col: str, label_col: str) -> dict:
    key = df[text_col].astype(str).map(normalize)
    dup_mask = key.duplicated(keep=False)
    groups = df[dup_mask].assign(_key=key[dup_mask]).groupby("_key")[label_col].nunique()
    return {
        "rows_in_duplicate_groups": int(dup_mask.sum()),
        "extra_copies": int(key.duplicated().sum()),
        "groups_with_conflicting_labels": int((groups > 1).sum()),
    }


def near_duplicates(df: pd.DataFrame, text_col: str, label_col: str, threshold: float, max_rows: int) -> dict:
    from sklearn.feature_extraction.text import TfidfVectorizer
    from sklearn.neighbors import NearestNeighbors

    sub = df.sample(min(len(df), max_rows), random_state=42).reset_index(drop=True)
    texts = sub[text_col].astype(str).map(normalize)
    X = TfidfVectorizer(min_df=2, max_features=50000, sublinear_tf=True).fit_transform(texts)
    nn = NearestNeighbors(n_neighbors=2, metric="cosine", algorithm="brute").fit(X)
    dist, idx = nn.kneighbors(X)
    sim = 1 - dist[:, 1]
    nb = idx[:, 1]
    near = sim >= threshold
    labels = sub[label_col].astype(str).to_numpy()
    conflict = near & (labels != labels[nb])
    examples = [
        {"sim": round(float(sim[i]), 3), "a": texts[i][:160], "b": texts[nb[i]][:160],
         "label_a": labels[i], "label_b": labels[nb[i]]}
        for i in np.where(near)[0][:5]
    ]
    return {
        "sampled_rows": int(len(sub)),
        "threshold": threshold,
        "rows_with_near_duplicate": int(near.sum()),
        "near_duplicate_ratio": round(float(near.mean()), 4),
        "near_duplicates_with_conflicting_labels": int(conflict.sum()),
        "examples": examples,
    }


def leakage_markers(df: pd.DataFrame, text_col: str, label_col: str, title_col: str | None) -> dict:
    text = df[text_col].astype(str).str.lower()
    if title_col and title_col in df:
        text = df[title_col].astype(str).str.lower() + " " + text
    out = {}
    for name, pat in LEAK_PATTERNS.items():
        hit = text.str.contains(pat, regex=True)
        per_label = hit.groupby(df[label_col].astype(str)).mean().round(4).to_dict()
        gap = (max(per_label.values()) - min(per_label.values())) if per_label else 0
        out[name] = {"share_per_label": per_label, "max_gap": round(float(gap), 4)}
    return out


def discriminative_tokens(df: pd.DataFrame, text_col: str, label_col: str, top: int = 25) -> dict:
    from sklearn.feature_extraction.text import CountVectorizer

    labels = df[label_col].astype(str)
    if labels.nunique() != 2:
        return {"note": "skipped (needs exactly 2 labels)"}
    cv = CountVectorizer(min_df=5, max_features=30000, ngram_range=(1, 2))
    X = cv.fit_transform(df[text_col].astype(str).map(normalize))
    names = np.array(cv.get_feature_names_out())
    a, b = sorted(labels.unique())
    ma = (labels == a).to_numpy()
    ca = np.asarray(X[ma].sum(axis=0)).ravel() + 1
    cb = np.asarray(X[~ma].sum(axis=0)).ravel() + 1
    logodds = np.log(ca / ca.sum()) - np.log(cb / cb.sum())
    return {
        f"top_for_{a}": names[np.argsort(-logodds)[:top]].tolist(),
        f"top_for_{b}": names[np.argsort(logodds)[:top]].tolist(),
    }


def quick_baseline(df: pd.DataFrame, text_col: str, label_col: str, clean: bool, title_col: str | None) -> float | None:
    from sklearn.feature_extraction.text import TfidfVectorizer
    from sklearn.linear_model import LogisticRegression
    from sklearn.metrics import f1_score
    from sklearn.model_selection import train_test_split

    labels = df[label_col].astype(str)
    if labels.nunique() != 2:
        return None
    text = df[text_col].astype(str).map(normalize)
    if title_col and title_col in df:
        text = df[title_col].astype(str).map(normalize) + " " + text
    if clean:
        for pat in LEAK_PATTERNS.values():
            text = text.str.replace(pat, " ", regex=True)
    Xtr, Xte, ytr, yte = train_test_split(text, labels, test_size=0.2, stratify=labels, random_state=42)
    vec = TfidfVectorizer(min_df=2, ngram_range=(1, 2), sublinear_tf=True, max_features=100000)
    clf = LogisticRegression(max_iter=1000, class_weight="balanced")
    clf.fit(vec.fit_transform(Xtr), ytr)
    return round(float(f1_score(yte, clf.predict(vec.transform(Xte)), average="macro")), 4)


def evidence_fields(df: pd.DataFrame, fact_col: str | None, ref_col: str | None) -> dict:
    out = {}
    for name, col in (("fact", fact_col), ("references", ref_col)):
        if col and col in df:
            s = df[col].astype(str).str.strip()
            out[name] = {"non_empty_ratio": round(float(((s != "") & (s.str.lower() != "nan")).mean()), 4)}
    if ref_col and ref_col in df:
        urls = df[ref_col].astype(str).str.contains(r"https?://", regex=True)
        out["references_with_url_ratio"] = round(float(urls.mean()), 4)
    return out


def render_md(name: str, rep: dict) -> str:
    def j(x):
        return "```json\n" + json.dumps(x, indent=2, ensure_ascii=False, default=str) + "\n```\n"

    parts = [f"# Audit: {name}\n"]
    for key, title in (
        ("structure", "Struktur"), ("label_distribution", "Distribusi label"),
        ("exact_duplicates", "Duplikat persis"), ("near_duplicates", "Near-duplicate"),
        ("leakage_markers", "Penanda leakage (share per label)"),
        ("discriminative_tokens", "Token paling diskriminatif"),
        ("baseline_macro_f1", "Baseline macro-F1 (indikator leakage)"),
        ("evidence_fields", "Field evidence"),
    ):
        if key in rep:
            parts.append(f"## {title}\n{j(rep[key])}")
    return "\n".join(parts)


def main() -> None:
    ap = argparse.ArgumentParser()
    src = ap.add_mutually_exclusive_group(required=True)
    src.add_argument("--hf")
    src.add_argument("--file")
    ap.add_argument("--name", required=True)
    ap.add_argument("--label-col", required=True)
    ap.add_argument("--text-col", required=True)
    ap.add_argument("--title-col")
    ap.add_argument("--fact-col")
    ap.add_argument("--ref-col")
    ap.add_argument("--near-threshold", type=float, default=0.9)
    ap.add_argument("--near-max-rows", type=int, default=8000)
    ap.add_argument("--out", default="reports")
    args = ap.parse_args()

    df = load(args)
    out_dir = Path(args.out) / args.name
    out_dir.mkdir(parents=True, exist_ok=True)

    rep = {
        "structure": structure(df, args.text_col),
        "label_distribution": label_distribution(df, args.label_col),
        "exact_duplicates": exact_duplicates(df, args.text_col, args.label_col),
        "near_duplicates": near_duplicates(df, args.text_col, args.label_col, args.near_threshold, args.near_max_rows),
        "leakage_markers": leakage_markers(df, args.text_col, args.label_col, args.title_col),
        "discriminative_tokens": discriminative_tokens(df, args.text_col, args.label_col),
        "baseline_macro_f1": {
            "raw_text": quick_baseline(df, args.text_col, args.label_col, False, args.title_col),
            "markers_removed": quick_baseline(df, args.text_col, args.label_col, True, args.title_col),
            "note": "Skor sangat tinggi + penurunan besar setelah marker dihapus => indikasi leakage.",
        },
        "evidence_fields": evidence_fields(df, args.fact_col, args.ref_col),
    }

    (out_dir / "audit.json").write_text(json.dumps(rep, indent=2, ensure_ascii=False, default=str), encoding="utf-8")
    (out_dir / "audit.md").write_text(render_md(args.name, rep), encoding="utf-8")

    # Manual label-quality review sample (30 per label).
    review = (
        df.groupby(df[args.label_col].astype(str), group_keys=False)
        .apply(lambda g: g.sample(min(30, len(g)), random_state=42))
    )
    keep = [c for c in (args.title_col, args.text_col, args.label_col, args.fact_col, args.ref_col) if c and c in review]
    review = review[keep].copy()
    review[args.text_col] = review[args.text_col].astype(str).str.slice(0, 600)
    review["reviewer_agrees_with_label"] = ""
    review["reviewer_note"] = ""
    review.to_csv(out_dir / "label_review_sample.csv", index=False, encoding="utf-8-sig")

    print(f"Audit selesai -> {out_dir}")


if __name__ == "__main__":
    main()
