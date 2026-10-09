# HoaxDetector Python Backend & ML Pipeline

This directory contains the backend API (`api/`), machine learning models (`ml/`), and fact-checking verifier algorithms (`verifier/`) from the teammate's Hoax-Detector project.

## Project Structure
- `api/`: FastAPI backend server (`main.py`)
- `ml/`: Machine learning models, training, and prediction scripts (`hoax_ml/`)
- `verifier/`: Fact-checking evidence retrieval, NLI, and source reliability pipeline (`hoax_verifier/`)
- `data/`, `models/`, `reports/`: Datasets, trained models, and evaluation reports.

## How to Run the Backend

1. **Set up Python Virtual Environment:**
   ```bash
   python -m venv venv
   source venv/bin/activate  # On Windows: venv\Scripts\activate
   ```

2. **Install Dependencies:**
   ```bash
   pip install -r api/requirements.txt
   pip install -e ml/
   pip install -e verifier/
   ```

3. **Run the FastAPI Server:**
   ```bash
   uvicorn api.app.main:app --host 0.0.0.0 --port 3000 --reload
   ```

4. **Connect Android App:**
   Make sure your Android app (`app/build.gradle.kts`) points to your computer's local IP address running this backend server (e.g., `http://192.168.1.17:3000/`).
