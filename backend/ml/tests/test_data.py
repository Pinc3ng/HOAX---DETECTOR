import pandas as pd
import pytest

from hoax_ml.data import load_standardized, prepare, validate
from hoax_ml.schema import DatasetConfig


def test_standardized_schema(synthetic_cfg):
    df = load_standardized(synthetic_cfg)
    assert {"id", "title", "content", "label", "raw_text"} <= set(df.columns)
    assert set(df["label"].unique()) == {0, 1}


def test_config_requires_both_classes(tmp_path):
    p = tmp_path / "c.toml"
    p.write_text(
        'dataset_id="x"\nraw_path="a.csv"\n[columns]\ncontent="t"\nlabel="l"\n[label_map]\nA=1\n', encoding="utf-8")
    with pytest.raises(ValueError):
        DatasetConfig.from_toml(p)


def test_prepare_logs_every_removal(synthetic_cfg):
    df = load_standardized(synthetic_cfg)
    extra = pd.DataFrame(
        [
            {"id": "dup", "title": "", "content": df.loc[0, "content"], "label_raw": "x",
             "label": df.loc[0, "label"], "raw_text": df.loc[0, "raw_text"]},                      # exact duplicate
            {"id": "conf", "title": "", "content": df.loc[1, "content"], "label_raw": "x",
             "label": 1 - df.loc[1, "label"], "raw_text": df.loc[1, "raw_text"]},                  # conflicting duplicate
            {"id": "empty", "title": "", "content": "", "label_raw": "Bad", "label": 1, "raw_text": ""},
            {"id": "unk", "title": "", "content": "abc def", "label_raw": "Maybe", "label": None, "raw_text": "abc def"},
        ]
    )
    both = pd.concat([df, extra], ignore_index=True)
    clean, log = prepare(both, synthetic_cfg)
    assert log.removed_unmapped_label == 1
    assert log.removed_empty == 1
    assert log.removed_conflicting_duplicates == 2          # both members of the conflicting pair
    assert log.removed_exact_duplicates == 1
    assert log.rows_in - (1 + 1 + 2 + 1) == log.rows_out == len(clean)
    assert "dup" in log.removed_ids["exact_duplicates"]


def test_validate_flags_temporary_and_unmapped(synthetic_cfg):
    df = load_standardized(synthetic_cfg)
    df.loc[0, "label"] = None
    rep = validate(df, synthetic_cfg)
    codes = {i["code"] for i in rep["issues"]}
    assert {"temporary_dataset", "unmapped_labels"} <= codes
    assert rep["has_blocking_error"]
