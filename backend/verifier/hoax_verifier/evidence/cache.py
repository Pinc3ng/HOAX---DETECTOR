"""Tiny in-memory TTL cache (no external dependency, injectable clock for tests)."""
from __future__ import annotations

import time
from collections import OrderedDict
from typing import Callable, Generic, Hashable, TypeVar

V = TypeVar("V")


class TTLCache(Generic[V]):
    def __init__(self, ttl: float, max_entries: int = 256, clock: Callable[[], float] = time.monotonic):
        self.ttl = ttl
        self.max_entries = max_entries
        self._clock = clock
        self._data: OrderedDict[Hashable, tuple[float, V]] = OrderedDict()

    def get(self, key: Hashable) -> V | None:
        entry = self._data.get(key)
        if entry is None:
            return None
        expires, value = entry
        if self._clock() >= expires:
            del self._data[key]
            return None
        return value

    def set(self, key: Hashable, value: V) -> None:
        self._data[key] = (self._clock() + self.ttl, value)
        self._data.move_to_end(key)
        while len(self._data) > self.max_entries:
            self._data.popitem(last=False)

    def __len__(self) -> int:
        return len(self._data)
