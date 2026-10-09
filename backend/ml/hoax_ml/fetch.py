"""One-time download of a public Hugging Face dataset into ``data/raw/``.

Needs the optional ``fetch`` extra (``datasets``). Gated datasets require ``hf auth login``.
"""
from __future__ import annotations

import json
from datetime import datetime, timezone

import pandas as pd

from .schema import DatasetConfig


def fetch(cfg: DatasetConfig, force: bool = False) -> None:
    src = cfg.source
    if src.get("type") != "huggingface":
        raise SystemExit("Only [source].type = 'huggingface' can be fetched automatically; "
                         f"place the file manually at {cfg.raw_path}")
    if cfg.raw_path.exists() and not force:
        print(f"Already exists: {cfg.raw_path} (use --force to overwrite)")
        return
    from datasets import load_dataset  # lazy: optional dependency
    from huggingface_hub import HfApi

    repo = src["repo"]
    ds = load_dataset(repo)
    frames = []
    for split, d in ds.items():
        df = d.to_pandas()
        df["_hf_split"] = split
        frames.append(df)
    df = pd.concat(frames, ignore_index=True)
    cfg.raw_path.parent.mkdir(parents=True, exist_ok=True)
    df.to_csv(cfg.raw_path, index=False, encoding="utf-8")

    try:
        revision = HfApi().dataset_info(repo).sha
    except Exception:  # noqa: BLE001 - provenance is best-effort
        revision = None
    meta = {
        "repo": repo,
        "revision": revision,
        "fetched_at_utc": datetime.now(timezone.utc).isoformat(timespec="seconds"),
        "rows": int(len(df)),
        "columns": list(df.columns),
    }
    cfg.raw_path.with_suffix(".source.json").write_text(json.dumps(meta, indent=2), encoding="utf-8")
    print(f"Saved {len(df)} rows -> {cfg.raw_path}")
