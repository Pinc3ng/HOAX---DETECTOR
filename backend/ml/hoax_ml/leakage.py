"""Leakage / shortcut diagnostics.

The check only MEASURES and REPORTS. It never edits the data. The effect of removing a
shortcut is quantified with ablation variants evaluated by repeated grouped CV.
"""
from __future__ import annotations

import re
from collections import Counter
from dataclasses import replace

import numpy as np
from scipy.stats import fisher_exact
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import roc_auc_score
from sklearn.model_selection import StratifiedGroupKFold

from .cleaning import DATELINE_RE, MARKER_GROUPS
from .evaluate import clean_many, compute_metrics, repeated_grouped_cv
from .groups import group_summary
from .model import ModelParams


def _sev(gap: float, p: float) -> str:
    if gap >= 0.25 and p < 0.01:
        return "high"
    if gap >= 0.10 and p < 0.05:
        return "warn"
    return "info"


def marker_prevalence(raw_texts, y) -> dict:
    y = np.asarray(y)
    out = {}
    for name, pat in MARKER_GROUPS.items():
        hit = np.array([bool(pat.search(t)) for t in raw_texts])
        a, b = int(hit[y == 1].sum()), int(hit[y == 0].sum())
        n1, n0 = int((y == 1).sum()), int((y == 0).sum())
        s1, s0 = a / max(n1, 1), b / max(n0, 1)
        p = fisher_exact([[a, n1 - a], [b, n0 - b]])[1]
        examples = {
            lab: Counter(m.group(0).lower() for t, yy in zip(raw_texts, y) if yy == lab for m in pat.finditer(t)).most_common(6)
            for lab in (1, 0)
        }
        out[name] = {
            "share_positive": round(s1, 3),
            "share_negative": round(s0, 3),
            "gap": round(abs(s1 - s0), 3),
            "fisher_p": float(p),
            "severity": _sev(abs(s1 - s0), p),
            "top_matches_positive": examples[1],
            "top_matches_negative": examples[0],
        }
    return out


def style_shortcuts(raw_texts, y) -> dict:
    y = np.asarray(y)
    texts = list(raw_texts)
    words = np.array([len(t.split()) for t in texts], dtype=float)

    def ratio(fn):
        return np.array([fn(t) for t in texts], dtype=float)

    feats = {
        "word_count": words,
        "upper_ratio": ratio(lambda t: sum(c.isupper() for c in t) / max(len(t), 1)),
        "digit_ratio": ratio(lambda t: sum(c.isdigit() for c in t) / max(len(t), 1)),
        "exclamation_count": ratio(lambda t: t.count("!")),
        "quote_count": ratio(lambda t: t.count('"') + t.count("“") + t.count("”")),
        "avg_word_len": ratio(lambda t: np.mean([len(w) for w in t.split()]) if t.split() else 0),
        "starts_with_quote": ratio(lambda t: t.lstrip()[:1] in "\"“'"),
        "starts_with_dateline": ratio(lambda t: bool(DATELINE_RE.match(t.lstrip().lstrip('"“').lstrip()))),
    }
    table = {}
    for k, v in feats.items():
        both = len(set(v.tolist())) > 1
        auc = float(roc_auc_score(y, v)) if both else 0.5
        table[k] = {
            "auc_as_single_feature": round(auc, 3),
            "median_positive": float(np.median(v[y == 1])),
            "median_negative": float(np.median(v[y == 0])),
            "severity": "high" if abs(auc - 0.5) >= 0.25 else "warn" if abs(auc - 0.5) >= 0.15 else "info",
        }
    return table


def _log_odds_tokens(clean_texts, y, top=20):
    cv = CountVectorizer(min_df=3, ngram_range=(1, 1))
    X = cv.fit_transform(clean_texts)
    names = np.array(cv.get_feature_names_out())
    y = np.asarray(y)
    c1 = np.asarray(X[y == 1].sum(axis=0)).ravel() + 0.5
    c0 = np.asarray(X[y == 0].sum(axis=0)).ravel() + 0.5
    lo = np.log(c1 / c1.sum()) - np.log(c0 / c0.sum())
    pos = names[np.argsort(-lo)[:top]].tolist()
    neg = names[np.argsort(lo)[:top]].tolist()
    return pos, neg


def discriminative_tokens(raw_texts, y, top=20) -> dict:
    base = ModelParams()
    pos, neg = _log_odds_tokens(clean_many(raw_texts, base), y, top)
    shortcut_pats = [MARKER_GROUPS[g] for g in ("verdict_words", "factcheck_sites", "outlets", "bracket_tags")]

    def flagged(tokens):
        return [t for t in tokens if any(p.search(t) for p in shortcut_pats)]

    return {
        "top_positive_class": pos,
        "top_negative_class": neg,
        "flagged_as_marker_positive": flagged(pos),
        "flagged_as_marker_negative": flagged(neg),
        "note": "Tokens are ranked by smoothed log-odds on the full dataset; they describe the data, "
        "not the truth of any article.",
    }


