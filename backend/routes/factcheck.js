const express = require('express');
const router = express.Router();
const { analyzeWithGemini, fetchArticleContent } = require('../services/geminiService');

/**
 * POST /api/factcheck/text
 * Memeriksa teks berita atau klaim
 * Body: { text: string }
 */
router.post('/text', async (req, res) => {
  try {
    const { text } = req.body;

    if (!text || typeof text !== 'string') {
      return res.status(400).json({
        success: false,
        error: 'Teks berita harus diisi'
      });
    }

    if (text.trim().length < 20) {
      return res.status(400).json({
        success: false,
        error: 'Teks terlalu pendek. Minimal 20 karakter untuk dianalisis.'
      });
    }

    if (text.length > 10000) {
      return res.status(400).json({
        success: false,
        error: 'Teks terlalu panjang. Maksimal 10.000 karakter.'
      });
    }

    if (!process.env.GEMINI_API_KEY) {
      return res.status(500).json({
        success: false,
        error: 'API Key Gemini belum dikonfigurasi di server'
      });
    }

    console.log(`[FactCheck] Menganalisis teks (${text.length} karakter)...`);
    const analysis = await analyzeWithGemini(text);

    res.json({
      success: true,
      type: 'text',
      timestamp: new Date().toISOString(),
      analysis
    });

  } catch (error) {
    console.error('[FactCheck/text] Error:', error.message);

    if (error.message.includes('API_KEY_INVALID') || error.message.includes('API key')) {
      return res.status(401).json({
        success: false,
        error: 'API Key Gemini tidak valid. Periksa konfigurasi server.'
      });
    }

    if (error.message.includes('QUOTA') || error.message.includes('quota')) {
      return res.status(429).json({
        success: false,
        error: 'Kuota Gemini API habis. Coba lagi nanti.'
      });
    }

    res.status(500).json({
      success: false,
      error: 'Gagal menganalisis teks. Coba lagi nanti.'
    });
  }
});

/**
 * POST /api/factcheck/url
 * Memeriksa berita dari URL artikel
 * Body: { url: string }
 */
router.post('/url', async (req, res) => {
  try {
    const { url } = req.body;

    if (!url || typeof url !== 'string') {
      return res.status(400).json({
        success: false,
        error: 'URL artikel harus diisi'
      });
    }

    // Validasi URL
    let parsedUrl;
    try {
      parsedUrl = new URL(url);
      if (!['http:', 'https:'].includes(parsedUrl.protocol)) {
        throw new Error('Protocol tidak valid');
      }
    } catch {
      return res.status(400).json({
        success: false,
        error: 'URL tidak valid. Masukkan URL lengkap dengan http:// atau https://'
      });
    }

    if (!process.env.GEMINI_API_KEY) {
      return res.status(500).json({
        success: false,
        error: 'API Key Gemini belum dikonfigurasi di server'
      });
    }

    console.log(`[FactCheck] Mengambil konten dari URL: ${url}`);

    // Ambil konten artikel
    let articleData;
    try {
      articleData = await fetchArticleContent(url);
    } catch (fetchError) {
      return res.status(422).json({
        success: false,
        error: `Gagal mengambil konten artikel: ${fetchError.message}. Pastikan URL dapat diakses.`
      });
    }

    if (!articleData.content || articleData.content.length < 100) {
      return res.status(422).json({
        success: false,
        error: 'Konten artikel terlalu sedikit untuk dianalisis. Mungkin halaman memerlukan login atau JavaScript.'
      });
    }

    const textToAnalyze = `${articleData.title ? 'JUDUL: ' + articleData.title + '\n\n' : ''}${articleData.content}`;
    const context = `Artikel diambil dari: ${url}`;

    console.log(`[FactCheck] Menganalisis artikel dari URL (${textToAnalyze.length} karakter)...`);
    const analysis = await analyzeWithGemini(textToAnalyze, context);

    res.json({
      success: true,
      type: 'url',
      timestamp: new Date().toISOString(),
      articleInfo: {
        title: articleData.title,
        url: url,
        contentLength: articleData.content.length
      },
      analysis
    });

  } catch (error) {
    console.error('[FactCheck/url] Error:', error.message);

    if (error.message.includes('API_KEY_INVALID') || error.message.includes('API key')) {
      return res.status(401).json({
        success: false,
        error: 'API Key Gemini tidak valid. Periksa konfigurasi server.'
      });
    }

    res.status(500).json({
      success: false,
      error: 'Terjadi kesalahan saat memproses permintaan.'
    });
  }
});

module.exports = router;
