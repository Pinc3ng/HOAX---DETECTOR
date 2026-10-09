"""Near-duplicate grouping so that near-identical articles never straddle train/test."""
from __future__ import annotations

import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer


def near_duplicate_groups(texts, threshold: float = 0.9, chunk: int = 1000) -> np.ndarray:
    """Return group ids (union-find over TF-IDF cosine >= threshold)."""
    n = len(texts)
    parent = list(range(n))

    def find(x):
        while parent[x] != x:
            parent[x] = parent[parent[x]]
            x = parent[x]
        return x

    def union(a, b):
        ra, rb = find(a), find(b)
        if ra != rb:
            parent[rb] = ra

    X = TfidfVectorizer(min_df=1, sublinear_tf=True).fit_transform(texts)  # L2-normalised rows
    X = X.tocsr()
    for start in range(0, n, chunk):
        sim = (X[start : start + chunk] @ X.T).tocoo()
        for i, j, v in zip(sim.row, sim.col, sim.data):
            gi = start + i
            if j > gi and v >= threshold:
                union(gi, j)
    roots = np.array([find(i) for i in range(n)])
    _, groups = np.unique(roots, return_inverse=True)
    return groups


def group_summary(groups: np.ndarray, y: np.ndarray) -> dict:
    sizes = np.bincount(groups)
    multi = np.where(sizes > 1)[0]
    conflict = 0
    for g in multi:
        if len(set(y[groups == g])) > 1:
            conflict += 1
    return {
        "n_groups": int(len(sizes)),
        "groups_with_more_than_one_row": int(len(multi)),
        "rows_in_multi_row_groups": int(sizes[multi].sum()) if len(multi) else 0,
        "groups_with_conflicting_labels": conflict,
        "largest_group": int(sizes.max()) if len(sizes) else 0,
    }
