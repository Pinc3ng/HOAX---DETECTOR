# Architecture Decision Records

Dasar: *System Design Proposal v1* (disetujui 2026-10-03). Dokumen ini mencatat keputusan yang mengikat implementasi.

## ADR-001 — Posisi sistem
Sistem adalah **decision-support**, bukan penentu kebenaran. Output: Trust Score + label + bukti + alasan. Istilah wajib: "indikasi", "perlu diverifikasi", "tingkat kepercayaan". Status khusus **"Bukti belum cukup"** (tidak ada bukti ≠ hoaks).

## ADR-002 — Komponen penentu hasil (core, deterministik)
TF-IDF + SVM (terkalibrasi) → real-time evidence retrieval → embedding similarity (relevansi) → NLI (stance) → source reliability → **fuzzy hierarkis (Mamdani)**.
**Trust Score & status verifikasi hanya dihasilkan oleh fuzzy engine.**

## ADR-003 — Kebijakan LLM (auxiliary, opsional)
| Boleh | Tidak boleh |
|---|---|
| Ekstraksi klaim (opsional) | Menentukan Trust Score / label / status |
| Penjelasan bahasa natural | Menambah fakta baru di luar indikator/evidence/fired rules |
| — | Menjadi dependensi wajib |

Aturan implementasi:
1. Sistem **harus berjalan penuh tanpa LLM**: fallback = heuristic claim extraction + template explanation.
2. Penjelasan LLM **grounded**: input hanya JSON hasil sistem (indikator, evidence snippets, fired rules); output divalidasi (tidak boleh memuat angka/entitas/URL yang tidak ada di input; bila gagal validasi → pakai template).
3. Teks artikel/evidence = data tidak tepercaya (anti prompt-injection); tidak dieksekusi sebagai instruksi.
4. Interface `ClaimExtractor` & `ExplanationGenerator` dengan implementasi `Heuristic/Template` (default) dan `LLM` (flag `LLM_ENABLED`).
5. Model & prompt version dicatat di `analysis_results`.

## ADR-004 — Environment
| Aspek | Keputusan |
|---|---|
| Eksperimen NLI/IndoBERT | Google Colab (GPU). Evaluasi **pretrained dulu**; fine-tuning IndoBERT hanya jika baseline NLI belum memenuhi target |
| Development | Laptop (CPU): backend, app, pipeline |
| Deploy primer | **Docker Compose lokal** (frontend, FastAPI, ML/NLP, DB) — reproducible, memenuhi Definition of Done |
| Deploy sekunder | Public hosting untuk demo akhir; **opsional**, bukan dependensi |

Catatan: model dari Colab di-*export* (artifact + versi + metrik) dan dimuat di container CPU.

## ADR-005 — Dataset
- Mulai dari dataset publik berbahasa Indonesia; **tidak digabung sembarangan**.
- Wajib **dataset audit** sebelum dipakai: struktur label, distribusi kelas, duplikat/near-duplicate, source leakage, kualitas label.
- Prioritas dataset ber-label HOAX/NON-HOAX + claim + factual evidence + reference (berguna untuk retrieval & NLI).
- Dokumentasikan: sumber, lisensi, jumlah, distribusi label, keterbatasan (lihat `docs/dataset_audit.md`).
- Final evaluation: **manually-curated test set kecil**, terpisah dari training, berisi berita/informasi lebih baru.

## ADR-006 — Versi Python
Dev lokal: Python 3.14 terdeteksi; **image Docker memakai Python 3.11/3.12** agar wheel ML (scikit-learn, torch, sentence-transformers) tersedia. Audit script dibuat kompatibel lintas versi.
