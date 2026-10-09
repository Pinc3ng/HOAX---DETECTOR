from __future__ import annotations

import asyncio
import pathlib

import httpx
import pytest

FIXTURES = pathlib.Path(__file__).parent / "fixtures"
CLAIM = "Bawang putih dapat menyembuhkan COVID-19."


def fixture_bytes(name: str) -> bytes:
    return (FIXTURES / name).read_bytes()


def run(coro):
    return asyncio.run(coro)


@pytest.fixture
def make_client():
    """httpx client backed by an in-process handler: no network is ever touched."""
    def _make(handler):
        return httpx.AsyncClient(transport=httpx.MockTransport(handler), follow_redirects=True)
    return _make


class FakeClock:
    def __init__(self, t: float = 1000.0):
        self.t = t

    def __call__(self) -> float:
        return self.t
