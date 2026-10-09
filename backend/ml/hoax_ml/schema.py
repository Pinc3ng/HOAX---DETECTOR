"""Dataset-agnostic interface.

Any dataset (temporary or BRIN/MAFINDO final) is described by a TOML config that
maps its columns onto the standard schema ``id, title, content, label``.
The rest of the pipeline only ever sees the standard schema.
"""
from __future__ import annotations

import tomllib
from dataclasses import dataclass, field
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
MODELS_DIR = REPO_ROOT / "models"
REPORTS_DIR = REPO_ROOT / "reports"

# Label semantics: 1 = text pattern flagged as problematic in the source dataset
# (e.g. HOAX), 0 = not flagged (e.g. VALID / NON-HOAX). This is a *dataset label*,
# not a claim that the content is objectively false.
POSITIVE_LABEL = 1
NEGATIVE_LABEL = 0
STANDARD_COLUMNS = ("id", "title", "content", "label")
VALID_STATUS = ("temporary", "final")


@dataclass(frozen=True)
class DatasetConfig:
    dataset_id: str
    version: str
    status: str  # "temporary" | "final"
    raw_path: Path
    notes: str
    source: dict
    columns: dict  # standard column -> source column name (or None)
    label_map: dict  # raw label (str) -> 0/1
    cleaning: dict = field(default_factory=dict)
    model: dict = field(default_factory=dict)
    config_path: Path | None = None

    @property
    def is_temporary(self) -> bool:
        return self.status != "final"

    @property
    def reports_dir(self) -> Path:
        return REPORTS_DIR / self.dataset_id

    @classmethod
    def from_toml(cls, path: str | Path) -> "DatasetConfig":
        path = Path(path)
        if not path.is_absolute() and not path.exists():
            path = REPO_ROOT / path
        data = tomllib.loads(path.read_text(encoding="utf-8"))

        status = data.get("status", "temporary")
        if status not in VALID_STATUS:
            raise ValueError(f"status must be one of {VALID_STATUS}, got {status!r}")

        raw_path = Path(data["raw_path"])
        if not raw_path.is_absolute():
            raw_path = REPO_ROOT / raw_path

        columns = {k: (v or None) for k, v in data.get("columns", {}).items()}
        for required in ("content", "label"):
            if not columns.get(required):
                raise ValueError(f"[columns].{required} must be mapped in {path.name}")

        label_map = {str(k).strip(): int(v) for k, v in data.get("label_map", {}).items()}
        if not label_map or not set(label_map.values()) <= {NEGATIVE_LABEL, POSITIVE_LABEL}:
            raise ValueError("[label_map] must map raw labels onto {0, 1}")
        if set(label_map.values()) != {NEGATIVE_LABEL, POSITIVE_LABEL}:
            raise ValueError("[label_map] must contain both classes (0 and 1)")

        return cls(
            dataset_id=data["dataset_id"],
            version=str(data.get("version", "unversioned")),
            status=status,
            raw_path=raw_path,
            notes=data.get("notes", ""),
            source=data.get("source", {}),
            columns=columns,
            label_map=label_map,
            cleaning=data.get("cleaning", {}),
            model=data.get("model", {}),
            config_path=path,
        )
