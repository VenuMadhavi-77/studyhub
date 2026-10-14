import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  BookOpen,
  UserPlus,
  AlertCircle,
  RefreshCw,
  CheckCircle2,
  Mail,
  Lock,
  User,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { Department } from '../types';
import api from '../services/api';

const FALLBACK_DEPARTMENTS: Department[] = [
  { id: 1, name: 'Computer Science & Engineering', code: 'CSE', active: true },
  { id: 2, name: 'Information Technology', code: 'IT', active: true },
  { id: 3, name: 'Electronics & Communication Engineering', code: 'ECE', active: true },
  { id: 4, name: 'Mechanical Engineering', code: 'MECH', active: true }
];

export const RegisterPage: React.FC = () => {
  const navigate = useNavigate();
  const { register } = useAuth();

  const [departments, setDepartments] = useState<Department[]>([]);
  const [departmentsLoading, setDepartmentsLoading] = useState(true);
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role] = useState('STUDENT');
  const [departmentId, setDepartmentId] = useState<number | ''>('');
  const [semester, setSemester] = useState<number | ''>('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    let isMounted = true;
    setDepartmentsLoading(true);

    api.get('/taxonomy/departments')
      .then((res) => {
        if (!isMounted) return;
        const list = res.data?.data;
        if (Array.isArray(list) && list.length > 0) {
          setDepartments(list);
        } else {
          setDepartments(FALLBACK_DEPARTMENTS);
        }
      })
      .catch((err) => {
        if (!isMounted) return;
        console.warn('Backend departments fetch failed, using academic departments fallback:', err);
        setDepartments(FALLBACK_DEPARTMENTS);
      })
      .finally(() => {
        if (isMounted) setDepartmentsLoading(false);
      });

    return () => {
      isMounted = false;
    };
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (password.length < 8) {
      setError('Password must be at least 8 characters long');
      return;
    }

    if (!departmentId) {
      setError('Please select your Department');
      return;
    }

    if (!semester) {
      setError('Please select your Semester');
      return;
    }

    setLoading(true);

    try {
      const user = await register(
        fullName.trim(),
        email.trim(),
        password,
        role,
        Number(departmentId),
        Number(semester)
      );

      if (user.role === 'ADMIN') {
        navigate('/admin');
      } else {
        navigate('/dashboard');
      }
    } catch (err: any) {
      const resp = err.response?.data;
      if (resp?.validationErrors && typeof resp.validationErrors === 'object') {
        const firstError = Object.values(resp.validationErrors)[0];
        setError(String(firstError));
      } else if (resp?.details && typeof resp.details === 'object') {
        const firstError = Object.values(resp.details)[0];
        setError(String(firstError));
      } else if (resp?.message) {
        setError(resp.message);
      } else {
        setError('Registration failed. Institutional email may already be registered or details are invalid.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[calc(100vh-4.25rem)] flex items-center justify-center p-4 sm:p-6 lg:p-8">
      <div className="max-w-4xl w-full grid grid-cols-1 md:grid-cols-2 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800 shadow-2xl overflow-hidden">

        {/* Left: Branding */}
        <div className="p-8 sm:p-10 bg-gradient-to-br from-indigo-900 via-indigo-800 to-violet-950 text-white flex flex-col justify-between relative overflow-hidden">
          <div className="space-y-6 relative z-10">
            <div className="flex items-center gap-2.5">
              <div className="w-10 h-10 rounded-2xl bg-white/15 backdrop-blur-md flex items-center justify-center border border-white/20">
                <BookOpen size={22} className="text-amber-400" />
              </div>
              <span className="text-xl font-extrabold font-heading tracking-tight">
                StudyHub
              </span>
            </div>

            <div className="space-y-2">
              <h2 className="text-2xl font-bold tracking-tight font-heading">
                Join the Campus Learning Community
              </h2>
              <p className="text-xs sm:text-sm text-indigo-200 leading-relaxed">
                Create your student profile with your department and semester
                to get automated custom recommendations, note sharing, and
                study room access.
              </p>
            </div>

            <div className="space-y-3 pt-2">
              <div className="flex items-center gap-2.5 text-xs text-indigo-100 font-medium">
                <CheckCircle2 size={16} className="text-amber-400 flex-shrink-0" />
                <span>Personalized algorithmic feed for your semester subjects</span>
              </div>
              <div className="flex items-center gap-2.5 text-xs text-indigo-100 font-medium">
                <CheckCircle2 size={16} className="text-amber-400 flex-shrink-0" />
                <span>Real-time collaborative study whiteboard and doubt forum</span>
              </div>
              <div className="flex items-center gap-2.5 text-xs text-indigo-100 font-medium">
                <CheckCircle2 size={16} className="text-amber-400 flex-shrink-0" />
                <span>Focus timer with streaks, sounds, and campus leaderboard</span>
              </div>
            </div>
          </div>

          <div className="pt-6 relative z-10 text-xs text-indigo-300">
            Already have an account?{' '}
            <Link to="/login" className="font-bold text-white hover:underline ml-1">
              Sign In →
            </Link>
          </div>

          <div className="absolute -bottom-16 -right-16 w-64 h-64 rounded-full bg-indigo-500/20 blur-3xl pointer-events-none" />
        </div>

        {/* Right: Registration Form */}
        <div className="p-8 sm:p-10 flex flex-col justify-center space-y-5 bg-white dark:bg-slate-900">
          <div className="space-y-1">
            <h2 className="text-2xl font-bold text-slate-900 dark:text-white font-heading">
              Student Registration
            </h2>
            <p className="text-xs text-slate-500 dark:text-slate-400">
              Fill in your details to start learning
            </p>
          </div>

          {error && (
            <div className="p-3.5 rounded-2xl bg-rose-50 dark:bg-rose-950/60 border border-rose-200 text-rose-700 dark:text-rose-300 text-xs font-semibold flex items-center gap-2">
              <AlertCircle size={16} className="flex-shrink-0" />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-3.5">
            <div className="space-y-1">
              <label className="text-xs font-bold text-slate-700 dark:text-slate-300 flex items-center gap-1.5">
                <User size={13} /> Full Name
              </label>
              <input
                type="text"
                required
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                placeholder="e.g. Alex Rivera"
                className="w-full px-3.5 py-2 text-sm rounded-xl bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-slate-100 placeholder-slate-400 focus:border-indigo-500 focus:outline-none"
              />
            </div>

            <div className="space-y-1">
              <label className="text-xs font-bold text-slate-700 dark:text-slate-300 flex items-center gap-1.5">
                <Mail size={13} /> Institutional Email
              </label>
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="e.g. alex@studyhub.edu"
                className="w-full px-3.5 py-2 text-sm rounded-xl bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-slate-100 placeholder-slate-400 focus:border-indigo-500 focus:outline-none"
              />
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1">
                <label className="text-xs font-bold text-slate-700 dark:text-slate-300">
                  Department
                </label>
                <select
                  required
                  value={departmentId}
                  disabled={departmentsLoading && departments.length === 0}
                  onChange={(e) =>
                    setDepartmentId(
                      e.target.value ? Number(e.target.value) : ''
                    )
                  }
                  className="w-full px-3 py-2 text-xs rounded-xl bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-slate-100 focus:border-indigo-500 focus:outline-none disabled:opacity-60"
                >
                  <option value="">
                    {departmentsLoading && departments.length === 0
                      ? 'Loading departments...'
                      : 'Select Dept'}
                  </option>
                  {departments.map((d) => (
                    <option key={d.id} value={d.id}>
                      {d.code} - {d.name}
                    </option>
                  ))}
                </select>
              </div>

              <div className="space-y-1">
                <label className="text-xs font-bold text-slate-700 dark:text-slate-300">
                  Semester
                </label>
                <select
                  required
                  value={semester}
                  onChange={(e) =>
                    setSemester(e.target.value ? Number(e.target.value) : '')
                  }
                  className="w-full px-3 py-2 text-xs rounded-xl bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-slate-100 focus:border-indigo-500 focus:outline-none"
                >
                  <option value="">Semester</option>
                  {[1, 2, 3, 4, 5, 6, 7, 8].map((s) => (
                    <option key={s} value={s}>
                      Semester {s}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <div className="space-y-1">
              <label className="text-xs font-bold text-slate-700 dark:text-slate-300 flex items-center gap-1.5">
                <Lock size={13} /> Password (min 8 chars)
              </label>
              <input
                type="password"
                required
                minLength={8}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                className="w-full px-3.5 py-2 text-sm rounded-xl bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-slate-100 placeholder-slate-400 focus:border-indigo-500 focus:outline-none"
              />
            </div>

            <button
              type="submit"
              disabled={loading || (departmentsLoading && departments.length === 0)}
              className="w-full py-2.5 rounded-xl font-bold text-sm text-white bg-indigo-600 hover:bg-indigo-700 shadow-md shadow-indigo-600/30 transition-transform hover:scale-[1.02] active:scale-[0.98] disabled:opacity-60 flex items-center justify-center gap-2 mt-3"
            >
              {loading ? (
                <RefreshCw className="animate-spin" size={16} />
              ) : (
                <UserPlus size={16} />
              )}
              {loading ? 'Creating Account...' : 'Create Student Account'}
            </button>
          </form>

          <div className="pt-2 text-center text-xs text-slate-500 dark:text-slate-400">
            Already registered?{' '}
            <Link to="/login" className="font-bold text-indigo-600 hover:text-indigo-700 ml-1">
              Sign In here →
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};
