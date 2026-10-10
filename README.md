# 🔍 HOAX-DETECTOR MELALUI NLI

> **Dibuat oleh Tim:**  
> - **Vincent**  
> - **Leonardo Angkasa**  
> - **Giliant Chandra**  
> 
> *Proyek Sistem Deteksi & Verifikasi Hoax Cerdas berbasis **Natural Language Inference (NLI)** dengan Bahasa Pemrograman **Python** dan Ekosistem Client Android Native (Kotlin Jetpack Compose).*

---

## 📌 Ringkasan Proyek & Metodologi NLI

Proyek ini berjudul **HOAX-DETECTOR MELALUI NLI**, yaitu sistem deteksi hoaks yang memanfaatkan metode **Natural Language Inference (NLI)** (Premis vs Hipotesis) yang diimplementasikan secara penuh dalam **Python**.

### Konsep Kerja Natural Language Inference (NLI):
1. **Hypothesis ($H$):** Teks klaim atau berita yang ingin diuji kebenarannya.
2. **Premise ($P$):** Fakta tervalidasi dari repositori cek fakta terpercaya (MAFINDO / TurnBackHoax.id, CekFakta, media terverifikasi Dewan Pers, dan GDELT).
3. **NLI Stance Classification:**
   - **Contradiction ($H$ bertentangan dengan $P$):** Terindikasi **HOAX / Menyesatkan / Bertentangan dengan Bukti**.
   - **Entailment ($H$ sejalan dengan $P$):** Terindikasi **Fakta Valid / Didukung Bukti**.
   - **Neutral ($H$ tidak memiliki korelasi langsung dengan $P$):** **Belum Dapat Diverifikasi**.

> [!NOTE]
> Sistem ini **tidak menggunakan Gemini API / LLM generatif**, melainkan murni algoritma **Natural Language Inference (NLI) & Machine Learning** di Python.

---

## 📂 Struktur Repositori

```
HOAX---DETECTOR/
├── 📱 android/              # Aplikasi Android Client (Kotlin + Jetpack Compose + Retrofit + Room DB)
├── ⚙️ backend/              # Core NLI Algorithm & Server (Python FastAPI):
│   ├── api/                 # - FastAPI Server (Port 8000: /api/factcheck/text & /api/factcheck/url)
│   ├── verifier/            # - Python NLI Stance Classifier (Premise-Hypothesis Engine & Multi-Source Scraper)
│   └── ml/                  # - Machine Learning pipeline (TF-IDF + SVM Model)
├── 💻 frontend/             # Web Dashboard (React + Vite + Tailwind CSS)
├── 📦 HoaxDetector-v1.0.apk  # APK Android siap install langsung di smartphone
├── 🎨 design.fig            # Master Desain UI/UX Figma
├── 🖼️ figma_thumbnail.png   # Screenshot preview desain Figma
├── 🚀 buka-di-hp.bat        # Skrip otomatis jalankan di HP Android via ADB
├── ⚡ start-backend.bat     # Skrip otomatis jalankan backend server Python NLI (Port 8000)
└── 📄 README.md             # Dokumentasi lengkap proyek
```

---

## 📱 1. Fitur Aplikasi Android (Client UI)

| Fitur | Deskripsi |
|---|---|
| **Antarmuka Modern** | Jetpack Compose + Material Design 3 dengan tema gelap/terang |
| **Pemeriksaan Teks & URL** | Kirim klaim atau tautan berita ke backend Python NLI |
| **Hasil Inferensi NLI** | Menampilkan status bukti, persentase kepercayaan (*confidence*), dan penjelasan semantik |
| **Riwayat Lokal (Room DB)** | Tersimpan permanen di HP, dilengkapi pencarian dan filter |
| **Pusat Edukasi Literasi** | Panduan mengenali hoaks, clickbait, dan verifikasi sumber |

---

## ⚙️ 2. Arsitektur & Teknologi

### 📱 Android Client
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose + Material Design 3
- **Architecture**: MVVM + Clean Architecture + Repository Pattern
- **Local DB**: Room Database (SQLite) untuk caching riwayat pemeriksaan
- **Network**: Retrofit 2 + OkHttp (komunikasi ke Python FastAPI)

### ⚙️ Backend Algorithm (Python)
- **Framework**: FastAPI + Uvicorn (REST API Port 8000)
- **Algorithm**: Natural Language Inference (NLI) Stance Classification
- **Knowledge Base & Retrieval**: MAFINDO (TurnBackHoax.id), CekFakta, GDELT API
- **Semantic Ranking**: Cosine Similarity & Relevance Filtering
- **Aggregation**: Evidence Scoring (Support vs Contradiction)

---

## 🚀 Panduan Menjalankan

### Cara 1: Menjalankan Backend Python NLI
Cukup klik dua kali file **`start-backend.bat`** atau jalankan perintah berikut di terminal:
```bash
cd backend
python -m venv venv
venv\Scripts\activate   # Windows
pip install -r api/requirements.txt
pip install -e ml/
pip install -e verifier/
uvicorn api.app.main:app --host 0.0.0.0 --port 8000 --reload
```
Server FastAPI akan aktif di `http://localhost:8000`.

---

### Cara 2: Menjalankan Aplikasi Android di HP
1. Install file [`HoaxDetector-v1.0.apk`](./HoaxDetector-v1.0.apk) di smartphone Android.
2. Pastikan HP terhubung ke jaringan yang sama dengan komputer server, atau jalankan via Android Studio.

---

## 👥 Tim Pengembang
- **Vincent**
- **Leonardo Angkasa**
- **Giliant Chandra**
