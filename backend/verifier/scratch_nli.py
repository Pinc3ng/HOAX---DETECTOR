import time
import sys
import psutil
import os
from transformers import pipeline

def measure_memory():
    process = psutil.Process(os.getpid())
    return process.memory_info().rss / 1024 / 1024 # MB

print(f"Memory before load: {measure_memory():.1f} MB")

t0 = time.time()
model_name = "MoritzLaurer/mDeBERTa-v3-base-xnli-multilingual-nli-2mil7"
try:
    classifier = pipeline("text-classification", model=model_name, device=-1, top_k=None)
except TypeError:
    # Older transformers fallback
    classifier = pipeline("text-classification", model=model_name, device=-1, return_all_scores=True)
load_time = time.time() - t0
print(f"Model loading time: {load_time:.2f}s")
print(f"Memory after load: {measure_memory():.1f} MB")

claim = "Bawang putih dapat menyembuhkan COVID-19."
evidences = [
    # Supporting
    "Bawang putih mentah terbukti ampuh membunuh virus corona dan dapat menyembuhkan COVID-19 secara total jika dikonsumsi rutin.",
    # Contradicting
    "WHO menegaskan bahwa tidak ada bukti medis yang menunjukkan bahwa makan bawang putih dapat menyembuhkan penderita COVID-19.",
    # Unrelated
    "Resep nasi goreng spesial menggunakan 3 siung bawang putih dan tambahan kecap manis.",
]

for ev in evidences:
    print(f"\nPremise (Evidence): {ev}")
    print(f"Hypothesis (Claim): {claim}")
    
    # NLI premise and hypothesis format for huggingface pipeline
    # We can pass them as a dict or text pair depending on the pipeline
    t1 = time.time()
    result = classifier({"text": ev, "text_pair": claim})
    lat = time.time() - t1
    print(f"Latency: {lat:.3f}s")
    print(f"Output: {result}")
