from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from typing import Optional

from hoax_ml.predict import predict_content_risk, ModelNotFoundError

app = FastAPI(
    title="Smart News & Information Verifier API",
    description="Digital Trust Assistant API to assess content risk and provide trust scores.",
    version="0.1.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Allows all origins for development
    allow_credentials=True,
    allow_methods=["*"],  # Allows all methods
    allow_headers=["*"],  # Allows all headers
)

class AnalyzeRequest(BaseModel):
    title: Optional[str] = Field(None, description="Optional title of the news or article")
    text: str = Field(..., description="The main content text to analyze")

class RiskResponse(BaseModel):
    signal: str
    model_version: str
    model_reliability: str
    training_dataset_status: str
    notice: str
    risk_label: str
    risk_score: Optional[float] = None
    reason: Optional[str] = None

@app.get("/")
def read_root():
    return {"message": "Welcome to Smart News & Information Verifier API"}

@app.post("/api/analyze/text", response_model=RiskResponse)
def analyze_text(req: AnalyzeRequest):
    """
    Analyze textual content and return a linguistic risk signal.
    """
    try:
        # Panggil fungsi predict dari package hoax_ml
        result = predict_content_risk(text=req.text, title=req.title)
        return result
    except ModelNotFoundError as e:
        raise HTTPException(status_code=503, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Internal Server Error: {str(e)}")

# ==============================================================================
# DEMO ENDPOINT UNTUK UTS (STEP 3 - 6 PIPELINE)
# ==============================================================================

class DemoAnalyzeRequest(BaseModel):
    text: str = Field(..., description="Claim text to verify")

@app.post("/api/v1/demo/analyze")
async def demo_analyze(req: DemoAnalyzeRequest):
    from hoax_verifier.schemas import Claim
    from hoax_verifier.evidence import EvidenceRetriever
    from hoax_verifier.relevance import SemanticRanker
    from hoax_verifier.nli import StanceClassifier
    from hoax_verifier.aggregation import EvidenceAggregator

    claim = Claim(text=req.text)
    
    try:
        # STEP 3: Retrieval
        async with EvidenceRetriever.default() as retriever:
            retrieval_result = await retriever.retrieve(claim)
            
        if not retrieval_result.items:
            return {
                "claim": claim.text,
                "evidence_coverage": "INSUFFICIENT",
                "support_score": 0.0,
                "contradiction_score": 0.0,
                "independent_evidence_count": 0,
                "conflicting_evidence": False,
                "supporting_evidence": [],
                "contradicting_evidence": [],
                "neutral_evidence": []
            }
            
        # STEP 4: Relevance
        ranker = SemanticRanker()
        ranked_items = ranker.rank(claim, retrieval_result.items)
        top_items = ranked_items[:10]
        
        # STEP 5: NLI
        nli = StanceClassifier()
        analyzed_items = nli.classify(claim, top_items)
        
        # STEP 6: Aggregation
        aggregator = EvidenceAggregator(relevance_threshold=0.2)
        agg_result = aggregator.aggregate(analyzed_items)
        
        return {
            "claim": claim.text,
            "evidence_coverage": agg_result.evidence_coverage,
            "support_score": agg_result.support_score,
            "contradiction_score": agg_result.contradiction_score,
            "independent_evidence_count": agg_result.independent_evidence_count,
            "conflicting_evidence": agg_result.conflicting_evidence,
            "supporting_evidence": [item.to_dict() for item in agg_result.supporting_evidence],
            "contradicting_evidence": [item.to_dict() for item in agg_result.contradicting_evidence],
            "neutral_evidence": [item.to_dict() for item in agg_result.neutral_evidence],
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

