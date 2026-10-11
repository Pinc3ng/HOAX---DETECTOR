# 🚀 Panduan Deploy Backend ke Cloud (Online 24/7 Gratis)

Agar aplikasi Android dan Web Hoax Detector Anda **bisa digunakan kapan saja meskipun laptop Anda dalam keadaan SHUTDOWN / MATI**, backend Python FastAPI ini perlu dijalankan di server cloud online.

Semua file konfigurasi sudah dibuatkan secara otomatis:
* `render.yaml` — Konfigurasi otomatis untuk Render.com (Paling Direkomendasikan)
* `backend/requirements.txt` & `requirements.txt` — Daftar dependensi Python
* `Dockerfile` & `.dockerignore` — Untuk Hugging Face Spaces / Docker / VPS
* `backend/Procfile` — Konfigurasi web dyno

---

## 🌟 Cara 1: Deploy ke Render.com (Paling Mudah & Gratis)

Render menyediakan Web Service gratis yang terhubung langsung ke repositori GitHub Anda:

1. **Push perubahan terbaru ke GitHub Anda:**
   Buka terminal di VS Code / folder proyek dan jalankan:
   ```bash
   git add .
   git commit -m "Siapkan konfigurasi cloud deployment"
   git push origin main
   ```

2. **Daftar / Masuk ke [Render.com](https://render.com):**
   * Login menggunakan akun GitHub Anda (`Pinc3ng`).

3. **Buat Web Service Baru:**
   * Klik tombol **"New +"** di pojok kanan atas -> pilih **"Web Service"**.
   * Pilih repositori **`HOAX---DETECTOR`** dari daftar repositori GitHub Anda.
   * Render akan otomatis mendeteksi file `render.yaml`, atau Anda cukup isi:
     * **Name:** `hoax-detector-api`
     * **Root Directory:** `backend`
     * **Runtime:** `Python 3`
     * **Build Command:** `pip install -r requirements.txt`
     * **Start Command:** `uvicorn api.app.main:app --host 0.0.0.0 --port $PORT`
     * **Instance Type:** `Free`
   * Klik **"Deploy Web Service"**.

4. **Dapatkan URL Online Anda:**
   * Tunggu sekitar 2-3 menit sampai build selesai (status menjadi **Live**).
   * Anda akan mendapatkan link HTTPS gratis, contohnya:
     `https://hoax-detector-api-xxxx.onrender.com`

---

## 🌟 Cara 2: Deploy ke Hugging Face Spaces (Gratis Selamanya, 16GB RAM)

Jika menyukai ekosistem AI Hugging Face:
1. Buka [huggingface.co/spaces](https://huggingface.co/spaces) -> klik **"Create new Space"**.
2. Beri nama space: `hoax-detector`.
3. Pilih **Space SDK:** **Docker** (Blank).
4. Hubungkan repositori GitHub atau push file ke Space tersebut. Space akan membaca `Dockerfile` yang sudah kami sediakan dan langsung aktif 24/7!

---

## 📱 Cara Menghubungkan ke Aplikasi Android di HP

Setelah mendapatkan URL dari Render (misal: `https://hoax-detector-api-xxxx.onrender.com`):

1. **Lewat Aplikasi Langsung (Tanpa Perlu Kompilasi Ulang):**
   * Buka aplikasi di HP (atau browser HP).
   * Masuk ke tab **Profil / Pengaturan** (ikon gear di kanan bawah).
   * Pada kartu **"Alamat Server Backend (Cloud / Online)"**, masukkan URL Render Anda.
   * Klik tombol **"Simpan"**.
   * Aplikasi akan otomatis tersambung ke server cloud!

2. **Laptop Sekarang Bebas Dimatikan / Shutdown!**
   * Karena backend sudah berjalan di cloud 24/7, Anda bisa mematikan komputer kapan saja.
   * Aplikasi di HP tetap bisa mendeteksi hoaks secara real-time kapan pun dan di mana pun!
