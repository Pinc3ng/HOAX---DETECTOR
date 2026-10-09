require('dotenv').config();
const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const rateLimit = require('express-rate-limit');

const factCheckRoutes = require('./routes/factcheck');
const healthRoutes = require('./routes/health');

const app = express();
const PORT = process.env.PORT || 3000;

// Security middleware
app.use(helmet());
app.use(cors({
  origin: '*', // Allow all origins for mobile app
  methods: ['GET', 'POST'],
  allowedHeaders: ['Content-Type', 'Authorization']
}));

// Rate limiting: 20 requests per minute per IP
const limiter = rateLimit({
  windowMs: 1 * 60 * 1000, // 1 minute
  max: 20,
  message: {
    success: false,
    error: 'Terlalu banyak permintaan. Coba lagi dalam 1 menit.'
  }
});
app.use('/api/', limiter);

// Normalize Content-Type (fix UTF8 without hyphen to utf-8)
app.use((req, res, next) => {
  if (req.headers['content-type']) {
    req.headers['content-type'] = req.headers['content-type'].replace(/UTF8/gi, 'utf-8');
  }
  next();
});

// Body parser
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true, limit: '10mb' }));

// Request logging
app.use((req, res, next) => {
  const timestamp = new Date().toISOString();
  console.log(`[${timestamp}] ${req.method} ${req.path}`);
  next();
});

// Routes
app.use('/api/health', healthRoutes);
app.use('/api/factcheck', factCheckRoutes);

// 404 handler
app.use((req, res) => {
  res.status(404).json({
    success: false,
    error: 'Endpoint tidak ditemukan'
  });
});

// Global error handler
app.use((err, req, res, next) => {
  console.error('Error:', err.message);
  res.status(500).json({
    success: false,
    error: 'Terjadi kesalahan server internal'
  });
});

app.listen(PORT, '0.0.0.0', () => {
  console.log(`🚀 Hoax Detector Backend berjalan di port ${PORT}`);
  console.log(`📡 API tersedia di: http://localhost:${PORT}/api`);
  console.log(`🔑 Gemini API Key: ${process.env.GEMINI_API_KEY ? '✅ Terkonfigurasi' : '❌ BELUM diset!'}`);
});

module.exports = app;
