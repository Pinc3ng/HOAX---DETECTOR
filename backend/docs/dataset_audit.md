# Dataset Audit

Status: **kandidat teridentifikasi, audit belum dijalankan**. Informasi di bawah berasal dari pencarian web (Okt 2026) dan dataset card; **verifikasi ulang** sebelum dikutip di laporan.

## 1. Kandidat

| # | Dataset | Sumber | Lisensi (menurut card) | Label / field | Catatan awal |
|---|---|---|---|---|---|
| 1 | `nlp-brin-id/fakenews-mafindo` | Hugging Face (BRIN), data TurnBackHoax.id 2018–2023 | MIT | `Label` (HOAX/NON-HOAX), `Title`, `Content`, `Fact`, `References` | Raw dataset untuk "Fact-Aware Fake-news Classification for Indonesian Language". Card: **beta, NON-HOAX domain-specific & imbalanced**; repo tidak dipelihara — versi terbaru ada di BRIN Dataverse. Skala 1K–10K. **Kandidat utama** (punya fact+reference → retrieval/NLI) |
| 2 | `nlp-brin-id/id-hoax-report-merge` | Hugging Face (BRIN) | MIT | Biner + label granular (mis. disinformasi, fabricated content, satire) | Gabungan TurnBackHoax, Saber Hoaks, Klinik Hoaks; 10K–100K. Disclaimer sama (imbalanced). Cek duplikasi lintas sumber |
| 3 | BRIN Dataverse (versi terbaru) | `data.brin.go.id` (hdl:20.500.12690/RIN/7QBRKQ) | *cek* | *cek* | Pengganti resmi dataset di atas; **periksa dulu** |
| 4 | Indonesian Fact and Hoax Political News | Kaggle | MIT (menurut listing) | Politik: CNN Indonesia, Kompas, Tempo, TurnBackHoax | Berpotensi **source leakage** (kelas ditentukan sumber). Cadangan/pembanding |
| 5 | Pratiwi, Asmara, Rahutomo (2017) | Mendeley Data (DOI 10.17632/p3hfgr5j3m.1) | *cek halaman* | Hoaks/valid, 250 & 600 artikel | Kecil, berlabel voting 3 referee; cocok sebagai **uji silang/out-of-domain** |
| — | `Rifky/indonesian-hoax-news`, repo GitHub `turnbackhoax-dataset` | HF / GitHub | *cek* | — | Legacy; prioritas rendah |

## 2. Risiko yang diperkirakan (harus dibuktikan oleh audit)
1. **Source/label leakage**: teks HOAX dari turnbackhoax memuat penanda `[HOAX]`, `[SALAH]`, `Fakta`, nama situs, boilerplate → model "menyontek".
2. **Domain mismatch**: NON-HOAX dari domain tertentu saja (card sendiri menyebut imbalanced).
3. **Duplikat/near-duplicate** → kebocoran train–test.
4. **Kualitas label**: label TurnBackHoax mencerminkan penilaian fact-checker (mis. "Disinformasi", "Misleading") yang dipetakan ke biner.
5. **Kedaluwarsa temporal**: data ≤2023; sistem real-time berhadapan dengan 2026.

## 3. Prosedur audit (dijalankan oleh `ml/audit/audit_dataset.py`)
| Cek | Metode | Keluaran |
|---|---|---|
| Struktur | kolom, tipe, null, panjang teks | tabel |
| Distribusi kelas | counts & rasio per label (dan sub-label) | tabel |
| Duplikat | exact (normalisasi teks) + near-duplicate (TF-IDF cosine ≥ 0.9) | jumlah & contoh; **lintas label** (konflik label) ditandai |
| Leakage | regex penanda (`hoax/hoaks/salah/fakta/turnbackhoax/[...]`), URL/domain per label, **top n-gram diskriminatif** | frekuensi per label |
| Leakage uji cepat | baseline TF-IDF+LogReg pada (a) teks mentah (b) setelah penanda dibersihkan (c) hanya 1 sumber | selisih F1 = indikator leakage |
| Kualitas label | sampel acak 30 per kelas → review manual | berkas CSV untuk dilabeli |
| Cakupan temporal | distribusi tahun bila ada tanggal | histogram |
| Kegunaan evidence | proporsi baris punya `Fact`/`References` terisi, URL valid | persentase |

## 4. Keputusan pemilihan (diisi setelah audit)
| Item | Hasil |
|---|---|
| Dataset terpilih | _TBD_ |
| Alasan | _TBD_ |
| Dataset ditolak + alasan | _TBD_ |
| Pembersihan yang diterapkan | _TBD_ |
| Strategi split | _TBD_ (group by source / waktu) |
| Keterbatasan yang dilaporkan | _TBD_ |

## 5. Test set kurasi manual (evaluasi akhir)
- Target 100–200 klaim; sumber klaim: fact-checker (TurnBackHoax, Cek Fakta Kompas/Tempo/Liputan6, CekFakta.com) **terbit setelah tanggal akhir dataset**; pasangkan dengan sampel berita sah dari media terverifikasi pada periode serupa.
- Field: `claim_text`, `label` (benar/salah/menyesatkan/belum terbukti), `verdict_url`, `date`, `topic`, `annotator`, `notes`; minimal 2 anotator untuk ~30 sampel (hitung Cohen's κ).
- **Tidak boleh** dipakai untuk training/tuning ambang. Terpisah di `data/curated_test/`.
