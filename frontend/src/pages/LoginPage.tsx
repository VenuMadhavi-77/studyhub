import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  BookOpen,
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
  HelpCircle
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [forgotPasswordNotice, setForgotPasswordNotice] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const user = await login(email.trim(), password.trim());
      if (user.role === 'ADMIN') {
        navigate('/admin');
      } else {
        navigate('/dashboard');
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Invalid institutional email or password');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[calc(100vh-4.25rem)] flex items-center justify-center p-4 sm:p-6 lg:p-8">
      <div className="max-w-4xl w-full grid grid-cols-1 md:grid-cols-2 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800 shadow-2xl overflow-hidden">
        
        {/* Left: EdTech Brand Showcase Banner */}
        <div className="p-8 sm:p-10 bg-gradient-to-br from-indigo-900 via-indigo-800 to-violet-950 text-white flex flex-col justify-between relative overflow-hidden">
          <div className="space-y-6 relative z-10">
            <div className="flex items-center gap-2.5">
              <div className="w-10 h-10 rounded-2xl bg-white/15 backdrop-blur-md flex items-center justify-center border border-white/20">
                <BookOpen size={22} className="text-amber-400" />
              </div>
              <span className="text-xl font-extrabold font-heading tracking-tight">StudyHub</span>
            </div>

            <div className="space-y-2">
              <h2 className="text-2xl sm:text-3xl font-extrabold font-heading leading-tight">
                Level Up Your Engineering Journey
              </h2>
              <p className="text-xs sm:text-sm text-indigo-200 leading-relaxed">
                Access curated multi-platform video courses, verified notes, AI summaries, and collaborative focus study rooms.
              </p>
            </div>

            <div className="space-y-3 pt-2">
              <div className="flex items-center gap-2.5 text-xs text-indigo-100 font-medium">
                <CheckCircle2 size={16} className="text-amber-400 flex-shrink-0" />
                <span>Curated YouTube, PW Skills, LeetCode & NPTEL Catalog</span>
              </div>
              <div className="flex items-center gap-2.5 text-xs text-indigo-100 font-medium">
                <CheckCircle2 size={16} className="text-amber-400 flex-shrink-0" />
                <span>Bayesian Reliability Quality Ratings on Peer Materials</span>
              </div>
              <div className="flex items-center gap-2.5 text-xs text-indigo-100 font-medium">
                <CheckCircle2 size={16} className="text-amber-400 flex-shrink-0" />
                <span>Focus Timer with Server-Verified Streaks & Ambient Rain Audio</span>
              </div>
            </div>
          </div>



          {/* Background Glow */}
          <div className="absolute -bottom-16 -right-16 w-64 h-64 rounded-full bg-indigo-500/20 blur-3xl pointer-events-none" />
        </div>

        {/* Right: Login Form */}
        <div className="p-8 sm:p-10 flex flex-col justify-center space-y-6 bg-white dark:bg-slate-900">
          <div className="space-y-1">
            <h2 className="text-2xl font-bold text-slate-900 dark:text-white font-heading">Sign In</h2>
            <p className="text-xs text-slate-500 dark:text-slate-400">
              Access your personalized dashboard, bookmarks, and streaks
            </p>
          </div>

          {error && (
            <div className="p-3.5 rounded-2xl bg-rose-50 dark:bg-rose-950/60 border border-rose-200 text-rose-700 dark:text-rose-300 text-xs font-semibold flex items-center gap-2">
              <AlertCircle size={16} className="flex-shrink-0" />
              <span>{error}</span>
            </div>
          )}

          {forgotPasswordNotice && (
            <div className="p-3.5 rounded-2xl bg-indigo-50 dark:bg-indigo-950/60 border border-indigo-200 text-indigo-700 dark:text-indigo-300 text-xs font-medium space-y-1">
              <div className="flex items-center gap-1.5 font-bold">
                <HelpCircle size={15} /> Password Reset Instructions
              </div>
              <p>Please contact your department platform administrator at <span className="underline font-mono">admin@studyhub.edu</span> or update your password in your profile after logging in.</p>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-1.5">
              <label className="text-xs font-bold text-slate-700 dark:text-slate-300 flex items-center gap-1.5">
                <Mail size={14} /> Institutional Email
              </label>
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="e.g. alex@studyhub.edu"
                className="w-full px-4 py-2.5 rounded-xl text-sm bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-slate-100 placeholder-slate-400 focus:border-indigo-500 focus:bg-white dark:focus:bg-slate-900 focus:outline-none transition-all"
              />
            </div>

            <div className="space-y-1.5">
              <div className="flex items-center justify-between">
                <label className="text-xs font-bold text-slate-700 dark:text-slate-300 flex items-center gap-1.5">
                  <Lock size={14} /> Password
                </label>
                <button
                  type="button"
                  onClick={() => setForgotPasswordNotice(!forgotPasswordNotice)}
                  className="text-xs text-indigo-600 hover:text-indigo-700 font-semibold"
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
                  className="w-full px-4 py-2.5 pr-10 rounded-xl text-sm bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-slate-100 placeholder-slate-400 focus:border-indigo-500 focus:bg-white dark:focus:bg-slate-900 focus:outline-none transition-all"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 dark:hover:text-slate-200"
                  aria-label={showPassword ? 'Hide password' : 'Show password'}
                >
                  {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-3 rounded-xl font-bold text-sm text-white bg-indigo-600 hover:bg-indigo-700 shadow-lg shadow-indigo-600/30 transition-transform hover:scale-[1.02] active:scale-[0.98] disabled:opacity-60 flex items-center justify-center gap-2 mt-2"
            >
              {loading ? <RefreshCw className="animate-spin" size={16} /> : <LogIn size={16} />}
              {loading ? 'Signing in...' : 'Sign In to StudyHub'}
            </button>
          </form>

          <div className="pt-2 text-center text-xs text-slate-500 dark:text-slate-400 border-t border-slate-100 dark:border-slate-800">
            Don't have an account?{' '}
            <Link to="/register" className="font-bold text-indigo-600 hover:text-indigo-700 ml-1">
              Create student account →
            </Link>
          </div>
        </div>

      </div>
    </div>
  );
};
export default LoginPage;
