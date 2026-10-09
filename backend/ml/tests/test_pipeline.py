import json
import shutil

import numpy as np
import pytest

from hoax_ml import predict as predict_mod
from hoax_ml.data import load_standardized, prepare
from hoax_ml.evaluate import compute_metrics, holdout_split
from hoax_ml.groups import near_duplicate_groups
from hoax_ml.predict import predict_content_risk
from hoax_ml.train import train


def test_near_duplicates_grouped_and_never_split():
    import random

    rnd = random.Random(3)
    vocab = [f"kata{i}" for i in range(400)]
    texts, y = [], []
    for topic in range(12):  # 12 distinct topics, each present as a near-duplicate pair
        words = rnd.sample(vocab, 25)
        texts += [" ".join(words), " ".join(words) + " tambahan"]
        y += [topic % 2, topic % 2]
    y = np.array(y)
    groups = near_duplicate_groups(texts, 0.8)
    assert len(set(groups)) == 12                       # each pair collapsed into one group
    tr, te = holdout_split(y, groups, 0.25, seed=1)
    assert not set(groups[tr]) & set(groups[te])


def test_metrics_are_consistent():
    y = np.array([0, 0, 1, 1])
    m = compute_metrics(y, np.array([0.1, 0.4, 0.6, 0.9]))
    assert m["accuracy"] == 1.0 and m["macro_f1"] == 1.0 and m["roc_auc"] == 1.0
    assert m["confusion_matrix"] == {"tn": 2, "fp": 0, "fn": 0, "tp": 2}


@pytest.fixture
def trained(synthetic_cfg):
    result = train(synthetic_cfg)
    return synthetic_cfg, result


def test_train_is_reproducible_and_beats_majority(synthetic_cfg):
    r1 = train(synthetic_cfg)
    r2 = train(synthetic_cfg)
    assert r1["holdout"]["test"]["macro_f1"] == r2["holdout"]["test"]["macro_f1"]
    maj = r1["ablation"]["majority_baseline"]["macro_f1"]["mean"]
    word = r1["ablation"]["word_tfidf_1_2"]["macro_f1"]["mean"]
    assert word > maj + 0.2


def test_artifacts_and_metadata(trained, tmp_path):
    cfg, result = trained
    d = tmp_path / "models" / cfg.dataset_id
    assert {p.name for p in d.iterdir()} == {"tfidf_vectorizer.joblib", "calibrated_svm.joblib", "model_metadata.json"}
    meta = json.loads((d / "model_metadata.json").read_text(encoding="utf-8"))
    for key in ("model_version", "dataset", "preprocessing", "model_type", "tfidf", "trained_at_utc", "seed",
                "metrics", "artifacts", "environment"):
        assert key in meta
    assert meta["dataset"]["is_temporary"] is True
    assert meta["model_reliability"] == "baseline"
    assert (cfg.reports_dir / "evaluation_report.md").exists()
    assert "TEMPORARY" in (cfg.reports_dir / "evaluation_report.md").read_text(encoding="utf-8")


def test_predict_output_contract(trained):
    text = "viral sebarkan heboh rahasia konspirasi bocoran terbongkar warga panik ancaman " * 3
    out = predict_content_risk(text)
    assert set(out) >= {"risk_label", "risk_score", "model_version", "model_reliability"}
    assert 0.0 <= out["risk_score"] <= 1.0
    assert out["model_reliability"] == "baseline"
    assert out["training_dataset_status"] == "temporary"
    assert not {"is_hoax", "is_true", "is_fake"} & set(out)
    calm = predict_content_risk("pemerintah menteri rapat anggaran laporan resmi kebijakan daerah " * 3)
    assert out["risk_score"] > calm["risk_score"]


def test_predict_short_text_is_insufficient(trained):
    out = predict_content_risk("terlalu pendek")
    assert out["risk_label"] == "insufficient_text" and out["risk_score"] is None


def test_predict_refuses_tampered_artifact(trained, tmp_path):
    cfg, _ = trained
    d = tmp_path / "models" / cfg.dataset_id
    (d / "calibrated_svm.joblib").write_bytes(b"tampered")
    predict_mod._load.cache_clear()
    with pytest.raises(RuntimeError):
        predict_content_risk("viral sebarkan heboh rahasia konspirasi bocoran " * 4)


def test_predict_requires_metadata(trained, tmp_path):
    cfg, _ = trained
    d = tmp_path / "models" / cfg.dataset_id
    (d / "model_metadata.json").unlink()
    predict_mod._load.cache_clear()
    with pytest.raises(predict_mod.ModelNotFoundError):
        predict_content_risk("viral sebarkan heboh rahasia konspirasi bocoran " * 4)


def test_pipeline_does_not_depend_on_dataset_name(synthetic_cfg, tmp_path):
    # a second dataset with different column names/labels runs through the same code path
    import pandas as pd

    df = pd.read_csv(synthetic_cfg.raw_path).rename(columns={"text": "isi", "tag": "kelas", "idx": "no"})
    df["kelas"] = df["kelas"].map({"Bad": "HOAX", "Good": "NON-HOAX"})
    df.to_csv(synthetic_cfg.raw_path.with_name("other.csv"), index=False)
    toml = synthetic_cfg.config_path.read_text(encoding="utf-8")
    toml = (toml.replace('synthetic_test', 'other_ds').replace('raw.csv', 'other.csv')
            .replace('"text"', '"isi"').replace('"tag"', '"kelas"').replace('"idx"', '"no"')
            .replace('Bad = 1', 'HOAX = 1').replace('Good = 0', 'NON-HOAX = 0'))
    p = synthetic_cfg.config_path.with_name("other.toml")
    p.write_text(toml, encoding="utf-8")
    from hoax_ml.schema import DatasetConfig

    cfg2 = DatasetConfig.from_toml(p)
    df2, _ = prepare(load_standardized(cfg2), cfg2)
    assert len(df2) == 160 and set(df2["label"]) == {0, 1}
    shutil.rmtree(cfg2.reports_dir, ignore_errors=True)
