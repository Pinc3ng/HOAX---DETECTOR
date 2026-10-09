import { useState } from 'react';
import { ShieldCheck, ShieldAlert, Shield, Activity, Info, AlertTriangle, ArrowRight } from 'lucide-react';
import './App.css';

function App() {
  const [text, setText] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);

  const handleAnalyze = async () => {
    if (!text.trim()) return;
    
    setIsLoading(true);
    setError(null);
    setResult(null);
    
    try {
      const response = await fetch('http://localhost:8000/api/v1/demo/analyze', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ text })
      });
      
      if (!response.ok) {
        throw new Error(`Server error: ${response.status}`);
      }
      
      const data = await response.json();
      setResult(data);
    } catch (err) {
      setError(err.message || 'Failed to connect to the server.');
    } finally {
      setIsLoading(false);
    }
  };

  const renderEvidenceList = (title, items, icon) => {
    if (!items || items.length === 0) return null;
    return (
      <div className="evidence-section" style={{ marginTop: '1.5rem', textAlign: 'left' }}>
        <h4 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.5rem' }}>
          {icon} {title} ({items.length})
        </h4>
        <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
          {items.map((item, i) => (
            <li key={i} style={{ padding: '1rem', background: 'rgba(255,255,255,0.05)', borderRadius: '8px', marginBottom: '0.5rem', borderLeft: '4px solid #3b82f6' }}>
              <strong><a href={item.url} target="_blank" rel="noreferrer" style={{ color: '#60a5fa', textDecoration: 'none' }}>{item.title}</a></strong>
              <div style={{ fontSize: '0.85rem', color: '#9ca3af', marginTop: '0.5rem', display: 'flex', flexWrap: 'wrap', gap: '1rem' }}>
                <span><strong>Source:</strong> {item.domain}</span>
                <span><strong>Tier:</strong> {item.extra.source_reliability_tier?.toUpperCase() || 'UNKNOWN'}</span>
                <span><strong>NLI:</strong> {item.extra.nli_label?.toUpperCase()}</span>
                {item.published_at && <span><strong>Date:</strong> {new Date(item.published_at).toLocaleDateString()}</span>}
              </div>
            </li>
          ))}
        </ul>
      </div>
    );
  };

  return (
    <div className="app-container animate-fade-in">
      <header className="header">
        <h1>Hoax-Detector</h1>
        <p>Evidence-based Verification System</p>
      </header>

      <main>
        <div className="glass-panel input-section">
          <label className="input-label" htmlFor="news-text">
            Enter Claim to Verify
          </label>
          <textarea 
            id="news-text"
            className="text-input" 
            placeholder="e.g. Bawang putih dapat menyembuhkan COVID-19."
            value={text}
            onChange={(e) => setText(e.target.value)}
          />
          
          {error && (
            <div style={{ color: '#fca5a5', marginBottom: '1rem', display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
              <AlertTriangle size={18} /> {error}
            </div>
          )}

          <button 
            className="btn-analyze" 
            onClick={handleAnalyze} 
            disabled={isLoading || text.length < 5}
          >
            {isLoading ? (
              <><span className="loader"></span> Retrieving & Analyzing Evidence...</>
            ) : (
              <>Verify Claim <ArrowRight size={20} /></>
            )}
          </button>
        </div>

        {result && (
          <div className="glass-panel result-section animate-fade-in" style={{ maxWidth: '800px' }}>
            <div className="result-header">
              <div className="result-title">
                <Activity size={24} color="#3b82f6" /> Evidence Analysis
              </div>
            </div>

            {result.evidence_coverage === 'INSUFFICIENT' ? (
              <div style={{ textAlign: 'center', padding: '2rem' }}>
                <AlertTriangle size={48} color="#f59e0b" style={{ margin: '0 auto 1rem' }} />
                <h3>Bukti belum cukup</h3>
                <p style={{ color: 'var(--text-muted)' }}>Sistem tidak menemukan sumber kredibel yang relevan untuk memverifikasi klaim ini.</p>
              </div>
            ) : (
              <>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '2rem' }}>
                  <div style={{ background: 'rgba(59, 130, 246, 0.1)', padding: '1.5rem', borderRadius: '12px', textAlign: 'center' }}>
                    <div style={{ fontSize: '2.5rem', fontWeight: 'bold', color: '#60a5fa' }}>{Math.round(result.support_score * 100)}%</div>
                    <div style={{ color: '#9ca3af', fontSize: '0.9rem' }}>Support Score</div>
                  </div>
                  <div style={{ background: 'rgba(239, 68, 68, 0.1)', padding: '1.5rem', borderRadius: '12px', textAlign: 'center' }}>
                    <div style={{ fontSize: '2.5rem', fontWeight: 'bold', color: '#f87171' }}>{Math.round(result.contradiction_score * 100)}%</div>
                    <div style={{ color: '#9ca3af', fontSize: '0.9rem' }}>Contradiction Score</div>
                  </div>
                </div>

                <div className="metadata-box" style={{ marginBottom: '2rem' }}>
                  <div className="meta-item">
                    <strong>Evidence Coverage</strong>
                    {result.evidence_coverage}
                  </div>
                  <div className="meta-item">
                    <strong>Independent Clusters</strong>
                    {result.independent_evidence_count}
                  </div>
                  <div className="meta-item">
                    <strong>Conflicting Evidence</strong>
                    {result.conflicting_evidence ? "Yes" : "No"}
                  </div>
                </div>

                {renderEvidenceList("Supporting Evidence", result.supporting_evidence, <ShieldCheck size={20} color="#60a5fa" />)}
                {renderEvidenceList("Contradicting Evidence", result.contradicting_evidence, <ShieldAlert size={20} color="#f87171" />)}
                {renderEvidenceList("Neutral Evidence", result.neutral_evidence, <Info size={20} color="#9ca3af" />)}

                <div className="notice-banner" style={{ marginTop: '2rem' }}>
                  <Info size={24} style={{ flexShrink: 0 }} />
                  <div style={{ fontSize: '0.9rem' }}>
                    <strong>Note:</strong> <em>Source Reliability</em> digunakan sebagai bobot kepercayaan terhadap suatu argumen, BUKAN sebagai penentu akhir benar/salah (truth detector). Analisis NLI hanya memetakan <em>stance</em> bukti.
                  </div>
                </div>
              </>
            )}
          </div>
        )}
      </main>
    </div>
  );
}

export default App;
