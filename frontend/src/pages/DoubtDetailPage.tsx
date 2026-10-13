import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { 
  HelpCircle, 
  ThumbsUp, 
  CheckCircle2, 
  Sparkles, 
  MessageSquare, 
  User, 
  Calendar, 
  RefreshCw,
  Send,
  Check
} from 'lucide-react';
import api from '../services/api';
import { Doubt, Answer } from '../types';
import { useAuth } from '../context/AuthContext';

export const DoubtDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const { user, isAuthenticated } = useAuth();
  
  const [doubt, setDoubt] = useState<Doubt | null>(null);
  const [answers, setAnswers] = useState<Answer[]>([]);
  const [loading, setLoading] = useState(true);
  const [newAnswerContent, setNewAnswerContent] = useState('');
  const [submittingAnswer, setSubmittingAnswer] = useState(false);
  const [aiLoading, setAiLoading] = useState(false);
  const [aiSolution, setAiSolution] = useState<{ directAnswer: string; stepByStepExplanation: string; keyTakeaways: string } | null>(null);

  useEffect(() => {
    if (!id) return;
    loadDoubtData();
  }, [id]);

  const loadDoubtData = async () => {
    setLoading(true);
    try {
      const currentUserId = user?.id;
      const doubtRes = await api.get(`/doubts/${id}${currentUserId ? `?userId=${currentUserId}` : ''}`);
      const detail = doubtRes.data.data;
      setDoubt(detail);
      // answers are embedded in the detail response
      setAnswers(detail.answers || []);
    } catch (err) {
      console.error('Failed to load doubt details', err);
    } finally {
      setLoading(false);
    }
  };

  const handleUpvoteDoubt = async () => {
    if (!isAuthenticated || !id) return;
    try {
      // doubts don't have a dedicated upvote endpoint; show optimistic count
      if (doubt) setDoubt({ ...doubt, upvotesCount: doubt.upvotesCount + 1 });
    } catch (err) {
      console.error('Failed to upvote doubt', err);
    }
  };

  const handleUpvoteAnswer = async (answerId: number) => {
    if (!isAuthenticated) return;
    try {
      const res = await api.post(`/answers/${answerId}/vote`);
      setAnswers((prev) =>
        prev.map((a) => (a.id === answerId ? { ...a, upvotesCount: res.data.data.upvoteCount, hasVoted: res.data.data.upvoted } : a))
      );
    } catch (err) {
      console.error('Failed to upvote answer', err);
    }
  };

  const handleAcceptAnswer = async (answerId: number) => {
    if (!isAuthenticated || !id) return;
    try {
      await api.post(`/doubts/${id}/accept/${answerId}`);
      loadDoubtData();
    } catch (err) {
      alert('Failed to accept answer');
    }
  };

  const handlePostAnswer = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!isAuthenticated || !id || !newAnswerContent.trim()) return;

    setSubmittingAnswer(true);
    try {
      await api.post(`/doubts/${id}/answers`, { content: newAnswerContent.trim() });
      setNewAnswerContent('');
      loadDoubtData();
    } catch (err) {
      alert('Failed to post answer');
    } finally {
      setSubmittingAnswer(false);
    }
  };

  const handleGetAiSolution = async () => {
    if (!doubt) return;
    setAiLoading(true);
    try {
      const res = await api.post('/ai/doubt-assistant', {
        question: doubt.title + (doubt.content ? ': ' + doubt.content : ''),
        title: doubt.title,
        content: doubt.content,
        subjectContext: doubt.subjectName,
        subjectName: doubt.subjectName
      });
      setAiSolution(res.data.data);
    } catch (err) {
      alert('Failed to generate AI solution');
    } finally {
      setAiLoading(false);
    }
  };

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '6rem 0' }}>
        <RefreshCw className="animate-spin" size={36} color="var(--accent-primary)" />
        <p style={{ marginTop: '1rem', color: 'var(--text-secondary)' }}>Loading discussion...</p>
      </div>
    );
  }

  if (!doubt) {
    return (
      <div style={{ maxWidth: '600px', margin: '4rem auto', textAlign: 'center' }}>
        <h2>Question not found</h2>
        <Link to="/doubts" className="btn btn-primary" style={{ marginTop: '1rem' }}>Back to Forum</Link>
      </div>
    );
  }

  const isAuthor = user?.id === doubt.authorId;

  return (
    <div style={{ maxWidth: '1000px', margin: '0 auto', padding: '2rem 1.5rem' }}>
      
      {/* Breadcrumb */}
      <div style={{ marginBottom: '1.5rem', fontSize: '0.85rem', color: 'var(--text-muted)' }}>
        <Link to="/doubts">Doubt Forum</Link> &gt; <span>{doubt.subjectName}</span>
      </div>

      {/* Main Doubt Card */}
      <div className="glass-panel" style={{ padding: '2rem', marginBottom: '2rem' }}>
        <div style={{ display: 'flex', gap: '1.5rem' }}>
          
          {/* Upvote column */}
          <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', minWidth: '48px' }}>
            <button 
              onClick={handleUpvoteDoubt}
              disabled={!isAuthenticated}
              className="btn btn-secondary btn-sm"
              style={{ borderRadius: '50%', padding: '0.6rem' }}
              title="Upvote question"
            >
              <ThumbsUp size={16} />
            </button>
            <span style={{ fontSize: '1.2rem', fontWeight: 700, margin: '0.4rem 0' }}>{doubt.upvotesCount}</span>
          </div>

          {/* Question Details */}
          <div style={{ flex: 1 }}>
            <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '0.75rem', alignItems: 'center' }}>
              <span className={`badge ${doubt.status === 'RESOLVED' ? 'badge-emerald' : 'badge-indigo'}`}>
                {doubt.status}
              </span>
              <span style={{ fontSize: '0.8rem', color: 'var(--accent-cyan)' }}>{doubt.subjectName}</span>
            </div>

            <h1 style={{ fontSize: '1.5rem', marginBottom: '1rem', lineHeight: 1.35 }}>{doubt.title}</h1>

            <div style={{ color: 'var(--text-primary)', lineHeight: 1.7, fontSize: '0.95rem', whiteSpace: 'pre-wrap', marginBottom: '1.5rem' }}>
              {doubt.content}
            </div>

            {/* Tags & Meta */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderTop: '1px solid var(--border-subtle)', paddingTop: '1rem', flexWrap: 'wrap', gap: '0.75rem', fontSize: '0.8rem', color: 'var(--text-muted)' }}>
              <div style={{ display: 'flex', gap: '0.4rem' }}>
                {doubt.tags?.map((t, idx) => (
                  <span key={idx} style={{ background: 'rgba(255,255,255,0.06)', padding: '0.2rem 0.5rem', borderRadius: '4px' }}>
                    #{t}
                  </span>
                ))}
              </div>

              <div>
                Asked by <span style={{ color: '#ffffff', fontWeight: 600 }}>{doubt.authorName}</span> on {new Date(doubt.createdAt).toLocaleDateString()}
              </div>
            </div>
          </div>

        </div>
      </div>

      {/* AI Assistant Solution Box */}
      <div className="glass-panel" style={{ padding: '1.5rem', marginBottom: '2rem', border: '1px solid rgba(139,92,246,0.4)', background: 'linear-gradient(180deg, rgba(30, 27, 75, 0.4) 0%, rgba(15, 23, 42, 0.8) 100%)' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Sparkles size={20} color="#a855f7" />
            <h3 style={{ fontSize: '1.1rem' }}>Instant AI Reasoning Assistant</h3>
          </div>
          <button 
            onClick={handleGetAiSolution}
            disabled={aiLoading}
            className="btn btn-secondary btn-sm"
            style={{ fontSize: '0.8rem', color: '#c084fc', border: '1px solid rgba(139, 92, 246, 0.4)' }}
          >
            {aiLoading ? <RefreshCw className="animate-spin" size={14} /> : <Sparkles size={14} />}
            {aiSolution ? 'Regenerate AI Analysis' : 'Generate AI Explanation'}
          </button>
        </div>

        {aiSolution ? (
          <div>
            <div style={{ background: 'rgba(0,0,0,0.25)', padding: '1rem', borderRadius: 'var(--radius-md)', marginBottom: '1rem' }}>
              <h4 style={{ fontSize: '0.85rem', color: 'var(--accent-cyan)', marginBottom: '0.4rem', textTransform: 'uppercase' }}>Direct Solution</h4>
              <p style={{ fontSize: '0.92rem', lineHeight: 1.6 }}>{aiSolution.directAnswer}</p>
            </div>

            <div style={{ marginBottom: '1rem' }}>
              <h4 style={{ fontSize: '0.85rem', color: 'var(--accent-purple)', marginBottom: '0.4rem', textTransform: 'uppercase' }}>Step-by-Step Breakdown</h4>
              <div style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', lineHeight: 1.6, whiteSpace: 'pre-wrap' }}>
                {aiSolution.stepByStepExplanation}
              </div>
            </div>

            {aiSolution.keyTakeaways && (
              <div style={{ borderTop: '1px solid rgba(255,255,255,0.06)', paddingTop: '0.75rem', fontSize: '0.85rem', color: '#a5b4fc' }}>
                <strong>Key Concept:</strong> {aiSolution.keyTakeaways}
              </div>
            )}
          </div>
        ) : (
          <p style={{ color: 'var(--text-muted)', fontSize: '0.88rem' }}>
            Need an immediate answer? Click above to trigger Gemini 2.5 Flash for verified educational reasoning.
          </p>
        )}
      </div>

      {/* Answers Section */}
      <div style={{ marginBottom: '2rem' }}>
        <h2 style={{ fontSize: '1.3rem', marginBottom: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <MessageSquare size={20} color="var(--accent-primary)" /> {answers.length} Community Answers
        </h2>

        {answers.length === 0 ? (
          <div className="glass-panel" style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
            No peer answers submitted yet. Be the first to share your solution below!
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {answers.map((ans) => (
              <div 
                key={ans.id} 
                className="glass-panel" 
                style={{ 
                  padding: '1.5rem', 
                  border: ans.isAccepted ? '1px solid #10b981' : '1px solid var(--border-subtle)',
                  background: ans.isAccepted ? 'rgba(16, 185, 129, 0.05)' : 'var(--bg-card)'
                }}
              >
                <div style={{ display: 'flex', gap: '1.25rem' }}>
                  
                  {/* Upvote Answer */}
                  <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', minWidth: '40px' }}>
                    <button 
                      onClick={() => handleUpvoteAnswer(ans.id)}
                      disabled={!isAuthenticated}
                      className="btn btn-secondary btn-sm"
                      style={{ padding: '0.4rem 0.5rem', borderRadius: '50%' }}
                    >
                      <ThumbsUp size={14} />
                    </button>
                    <span style={{ fontSize: '0.95rem', fontWeight: 700, margin: '0.25rem 0' }}>{ans.upvotesCount}</span>
                  </div>

                  {/* Answer Content */}
                  <div style={{ flex: 1 }}>
                    {ans.isAccepted && (
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.3rem', color: '#10b981', fontSize: '0.8rem', fontWeight: 700, marginBottom: '0.5rem' }}>
                        <CheckCircle2 size={16} /> ACCEPTED SOLUTION
                      </div>
                    )}

                    <div style={{ fontSize: '0.92rem', lineHeight: 1.6, color: 'var(--text-primary)', whiteSpace: 'pre-wrap', marginBottom: '1rem' }}>
                      {ans.content}
                    </div>

                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderTop: '1px solid var(--border-subtle)', paddingTop: '0.75rem', fontSize: '0.8rem' }}>
                      <span style={{ color: 'var(--text-muted)' }}>
                        Answered by <strong style={{ color: '#ffffff' }}>{ans.authorName}</strong> on {new Date(ans.createdAt).toLocaleDateString()}
                      </span>

                      {/* Author can accept */}
                      {isAuthor && !doubt.acceptedAnswerId && !ans.isAccepted && (
                        <button 
                          onClick={() => handleAcceptAnswer(ans.id)} 
                          className="btn btn-primary btn-sm"
                          style={{ background: 'var(--accent-emerald)', padding: '0.3rem 0.75rem' }}
                        >
                          <Check size={14} /> Accept Solution
                        </button>
                      )}
                    </div>
                  </div>

                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Submit Answer Form */}
      {isAuthenticated ? (
        <div className="glass-panel" style={{ padding: '1.75rem' }}>
          <h3 style={{ fontSize: '1.1rem', marginBottom: '0.75rem' }}>Your Answer</h3>
          <form onSubmit={handlePostAnswer}>
            <textarea 
              required
              rows={5}
              placeholder="Provide a clear, respectful answer with code snippets or derivations..."
              value={newAnswerContent}
              onChange={(e) => setNewAnswerContent(e.target.value)}
              className="input-field"
              style={{ marginBottom: '1rem' }}
            />
            <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
              <button type="submit" disabled={submittingAnswer} className="btn btn-primary">
                {submittingAnswer ? <RefreshCw className="animate-spin" size={16} /> : <Send size={16} />}
                {submittingAnswer ? 'Submitting...' : 'Post Your Answer'}
              </button>
            </div>
          </form>
        </div>
      ) : (
        <div className="glass-panel" style={{ padding: '1.5rem', textAlign: 'center' }}>
          <p style={{ color: 'var(--text-secondary)', marginBottom: '0.75rem' }}>Sign in to share your knowledge and earn reputation points.</p>
          <Link to="/login" className="btn btn-primary btn-sm">Sign In to Answer</Link>
        </div>
      )}

    </div>
  );
};
