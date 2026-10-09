"""CLI: python -m hoax_ml <command> --config ml/configs/<dataset>.toml"""
from __future__ import annotations

import argparse
import json
import sys

from .schema import DatasetConfig


def _cfg(args) -> DatasetConfig:
    return DatasetConfig.from_toml(args.config)


def cmd_fetch(a):
    from .fetch import fetch

    fetch(_cfg(a), force=a.force)


def cmd_validate(a):
    from .data import load_standardized, prepare, validate

    cfg = _cfg(a)
    std = load_standardized(cfg)
    rep = validate(std, cfg)
    _, log = prepare(std, cfg)
    rep["prepare_log"] = log.as_dict()
    cfg.reports_dir.mkdir(parents=True, exist_ok=True)
    (cfg.reports_dir / "validation_report.json").write_text(json.dumps(rep, indent=2, ensure_ascii=False), encoding="utf-8")
    print(json.dumps({k: v for k, v in rep.items() if k != "prepare_log"}, indent=2, ensure_ascii=False))
    print("prepare_log:", {k: v for k, v in log.as_dict().items() if k != "removed_ids"})


def cmd_leakage(a):
    from .data import load_standardized, prepare
    from .groups import near_duplicate_groups
    from .leakage import render_leakage_md, run_leakage_check
    from .model import ModelParams

    cfg = _cfg(a)
    df, _ = prepare(load_standardized(cfg), cfg)
    m = cfg.model
    thr = float(m.get("near_duplicate_threshold", 0.9))
    groups = near_duplicate_groups(df["raw_text"].tolist(), thr)
    rep = run_leakage_check(df, groups, ModelParams(), thr, int(m.get("cv_splits", 5)),
                            int(m.get("cv_repeats", 3)), int(m.get("seed", 42)))
    cfg.reports_dir.mkdir(parents=True, exist_ok=True)
    (cfg.reports_dir / "leakage_report.json").write_text(json.dumps(rep, indent=2, ensure_ascii=False, default=str), encoding="utf-8")
    (cfg.reports_dir / "leakage_report.md").write_text(render_leakage_md(cfg.dataset_id, cfg.status, rep), encoding="utf-8")
    for f in rep["findings"]:
        print(f"[{f['severity']}] {f['code']}: {f['message']}")
    print(f"Report -> {cfg.reports_dir / 'leakage_report.md'}")


def cmd_train(a):
    from .train import train

    train(_cfg(a), fit_on=a.fit_on, skip_ablation=a.skip_ablation)


def cmd_all(a):
    cmd_validate(a)
    cmd_leakage(a)
    cmd_train(a)


def cmd_predict(a):
    from .predict import predict_content_risk

    text = a.text if a.text else sys.stdin.read()
    print(json.dumps(predict_content_risk(text, title=a.title, model_dir=a.model_dir), indent=2, ensure_ascii=False))


def main(argv=None):
    ap = argparse.ArgumentParser(prog="hoax_ml", description=__doc__)
    sub = ap.add_subparsers(dest="cmd", required=True)

    def with_cfg(name, fn, help_):
        p = sub.add_parser(name, help=help_)
        p.add_argument("--config", required=True)
        p.set_defaults(fn=fn)
        return p

    with_cfg("fetch", cmd_fetch, "download a public dataset into data/raw").add_argument("--force", action="store_true")
    with_cfg("validate", cmd_validate, "validate dataset and write validation_report.json")
    with_cfg("leakage", cmd_leakage, "leakage / shortcut diagnostics (report only, never edits data)")
    t = with_cfg("train", cmd_train, "train, evaluate, save artifact + metadata")
    t.add_argument("--fit-on", choices=["train", "all"], default="train")
    t.add_argument("--skip-ablation", action="store_true")
    t2 = with_cfg("all", cmd_all, "validate -> leakage -> train")
    t2.add_argument("--fit-on", choices=["train", "all"], default="train")
    t2.add_argument("--skip-ablation", action="store_true")

    p = sub.add_parser("predict", help="score a text with the latest model")
    p.add_argument("text", nargs="?")
    p.add_argument("--title")
    p.add_argument("--model-dir")
    p.set_defaults(fn=cmd_predict)

    args = ap.parse_args(argv)
    args.fn(args)


if __name__ == "__main__":
    main()
