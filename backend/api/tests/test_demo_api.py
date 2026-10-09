from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)

def test_demo_api_empty_text():
    response = client.post("/api/v1/demo/analyze", json={"text": ""})
    # Empty string is technically a valid string, so it should return 200 with INSUFFICIENT evidence
    assert response.status_code == 200
    data = response.json()
    assert data["evidence_coverage"] == "INSUFFICIENT"

def test_demo_api_valid_claim():
    # We shouldn't hit the real NLI model in an automated API test because it takes seconds and relies on real API.
    # But since it's just a demo endpoint, we can test it with a short query that returns no results quickly.
    # Actually, GDELT/RSS might take 1 second.
    response = client.post("/api/v1/demo/analyze", json={"text": "Klaim acak yang tidak mungkin ada beritanya 123456789"})
    assert response.status_code == 200
    data = response.json()
    assert "evidence_coverage" in data
    assert data["evidence_coverage"] == "INSUFFICIENT"
    assert data["support_score"] == 0.0
