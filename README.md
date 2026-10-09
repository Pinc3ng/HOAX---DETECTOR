# 🔍 Hoax Detector — Multi-Platform Fact-Checking System

> **Dibuat oleh Tim:**  
> - **Vincent**  
> - **Leonardo Angkasa**  
> - **Giliant Chandra**  
> 
> *Proyek Sistem Deteksi & Verifikasi Hoax Cerdas berbasis Machine Learning, Natural Language Inference (NLI), dan Generative AI.*

---

## 📂 Struktur Repositori

Proyek ini mencakup seluruh ekosistem aplikasi pendeteksi hoax dari mobile, web dashboard, hingga backend AI:

```
HOAX---DETECTOR/
├── 📱 android/              # Aplikasi Android Native (Kotlin + Jetpack Compose + Room DB)
├── ⚙️ backend/              # Dual Backend:
│   ├── server.js            # - Node.js Express (Gemini 1.5 Flash AI Fact-Checking Engine)
│   ├── api/                 # - Python FastAPI Server (REST API port 8000)
│   ├── ml/                  # - Machine Learning pipeline (TF-IDF + SVM Model BRIN/Mafindo)
│   └── verifier/            # - Fact Verifier (GDELT API, RSS Feeds, NLI Stance Classifier)
├── 💻 frontend/             # Web Dashboard (React + Vite + Tailwind CSS)
├── 📦 HoaxDetector-v1.0.apk  # APK Android siap install langsung di smartphone
├── 🎨 design.fig            # Master Desain UI/UX Figma
├── 🖼️ figma_thumbnail.png   # Screenshot preview desain Figma
├── 🚀 buka-di-hp.bat        # Skrip otomatis jalankan di HP Android via ADB
├── ⚡ start-backend.bat     # Skrip otomatis jalankan server backend
└── 📄 README.md             # Dokumentasi lengkap proyek
```

---

## 📱 1. Fitur Aplikasi Android

| Fitur | Deskripsi |
|---|---|
| **Sistem Autentikasi** | Login, Registrasi akun, atau Masuk Mode Tamu (*Guest*) |
| **Statistik Dinamis** | Total Pemeriksaan, Didukung Bukti, dan Menyesatkan (0 untuk pengguna baru) |
| **Dual-Engine Fact Check** | Analisis online via Gemini AI & analisis offline via On-Device Fallback Engine |
| **Pemeriksaan Teks & URL** | Periksa klaim berita lewat teks bebas atau salin link artikel berita |
| **Riwayat Lokal (Room DB)** | Tersimpan permanen di HP, dilengkapi fitur pencarian, filter, dan hapus |
| **Pusat Edukasi Interaktif** | 4 modul panduan literasi media (Clickbait, Domain/Tanggal, Verifikasi, dll.) |
| **Pengaturan & Profil** | Kelola akun, tema aplikasi, dan tes konektivitas backend |

---

## ⚙️ 2. Arsitektur & Teknologi

### 📱 Android Application
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose + Material Design 3
- **Architecture**: MVVM + Clean Architecture + Repository Pattern
- **Local DB**: Room Database (SQLite) v2
- **Network**: Retrofit 2 + OkHttp + Gson
- **Dependency Injection**: Hilt / Dagger
- **Fallback Engine**: On-Device `LocalFactCheckEngine.kt` (Berjalan 100% offline tanpa server)

### ⚙️ Backend Services
1. **Node.js Express Engine (`backend/`)**:
   - Google Gemini 1.5 Flash AI API
   - Cheerio HTML Article Scraper
   - Rate limiting, Helmet security, CORS support
2. **Python FastAPI & ML Engine (`backend/api`, `backend/ml`, `backend/verifier`)**:
   - TF-IDF Vectorizer + SVM (Support Vector Machine) classifier
   - GDELT Project API & Indonesian FactCheck RSS Scraper (TurnBackHoax, CekFakta, Kompas)
   - Cosine Similarity & Natural Language Inference (NLI) Stance Detection
   - Source Reliability Tiering (`source_reliability.csv`)

### 💻 Web Dashboard
- **Framework**: React 18 + Vite
- **Styling**: Tailwind CSS
- **Features**: Dashboard web untuk analisis cepat berita di browser desktop

---

## 🚀 Panduan Menjalankan

### Cara 1: Install Langsung di HP Android
1. Download file [`HoaxDetector-v1.0.apk`](./HoaxDetector-v1.0.apk) ke HP Android kamu.
2. Buka file APK dan pilih **Install**.
3. Aplikasi siap digunakan langsung (didukung oleh Mesin On-Device bahkan saat offline).

---

### Cara 2: Menjalankan Backend Node.js
```bash
cd backend
npm install
# Buat file .env dari template
cp .env.example .env
# Masukkan API Key Gemini kamu di .env
npm start
```
Server akan berjalan di `http://localhost:3000`.

---

### Cara 3: Menjalankan Backend Python FastAPI & ML
```bash
cd backend
python -m venv venv
venv\Scripts\activate   # Windows (atau source venv/bin/activate di Linux/Mac)
pip install -r api/requirements.txt
pip install -e ml/
pip install -e verifier/
uvicorn api.app.main:app --host 0.0.0.0 --port 8000 --reload
```
Server FastAPI akan berjalan di `http://localhost:8000`.

---

### Cara 4: Menjalankan Web Dashboard
```bash
cd frontend
npm install
npm run dev
```
Buka browser di `http://localhost:5173`.

---

### Cara 5: Membuka & Mengembangkan Android di Android Studio
1. Buka folder `android/` di Android Studio.
2. Tunggu Gradle sync selesai.
3. Jalankan ke Emulator atau HP fisik Android.

---

## 👥 Tim Pengembang
- **Vincent**
- **Leonardo Angkasa**
- **Giliant Chandra**
