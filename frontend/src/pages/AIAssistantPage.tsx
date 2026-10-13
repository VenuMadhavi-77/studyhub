import React, { useState, useRef, useEffect } from 'react';
import { Sparkles, Send, RefreshCw, BookOpen, Lightbulb, Search, AlertCircle, Mic } from 'lucide-react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import { Link } from 'react-router-dom';

interface Message {
  role: 'user' | 'assistant';
  text: string;
  timestamp: Date;
}

const SUGGESTED_QUESTIONS = [
  'Explain time complexity of quicksort vs mergesort',
  'What is normalization in databases? Explain 3NF',
  'How does TCP/IP handshake work?',
  'Difference between process and thread in OS',
  'Explain SOLID principles with examples',
  'What is dynamic programming? Give an example',
];

export const AIAssistantPage: React.FC = () => {
  const { isAuthenticated } = useAuth();
  const [messages, setMessages] = useState<Message[]>([]);
  const [inputText, setInputText] = useState('');
  const [loading, setLoading] = useState(false);
  const [subjectName, setSubjectName] = useState('');
  const [error, setError] = useState('');
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLTextAreaElement>(null);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const sendMessage = async (text?: string) => {
    const question = (text || inputText).trim();
    if (!question || loading) return;

    const userMessage: Message = { role: 'user', text: question, timestamp: new Date() };
    setMessages((prev) => [...prev, userMessage]);
    setInputText('');
    setLoading(true);
    setError('');

    try {
      const res = await api.post('/ai/doubt-assistant', {
        question: question,
        title: question,
        content: question,
        subjectContext: subjectName || undefined,
        subjectName: subjectName || undefined,
      });
      const data = res.data.data;

      let responseText = '';
      if (data.answer) {
        responseText = data.answer;
      } else if (data.directAnswer) {
        responseText = [
          data.directAnswer,
          data.stepByStepExplanation ? `\n\n📋 **Step-by-step Breakdown**:\n${data.stepByStepExplanation}` : '',
          data.keyTakeaways ? `\n\n💡 **Key Takeaways**: ${data.keyTakeaways}` : '',
          data.practiceQuestion ? `\n\n🧠 **Practice Challenge**: ${data.practiceQuestion}` : '',
        ].join('').trim();
      } else {
        responseText = 'Here is the explanation for your academic query.';
      }

      const assistantMessage: Message = {
        role: 'assistant',
        text: responseText,
        timestamp: new Date(),
      };
      setMessages((prev) => [...prev, assistantMessage]);
    } catch (err: any) {
      const errMsg = err.response?.data?.message || 'AI service is temporarily unavailable. Please try again.';
      setError(errMsg);
    } finally {
      setLoading(false);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  if (!isAuthenticated) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[60vh] gap-4 text-center">
        <Sparkles size={48} className="text-violet-500" />
        <h1 className="text-2xl font-bold text-slate-900 dark:text-white">AI Academic Assistant</h1>
        <p className="text-slate-500 dark:text-slate-400">Sign in to access Gemini-powered academic help.</p>
        <Link to="/login" className="btn btn-primary">Sign In</Link>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto pb-12 flex flex-col gap-4 h-[calc(100vh-120px)]">
      {/* Header */}
      <div className="flex items-center gap-3">
        <div className="p-2 rounded-xl bg-violet-500/10">
          <Sparkles size={22} className="text-violet-500" />
        </div>
        <div>
          <h1 className="text-xl font-bold text-slate-900 dark:text-white">AI Academic Assistant</h1>
          <p className="text-xs text-slate-500 dark:text-slate-400">Powered by Gemini · Ask any academic question</p>
        </div>
        <div className="ml-auto">
          <input
            type="text"
            placeholder="Subject context (optional)"
            value={subjectName}
            onChange={(e) => setSubjectName(e.target.value)}
            className="input-field text-sm py-1.5 px-3 w-44"
          />
        </div>
      </div>

      {/* Messages area */}
      <div className="flex-1 overflow-y-auto glass-panel p-4 space-y-4 min-h-0">
        {messages.length === 0 ? (
          <div className="space-y-4 py-4">
            <div className="text-center space-y-2">
              <Lightbulb size={32} className="text-amber-400 mx-auto" />
              <h2 className="text-lg font-semibold text-slate-800 dark:text-white">What would you like to learn?</h2>
              <p className="text-sm text-slate-500 dark:text-slate-400">
                Ask any CS/Engineering question — I'll give you clear explanations, examples, and step-by-step breakdowns.
              </p>
            </div>
            <div className="grid sm:grid-cols-2 gap-2 mt-4">
              {SUGGESTED_QUESTIONS.map((q, i) => (
                <button
                  key={i}
                  onClick={() => sendMessage(q)}
                  className="text-left p-3 rounded-xl border border-slate-200 dark:border-white/10 hover:border-violet-400/60 dark:hover:border-violet-400/40 hover:bg-violet-50 dark:hover:bg-violet-900/10 text-sm text-slate-700 dark:text-slate-300 transition-all"
                >
                  {q}
                </button>
              ))}
            </div>
          </div>
        ) : (
          messages.map((msg, idx) => (
            <div
              key={idx}
              className={`flex gap-3 ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}
            >
              {msg.role === 'assistant' && (
                <div className="w-8 h-8 rounded-full bg-violet-500/20 flex items-center justify-center shrink-0 mt-1">
                  <Sparkles size={14} className="text-violet-400" />
                </div>
              )}
              <div
                className={`max-w-[80%] rounded-2xl p-3.5 text-sm leading-relaxed whitespace-pre-wrap ${
                  msg.role === 'user'
                    ? 'bg-indigo-600 text-white rounded-tr-sm'
                    : 'bg-slate-100 dark:bg-white/5 text-slate-800 dark:text-slate-200 rounded-tl-sm border border-slate-200 dark:border-white/10'
                }`}
              >
                {msg.text}
                <div className={`text-xs mt-1.5 ${msg.role === 'user' ? 'text-indigo-200' : 'text-slate-400'}`}>
                  {msg.timestamp.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                </div>
              </div>
              {msg.role === 'user' && (
                <div className="w-8 h-8 rounded-full bg-indigo-500/20 flex items-center justify-center shrink-0 mt-1">
                  <BookOpen size={14} className="text-indigo-400" />
                </div>
              )}
            </div>
          ))
        )}

        {loading && (
          <div className="flex gap-3 justify-start">
            <div className="w-8 h-8 rounded-full bg-violet-500/20 flex items-center justify-center shrink-0">
              <Sparkles size={14} className="text-violet-400" />
            </div>
            <div className="bg-slate-100 dark:bg-white/5 rounded-2xl rounded-tl-sm p-3.5 border border-slate-200 dark:border-white/10">
              <div className="flex gap-1.5 items-center">
                {[0, 150, 300].map((delay) => (
                  <div
                    key={delay}
                    className="w-2 h-2 rounded-full bg-violet-400 animate-bounce"
                    style={{ animationDelay: `${delay}ms` }}
                  />
                ))}
              </div>
            </div>
          </div>
        )}

        {error && (
          <div className="flex items-center gap-2 p-3 rounded-xl bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 text-sm">
            <AlertCircle size={16} /> {error}
          </div>
        )}

        <div ref={messagesEndRef} />
      </div>

      {/* Input area */}
      <div className="glass-panel p-3 flex gap-3 items-end">
        <textarea
          ref={inputRef}
          rows={2}
          value={inputText}
          onChange={(e) => setInputText(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="Ask any academic question... (Enter to send, Shift+Enter for new line)"
          className="input-field flex-1 resize-none text-sm leading-relaxed"
          disabled={loading}
        />
        <button
          onClick={() => sendMessage()}
          disabled={loading || !inputText.trim()}
          className="btn btn-primary p-3 shrink-0"
          title="Send message"
        >
          {loading ? <RefreshCw size={18} className="animate-spin" /> : <Send size={18} />}
        </button>
      </div>
    </div>
  );
};