def _length_only_cv(raw_texts, y, groups, n_splits, n_repeats, seed):
    y, groups = np.asarray(y), np.asarray(groups)
    x = np.log1p(np.array([len(t.split()) for t in raw_texts], dtype=float)).reshape(-1, 1)
    folds = []
    for r in range(n_repeats):
        cv = StratifiedGroupKFold(n_splits=n_splits, shuffle=True, random_state=seed + r)
        for tr, te in cv.split(x, y, groups):
            clf = LogisticRegression(class_weight="balanced").fit(x[tr], y[tr])
            folds.append(compute_metrics(y[te], clf.predict_proba(x[te])[:, 1]))
    return float(np.mean([f["macro_f1"] for f in folds])), float(np.std([f["macro_f1"] for f in folds], ddof=1))


def _head(t, n=20):
    return " ".join(t.split()[:n])


def _tail(t, n=20):
    return " ".join(t.split()[-n:])


def shortcut_probes(raw_texts, y, groups, base: ModelParams, n_splits, n_repeats, seed) -> dict:
    kw = dict(n_splits=n_splits, n_repeats=n_repeats, seed=seed)
    variants = {
        "majority_baseline": ("majority", raw_texts, base),
        "full_text": ("svm", raw_texts, base),
        "markers_neutralized": ("svm", raw_texts, replace(base, neutralize_markers=True)),
        "markers_neutralized_and_dateline_stripped": (
            "svm", raw_texts, replace(base, neutralize_markers=True, strip_dateline=True)),
        "first_20_words_only": ("svm", [_head(t) for t in raw_texts], base),
        "last_20_words_only": ("svm", [_tail(t) for t in raw_texts], base),
    }
    res = {}
    for name, (kind, texts, params) in variants.items():
        s = repeated_grouped_cv(texts, y, groups, params, kind=kind, **kw)
        res[name] = {"macro_f1": s["macro_f1"], "roc_auc": s["roc_auc"], "pr_auc": s["pr_auc"]}
    m, sd = _length_only_cv(raw_texts, y, groups, n_splits, n_repeats, seed)
    res["length_only_logreg"] = {"macro_f1": {"mean": m, "std": sd}, "roc_auc": None, "pr_auc": None}
    return res


def run_leakage_check(df, groups, base: ModelParams, near_threshold: float, n_splits=5, n_repeats=3, seed=42) -> dict:
    raw, y = df["raw_text"].tolist(), df["label"].to_numpy()
    markers = marker_prevalence(raw, y)
    style = style_shortcuts(raw, y)
    tokens = discriminative_tokens(raw, y)
    probes = shortcut_probes(raw, y, groups, base, n_splits, n_repeats, seed)
    dup = group_summary(groups, y)

    f1 = {k: v["macro_f1"]["mean"] for k, v in probes.items()}
    findings = []

    def add(sev, code, msg, impact):
        findings.append({"severity": sev, "code": code, "message": msg, "impact": impact})

    for g, v in markers.items():
        if g == "forward_phrases":
            continue  # genuine linguistic cue, reported but not treated as a shortcut
        if v["severity"] in ("high", "warn"):
            add(v["severity"], f"marker_{g}",
                f"'{g}' appears in {v['share_positive']:.0%} of positive vs {v['share_negative']:.0%} of negative rows "
                f"(Fisher p={v['fisher_p']:.3g}).",
                "The classifier can use this as a shortcut instead of linguistic risk patterns; scores will not "
                "generalise to articles without this marker.")
    for k, v in style.items():
        if v["severity"] in ("high", "warn"):
            add(v["severity"], f"style_{k}",
                f"Feature '{k}' alone separates the classes (AUC={v['auc_as_single_feature']}).",
                "A formatting/length artefact of how the dataset was collected may be learned.")
    drop = f1["full_text"] - f1["markers_neutralized"]
    if drop >= 0.03:
        add("high" if drop >= 0.08 else "warn", "performance_depends_on_markers",
            f"Macro-F1 falls by {drop:.3f} when explicit markers are neutralised "
            f"({f1['full_text']:.3f} -> {f1['markers_neutralized']:.3f}).",
            "Part of the baseline score is explained by label/source markers, not content risk.")
    drop2 = f1["full_text"] - f1["markers_neutralized_and_dateline_stripped"]
    if drop2 - drop >= 0.03:
        add("warn", "performance_depends_on_dateline",
            f"Stripping the dateline lowers macro-F1 by a further {drop2 - drop:.3f}.",
            "Dateline/style (e.g. 'JAKARTA,') acts as a source fingerprint.")
    for name in ("first_20_words_only", "last_20_words_only"):
        if f1[name] >= 0.9 * f1["full_text"] and f1["full_text"] > f1["majority_baseline"] + 0.05:
            add("warn", f"shortcut_{name}",
                f"Only the {name.split('_')[0]} 20 words reach macro-F1={f1[name]:.3f} "
                f"(full text {f1['full_text']:.3f}).",
                "Signal concentrated at the text edges suggests boilerplate/dateline rather than content risk.")
    if f1["length_only_logreg"] > f1["majority_baseline"] + 0.08:
        add("warn", "length_alone_predicts",
            f"Article length alone gives macro-F1={f1['length_only_logreg']:.3f} "
            f"(majority baseline {f1['majority_baseline']:.3f}).",
            "Length is a collection artefact, not a hoax signal.")
    if dup["groups_with_more_than_one_row"]:
        add("info" if not dup["groups_with_conflicting_labels"] else "high", "near_duplicates",
            f"{dup['rows_in_multi_row_groups']} rows belong to {dup['groups_with_more_than_one_row']} "
            f"near-duplicate groups (cosine >= {near_threshold}); conflicting-label groups: "
            f"{dup['groups_with_conflicting_labels']}.",
            "Handled by group-aware splits so near-duplicates never straddle train/test.")
    if f1["full_text"] - f1["majority_baseline"] < 0.05:
        add("warn", "no_gain_over_majority",
            "The model barely beats the majority-class baseline.", "The baseline carries little signal on this data.")
    if not findings:
        add("info", "no_obvious_leakage", "No strong shortcut detected by these checks.",
            "Absence of evidence is not evidence of absence: the checks are heuristic.")

    order = {"high": 0, "warn": 1, "info": 2}
    findings.sort(key=lambda f: order[f["severity"]])
    return {
        "marker_prevalence": markers,
        "style_shortcuts": style,
        "discriminative_tokens": tokens,
        "near_duplicate_groups": {"threshold": near_threshold, **dup},
        "shortcut_probes_cv": probes,
        "cv_setup": {"n_splits": n_splits, "n_repeats": n_repeats, "seed": seed, "grouped": True},
        "findings": findings,
    }


