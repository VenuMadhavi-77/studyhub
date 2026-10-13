import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { 
  HelpCircle, 
  MessageSquare, 
  ThumbsUp, 
  CheckCircle2, 
  PlusCircle, 
  Sparkles, 
  Search, 
  Tag, 
  User, 
  RefreshCw,
  X
} from 'lucide-react';
import api from '../services/api';
import { Doubt, Subject } from '../types';
import { useAuth } from '../context/AuthContext';

export const DoubtForumPage: React.FC = () => {
  const { isAuthenticated } = useAuth();
  const [doubts, setDoubts] = useState<Doubt[]>([]);
  const [subjects, setSubjects] = useState<Subject[]>([]);
  const [loading, setLoading] = useState(true);
  const [filterStatus, setFilterStatus] = useState('');
  const [filterSubject, setFilterSubject] = useState<number | ''>('');

  // Ask Modal State
  const [askModalOpen, setAskModalOpen] = useState(false);
  const [askTitle, setAskTitle] = useState('');
  const [askContent, setAskContent] = useState('');
  const [askSubjectId, setAskSubjectId] = useState<number | ''>('');
  const [askTags, setAskTags] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    // Load subjects for filter and ask modal
    api.get('/taxonomy/departments/1/subjects')
      .then((res) => setSubjects(res.data.data || []))
      .catch(() => {});

    fetchDoubts();
  }, [filterStatus, filterSubject]);

  const fetchDoubts = async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams();
      if (filterStatus) params.append('status', filterStatus);
      if (filterSubject) params.append('subjectId', filterSubject.toString());
      const res = await api.get(`/doubts?${params.toString()}`);
      setDoubts(res.data.data.content || []);
    } catch (err) {
      console.error('Failed to load doubts', err);
    } finally {
      setLoading(false);
    }
  };

  const handleAskSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!askSubjectId || !askTitle.trim() || !askContent.trim()) return;

    setSubmitting(true);
    try {
      const tagsArray = askTags.split(',').map((t) => t.trim()).filter(Boolean);
      await api.post('/doubts', {
        title: askTitle.trim(),
        content: askContent.trim(),
        subjectId: Number(askSubjectId),
        tags: tagsArray
      });

      setAskModalOpen(false);
      setAskTitle('');
      setAskContent('');
      setAskTags('');
      fetchDoubts();
    } catch (err) {
      alert('Failed to post doubt');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div style={{ maxWidth: '1280px', margin: '0 auto', padding: '2rem 1.5rem' }}>
      
      {/* Forum Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '2rem', marginBottom: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
            <HelpCircle size={28} color="var(--accent-primary)" /> Academic Doubt Forum
          </h1>
          <p style={{ color: 'var(--text-secondary)' }}>
            Ask questions, exchange technical solutions, and get instant AI reasoning assistance.
          </p>
        </div>

        {isAuthenticated ? (
          <button 
            onClick={() => setAskModalOpen(true)} 
            className="btn btn-primary"
            style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}
          >
            <PlusCircle size={18} /> Ask a Question
          </button>
        ) : (
          <Link to="/login" className="btn btn-secondary">Sign in to Ask</Link>
        )}
      </div>

      {/* Filter Tabs */}
      <div className="glass-panel" style={{ padding: '0.75rem 1rem', marginBottom: '1.5rem', display: 'flex', gap: '1rem', alignItems: 'center', flexWrap: 'wrap' }}>
        <span style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-muted)' }}>Status:</span>
        <button 
          onClick={() => setFilterStatus('')}
          className={`btn btn-sm ${filterStatus === '' ? 'btn-primary' : 'btn-secondary'}`}
        >
          All
        </button>
        <button 
          onClick={() => setFilterStatus('OPEN')}
          className={`btn btn-sm ${filterStatus === 'OPEN' ? 'btn-primary' : 'btn-secondary'}`}
        >
          Unsolved
        </button>
        <button 
          onClick={() => setFilterStatus('RESOLVED')}
          className={`btn btn-sm ${filterStatus === 'RESOLVED' ? 'btn-primary' : 'btn-secondary'}`}
        >
          Resolved
        </button>
      </div>

      {/* Doubts List */}
      {loading ? (
        <div style={{ textAlign: 'center', padding: '4rem 0' }}>
          <RefreshCw className="animate-spin" size={32} color="var(--accent-primary)" />
          <p style={{ marginTop: '1rem', color: 'var(--text-secondary)' }}>Loading discussions...</p>
        </div>
      ) : doubts.length === 0 ? (
        <div className="glass-panel" style={{ textAlign: 'center', padding: '4rem 2rem' }}>
          <HelpCircle size={48} color="var(--text-muted)" style={{ margin: '0 auto 1rem' }} />
          <h3>No doubts posted yet</h3>
          <p style={{ color: 'var(--text-secondary)', marginTop: '0.5rem' }}>Be the first to start an academic discussion!</p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          {doubts.map((doubt) => (
            <div key={doubt.id} className="glass-panel glass-panel-hover" style={{ padding: '1.25rem', display: 'flex', gap: '1.25rem' }}>
              
              {/* Left counters (votes & answers) */}
              <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', minWidth: '60px', gap: '0.5rem' }}>
                <div style={{ textAlign: 'center' }}>
                  <div style={{ fontSize: '1.1rem', fontWeight: 700, color: '#ffffff' }}>{doubt.upvotesCount}</div>
                  <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>votes</div>
                </div>
                <div style={{ 
                  textAlign: 'center', 
                  padding: '0.2rem 0.5rem', 
                  borderRadius: 'var(--radius-sm)',
                  background: doubt.status === 'RESOLVED' ? 'rgba(16,185,129,0.2)' : 'rgba(255,255,255,0.05)',
                  border: doubt.status === 'RESOLVED' ? '1px solid #10b981' : '1px solid transparent'
                }}>
                  <div style={{ fontSize: '1rem', fontWeight: 700, color: doubt.status === 'RESOLVED' ? '#6ee7b7' : '#ffffff' }}>
                    {doubt.answersCount}
                  </div>
                  <div style={{ fontSize: '0.65rem', color: doubt.status === 'RESOLVED' ? '#6ee7b7' : 'var(--text-muted)' }}>answers</div>
                </div>
              </div>

              {/* Right content */}
              <div style={{ flex: 1 }}>
                <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', marginBottom: '0.4rem' }}>
                  <span className={`badge ${doubt.status === 'RESOLVED' ? 'badge-emerald' : 'badge-indigo'}`}>
                    {doubt.status === 'RESOLVED' ? <CheckCircle2 size={12} /> : null} {doubt.status}
                  </span>
                  <span style={{ fontSize: '0.75rem', color: 'var(--accent-cyan)' }}>{doubt.subjectName}</span>
                </div>

                <Link to={`/doubts/${doubt.id}`} style={{ textDecoration: 'none' }}>
                  <h3 style={{ fontSize: '1.1rem', color: '#ffffff', marginBottom: '0.4rem', lineHeight: 1.4 }}>
                    {doubt.title}
                  </h3>
                </Link>

                <p style={{ fontSize: '0.88rem', color: 'var(--text-secondary)', lineHeight: 1.5, marginBottom: '0.75rem', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
                  {doubt.content}
                </p>

                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                  <div style={{ display: 'flex', gap: '0.4rem', flexWrap: 'wrap' }}>
                    {doubt.tags?.map((t, i) => (
                      <span key={i} style={{ background: 'rgba(255,255,255,0.05)', padding: '0.15rem 0.4rem', borderRadius: '4px' }}>
                        #{t}
                      </span>
                    ))}
                  </div>
                  <div>
                    Asked by <span style={{ color: '#ffffff' }}>{doubt.authorName}</span> • {new Date(doubt.createdAt).toLocaleDateString()}
                  </div>
                </div>
              </div>

            </div>
          ))}
        </div>
      )}

      {/* Ask Question Modal */}
      {askModalOpen && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.7)', backdropFilter: 'blur(4px)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000, padding: '1rem' }}>
          <div className="glass-panel" style={{ width: '100%', maxWidth: '640px', padding: '2rem', background: 'var(--bg-secondary)', border: '1px solid var(--border-hover)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
              <h2 style={{ fontSize: '1.4rem' }}>Ask an Academic Question</h2>
              <button onClick={() => setAskModalOpen(false)} style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}>
                <X size={20} />
              </button>
            </div>

            <form onSubmit={handleAskSubmit}>
              <div style={{ marginBottom: '1rem' }}>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '0.3rem' }}>Subject *</label>
                <select 
                  required
                  value={askSubjectId} 
                  onChange={(e) => setAskSubjectId(e.target.value ? Number(e.target.value) : '')}
                  className="input-field"
                >
                  <option value="">Select Subject</option>
                  {subjects.map((s) => (
                    <option key={s.id} value={s.id}>{s.name} ({s.code})</option>
                  ))}
                </select>
              </div>

              <div style={{ marginBottom: '1rem' }}>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '0.3rem' }}>Question Title *</label>
                <input 
                  type="text" 
                  required 
                  placeholder="e.g. Why does QuickSort degrade to O(N^2) on sorted arrays?"
                  value={askTitle} 
                  onChange={(e) => setAskTitle(e.target.value)}
                  className="input-field"
                />
              </div>

              <div style={{ marginBottom: '1rem' }}>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '0.3rem' }}>Detailed Explanation *</label>
                <textarea 
                  required
                  rows={4}
                  placeholder="Provide complete context, error traces, code snippets or mathematical formulation..."
                  value={askContent} 
                  onChange={(e) => setAskContent(e.target.value)}
                  className="input-field"
                />
              </div>

              <div style={{ marginBottom: '1.5rem' }}>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '0.3rem' }}>Tags (comma-separated)</label>
                <input 
                  type="text" 
                  placeholder="e.g. algorithms, sorting, worst-case"
                  value={askTags} 
                  onChange={(e) => setAskTags(e.target.value)}
                  className="input-field"
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' }}>
                <button type="button" onClick={() => setAskModalOpen(false)} className="btn btn-secondary">Cancel</button>
                <button type="submit" disabled={submitting} className="btn btn-primary">
                  {submitting ? 'Posting...' : 'Post Question'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

    </div>
  );
};
