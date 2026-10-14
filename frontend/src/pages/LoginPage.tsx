import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  LogIn,
  AlertCircle,
  RefreshCw,
  Sparkles,
  ShieldCheck,
  CheckCircle2,
  Lock,
  Mail,
  ArrowRight,
  Eye,
  EyeOff,
  HelpCircle,
  Database,
  GraduationCap,
  Terminal,
  Cpu
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { getConnectionInfo } from '../services/api';

export const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [forgotPasswordNotice, setForgotPasswordNotice] = useState(false);

  const connectionInfo = getConnectionInfo();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const user = await login(email.trim(), password.trim());
      if (user?.role === 'ADMIN') {
        navigate('/admin');
      } else {
        navigate('/dashboard');
      }
    } catch (err: any) {
      if (err.code === 'ECONNABORTED' || err.message?.includes('Network Error') || !err.response) {
        setError('Cannot connect to backend server. Ensure backend is running on http://localhost:8080.');
      } else {
        setError(err.response?.data?.message || 'Invalid institutional roll number / email or password');
      }
    } finally {
      setLoading(false);
    }
  };

  const fillDemo = (demoEmail: string, demoPass: string) => {
    setEmail(demoEmail);
    setPassword(demoPass);
    setError('');
  };

  return (
    <div className="min-h-[calc(100vh-4.25rem)] flex items-center justify-center p-4 sm:p-6 lg:p-8 bg-slate-50 dark:bg-[#070B14]">
      <div className="max-w-5xl w-full grid grid-cols-1 lg:grid-cols-12 rounded-3xl bg-white dark:bg-[#0D1322] border border-slate-200/90 dark:border-slate-800 shadow-2xl overflow-hidden">
        
        {/* Left: Anurag InStacks Institutional Brand Showcase (5 cols) */}
        <div className="lg:col-span-5 p-8 sm:p-10 bg-gradient-to-br from-[#0A1128] via-[#001F54] to-[#121B3A] text-white flex flex-col justify-between relative overflow-hidden">
          
          <div className="space-y-6 relative z-10">
            {/* Anurag University + InStacks Co-branding */}
            <div className="space-y-2">
              <div className="flex items-center gap-2">
                <span className="px-2.5 py-1 rounded-lg bg-amber-400 text-slate-950 text-[10px] font-black uppercase tracking-wider flex items-center gap-1 shadow-sm">
                  <GraduationCap size={13} />
                  ANURAG UNIVERSITY
                </span>
                <span className="px-2.5 py-1 rounded-lg bg-cyan-500/20 text-cyan-300 border border-cyan-400/30 text-[10px] font-extrabold uppercase tracking-wider">
                  InStacks Portal
                </span>
              </div>
              <h1 className="text-2xl sm:text-3xl font-black font-heading tracking-tight text-white leading-tight">
                Academic & Placement Readiness Portal
              </h1>
              <p className="text-xs text-slate-300 leading-relaxed">
                Official unified portal for students and faculty. Access curriculum notes, continuous coding assessments, AI doubts, and placement tracks.
              </p>
            </div>

            {/* InStacks Campus Highlights */}
            <div className="space-y-3 pt-2">
              <div className="flex items-start gap-2.5 text-xs text-slate-200 font-medium">
                <div className="w-5 h-5 rounded-md bg-cyan-400/10 text-cyan-400 flex items-center justify-center flex-shrink-0 mt-0.5 border border-cyan-400/20">
                  <Terminal size={12} />
                </div>
                <span><strong>Multi-Language Coding Labs:</strong> Real-time compilation and DSA evaluation.</span>
              </div>

              <div className="flex items-start gap-2.5 text-xs text-slate-200 font-medium">
                <div className="w-5 h-5 rounded-md bg-amber-400/10 text-amber-400 flex items-center justify-center flex-shrink-0 mt-0.5 border border-amber-400/20">
                  <Sparkles size={12} />
                </div>
                <span><strong>AI Doubt Assistant:</strong> Instant syllabus-aligned explanations 24/7.</span>
              </div>

              <div className="flex items-start gap-2.5 text-xs text-slate-200 font-medium">
                <div className="w-5 h-5 rounded-md bg-emerald-400/10 text-emerald-400 flex items-center justify-center flex-shrink-0 mt-0.5 border border-emerald-400/20">
                  <Cpu size={12} />
                </div>
                <span><strong>Company-Specific Practice:</strong> Curated interview roadmaps & tests.</span>
              </div>
            </div>

            {/* Database Connection Verification Box */}
            <div className="p-3 rounded-2xl bg-slate-900/80 border border-cyan-500/30 backdrop-blur-md space-y-1.5">
              <div className="flex items-center justify-between">
                <span className="text-[11px] font-bold text-cyan-300 flex items-center gap-1.5">
                  <Database size={13} className="text-emerald-400" />
                  Database Persistence Active
                </span>
                <span className="flex h-2 w-2 relative">
                  <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
                  <span className="relative inline-flex rounded-full h-2 w-2 bg-emerald-500"></span>
                </span>
              </div>
              <p className="text-[11px] text-slate-300 leading-normal">
                {connectionInfo.isLocal
                  ? 'Connected directly to your local database (users and credentials are saved to MySQL/H2).'
                  : 'Connected to active database backend.'}
              </p>
              <div className="text-[10px] font-mono text-cyan-200/70 pt-0.5">
                Target: {connectionInfo.url}
              </div>
            </div>
          </div>

          <div className="pt-6 relative z-10 text-xs text-slate-400 border-t border-white/10 flex items-center justify-between">
            <span>© Anurag University</span>
            <span>Powered by InStacks</span>
          </div>

          {/* Background Ambient Glows */}
          <div className="absolute -bottom-16 -right-16 w-64 h-64 rounded-full bg-cyan-500/15 blur-3xl pointer-events-none" />
          <div className="absolute -top-16 -left-16 w-64 h-64 rounded-full bg-indigo-500/15 blur-3xl pointer-events-none" />
        </div>

        {/* Right: Modern InStacks Auth Form (7 cols) */}
        <div className="lg:col-span-7 p-8 sm:p-12 flex flex-col justify-center space-y-6 bg-white dark:bg-[#0D1322]">
          
          <div className="flex items-center justify-between border-b border-slate-100 dark:border-slate-800 pb-4">
            <div>
              <div className="flex items-center gap-2">
                <h2 className="text-2xl font-black text-slate-900 dark:text-white font-heading">
                  Student & Faculty Login
                </h2>
                <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-indigo-50 dark:bg-indigo-950/70 text-indigo-600 dark:text-cyan-400 border border-indigo-200 dark:border-cyan-800">
                  AU-SSO
                </span>
              </div>
              <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
                Enter your institutional credentials to access your portal
              </p>
            </div>
          </div>

          {/* Quick Demo Credentials for Fast Evaluation */}
          <div className="p-3.5 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700/80">
            <div className="flex items-center justify-between mb-2">
              <span className="text-[11px] font-bold text-slate-600 dark:text-slate-300 uppercase tracking-wider flex items-center gap-1.5">
                <Sparkles size={13} className="text-amber-500" />
                Quick 1-Click Demo Login:
              </span>
              <span className="text-[10px] text-slate-400">Pre-seeded accounts</span>
            </div>
            <div className="flex flex-wrap gap-2">
              <button
                type="button"
                onClick={() => fillDemo('student.test2026@studyhub.edu', 'Password123#')}
                className="px-3 py-1.5 text-xs rounded-xl bg-indigo-100 dark:bg-indigo-950/80 text-indigo-700 dark:text-indigo-300 font-bold hover:bg-indigo-200 dark:hover:bg-indigo-900 transition-colors shadow-sm"
              >
                🎓 Student
              </button>
              <button
                type="button"
                onClick={() => fillDemo('faculty.sharma@studyhub.edu', 'Password123#')}
                className="px-3 py-1.5 text-xs rounded-xl bg-emerald-100 dark:bg-emerald-950/80 text-emerald-700 dark:text-emerald-300 font-bold hover:bg-emerald-200 dark:hover:bg-emerald-900 transition-colors shadow-sm"
              >
                👨‍🏫 Faculty
              </button>
              <button
                type="button"
                onClick={() => fillDemo('admin@studyhub.edu', 'AdminStudyHub#2026')}
                className="px-3 py-1.5 text-xs rounded-xl bg-purple-100 dark:bg-purple-950/80 text-purple-700 dark:text-purple-300 font-bold hover:bg-purple-200 dark:hover:bg-purple-900 transition-colors shadow-sm"
              >
                ⚡ Admin
              </button>
            </div>
          </div>

          {error && (
            <div className="p-3.5 rounded-2xl bg-rose-50 dark:bg-rose-950/60 border border-rose-200 dark:border-rose-800 text-rose-700 dark:text-rose-300 text-xs font-semibold flex items-center gap-2">
              <AlertCircle size={16} className="flex-shrink-0" />
              <span>{error}</span>
            </div>
          )}

          {forgotPasswordNotice && (
            <div className="p-3.5 rounded-2xl bg-indigo-50 dark:bg-indigo-950/60 border border-indigo-200 dark:border-indigo-800 text-indigo-700 dark:text-indigo-300 text-xs font-medium space-y-1">
              <div className="flex items-center gap-1.5 font-bold">
                <HelpCircle size={15} /> Password Assistance
              </div>
              <p>For password resets, contact your department coordinator or email <span className="underline font-mono">admin@studyhub.edu</span>.</p>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-1.5">
              <label className="text-xs font-bold text-slate-700 dark:text-slate-300 flex items-center gap-1.5">
                <Mail size={14} className="text-indigo-600 dark:text-cyan-400" />
                Institutional Email or Roll Number
              </label>
              <input
                type="text"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="e.g. 23AU1A0501@anurag.edu.in or student@studyhub.edu"
                className="w-full px-4 py-3 rounded-xl text-sm bg-slate-50 dark:bg-slate-800/80 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-slate-100 placeholder-slate-400 focus:border-indigo-600 dark:focus:border-cyan-400 focus:bg-white dark:focus:bg-slate-900 focus:outline-none transition-all"
              />
            </div>

            <div className="space-y-1.5">
              <div className="flex items-center justify-between">
                <label className="text-xs font-bold text-slate-700 dark:text-slate-300 flex items-center gap-1.5">
                  <Lock size={14} className="text-indigo-600 dark:text-cyan-400" />
                  Password
                </label>
                <button
                  type="button"
                  onClick={() => setForgotPasswordNotice(!forgotPasswordNotice)}
                  className="text-xs text-indigo-600 dark:text-cyan-400 hover:underline font-semibold"
                >
                  Forgot password?
                </button>
              </div>
              <div className="relative">
                <input
                  type={showPassword ? 'text' : 'password'}
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  className="w-full px-4 py-3 pr-11 rounded-xl text-sm bg-slate-50 dark:bg-slate-800/80 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-slate-100 placeholder-slate-400 focus:border-indigo-600 dark:focus:border-cyan-400 focus:bg-white dark:focus:bg-slate-900 focus:outline-none transition-all"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 dark:hover:text-slate-200"
                  aria-label={showPassword ? 'Hide password' : 'Show password'}
                >
                  {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-3.5 rounded-xl font-bold text-sm text-white bg-gradient-to-r from-indigo-600 to-blue-600 hover:from-indigo-700 hover:to-blue-700 dark:from-cyan-500 dark:to-indigo-600 dark:hover:from-cyan-600 dark:hover:to-indigo-700 shadow-lg shadow-indigo-600/25 transition-transform hover:scale-[1.01] active:scale-[0.99] disabled:opacity-60 flex items-center justify-center gap-2 mt-2"
            >
              {loading ? <RefreshCw className="animate-spin" size={16} /> : <LogIn size={16} />}
              {loading ? 'Authenticating with Database...' : 'Sign In to InStacks Portal'}
            </button>
          </form>

          <div className="pt-2 text-center text-xs text-slate-500 dark:text-slate-400 border-t border-slate-100 dark:border-slate-800 flex items-center justify-center gap-1.5">
            <span>New student at Anurag University?</span>
            <Link to="/register" className="font-bold text-indigo-600 dark:text-cyan-400 hover:underline flex items-center gap-1">
              Create Student Account <ArrowRight size={13} />
            </Link>
          </div>
        </div>

      </div>
    </div>
  );
};
export default LoginPage;