def render_leakage_md(dataset_id: str, status: str, rep: dict) -> str:
    L = [f"# Leakage report — `{dataset_id}`", ""]
    if status != "final":
        L += ["> **TEMPORARY DATASET — pipeline testing only. Not a final result.**", ""]
    L += ["## Findings", "", "| Severity | Code | Finding | Impact |", "|---|---|---|---|"]
    for f in rep["findings"]:
        L.append(f"| {f['severity']} | `{f['code']}` | {f['message']} | {f['impact']} |")
    L += ["", "## Shortcut probes (repeated grouped CV, macro-F1 mean ± std)", "",
          "| Variant | Macro-F1 | ROC-AUC |", "|---|---|---|"]
    for k, v in rep["shortcut_probes_cv"].items():
        m = v["macro_f1"]
        auc = f"{v['roc_auc']['mean']:.3f}" if v.get("roc_auc") else "-"
        L.append(f"| {k} | {m['mean']:.3f} ± {m['std']:.3f} | {auc} |")
    L += ["", "## Marker prevalence (share of rows containing the marker)", "",
          "| Marker group | positive | negative | gap | Fisher p | severity | top matches (pos / neg) |", "|---|---|---|---|---|---|---|"]
    for k, v in rep["marker_prevalence"].items():
        L.append(f"| {k} | {v['share_positive']:.0%} | {v['share_negative']:.0%} | {v['gap']:.2f} | "
                 f"{v['fisher_p']:.2g} | {v['severity']} | {v['top_matches_positive'][:3]} / {v['top_matches_negative'][:3]} |")
    L += ["", "## Single-feature style shortcuts (ROC-AUC of the feature alone)", "",
          "| Feature | AUC | median pos | median neg | severity |", "|---|---|---|---|---|"]
    for k, v in rep["style_shortcuts"].items():
        L.append(f"| {k} | {v['auc_as_single_feature']} | {v['median_positive']:.3g} | {v['median_negative']:.3g} | {v['severity']} |")
    t = rep["discriminative_tokens"]
    L += ["", "## Most discriminative tokens (smoothed log-odds)", "",
          f"- Positive class: {', '.join(t['top_positive_class'])}",
          f"- Negative class: {', '.join(t['top_negative_class'])}",
          f"- Flagged as source/verdict markers: {t['flagged_as_marker_positive'] + t['flagged_as_marker_negative'] or 'none'}",
          "", f"_{t['note']}_", ""]
    d = rep["near_duplicate_groups"]
    L += ["## Near-duplicates", "",
          f"Threshold {d['threshold']}: {d['groups_with_more_than_one_row']} multi-row groups "
          f"({d['rows_in_multi_row_groups']} rows), conflicting-label groups: {d['groups_with_conflicting_labels']}.", ""]
    return "\n".join(L)
