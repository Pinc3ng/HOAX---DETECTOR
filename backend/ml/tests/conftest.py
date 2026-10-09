"""Shared fixtures: a synthetic dataset + config so tests never touch real data or models/."""
import random

import pandas as pd
import pytest

from hoax_ml import predict, schema, train
from hoax_ml.schema import DatasetConfig

RISKY = "viral sebarkan heboh rahasia konspirasi bocoran mengejutkan terbongkar warga panik berbahaya ancaman".split()
CALM = "pemerintah menteri rapat anggaran laporan resmi kebijakan daerah pembangunan penelitian lembaga data".split()
COMMON = "yang di dan untuk dengan pada adalah akan telah dalam oleh tahun hari ini itu para juga".split()


def make_rows(n_per_class=80, seed=0):
    rnd = random.Random(seed)
    rows = []
    for label, vocab in ((1, RISKY), (0, CALM)):
        for i in range(n_per_class):
            # overlapping vocab + noise so the task is learnable but not trivial
            words = rnd.choices(vocab, k=14) + rnd.choices(RISKY + CALM, k=6) + rnd.choices(COMMON, k=30)
            rnd.shuffle(words)
            rows.append({"idx": f"{label}-{i}", "text": " ".join(words), "tag": "Bad" if label else "Good"})
    rnd.shuffle(rows)
    return rows


@pytest.fixture
def synthetic_cfg(tmp_path, monkeypatch):
    csv = tmp_path / "raw.csv"
    pd.DataFrame(make_rows()).to_csv(csv, index=False)
    toml = tmp_path / "cfg.toml"
    toml.write_text(
        f"""
dataset_id = "synthetic_test"
version = "t1"
status = "temporary"
raw_path = "{csv.as_posix()}"
[columns]
id = "idx"
title = ""
content = "text"
label = "tag"
[label_map]
Bad = 1
Good = 0
[cleaning]
[model]
seed = 7
test_size = 0.2
cv_splits = 3
cv_repeats = 1
""",
        encoding="utf-8",
    )
    # redirect every output location away from the real repo
    monkeypatch.setattr(schema, "MODELS_DIR", tmp_path / "models")
    monkeypatch.setattr(schema, "REPORTS_DIR", tmp_path / "reports")
    monkeypatch.setattr(train, "MODELS_DIR", tmp_path / "models")
    monkeypatch.setattr(predict, "MODELS_DIR", tmp_path / "models")
    predict._load.cache_clear()
    return DatasetConfig.from_toml(toml)
