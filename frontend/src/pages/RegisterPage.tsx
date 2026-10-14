import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  UserPlus,
  AlertCircle,
  RefreshCw,
  CheckCircle2,
  Mail,
  Lock,
  User,
  GraduationCap,
  Building2,
  Calendar,
  ArrowRight
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { Department } from '../types';
import api from '../services/api';

const FALLBACK_DEPARTMENTS: Department[] = [
  { id: 1, name: 'Computer Science & Engineering', code: 'CSE', active: true },
  { id: 2, name: 'Information Technology', code: 'IT', active: true },
  { id: 3, name: 'Artificial Intelligence & Machine Learning', code: 'AIML', active: true },
  { id: 4, name: 'Data Science', code: 'DS', active: true },
  { id: 5, name: 'Electronics & Communication Engineering', code: 'ECE', active: true },
  { id: 6, name: 'Mechanical Engineering', code: 'MECH', active: true },
  { id: 7, name: 'Civil Engineering', code: 'CIVIL', active: true }
];

export const RegisterPage: React.FC = () => {
  const navigate = useNavigate();
  const { register } = useAuth();

  const [departments, setDepartments] = useState<Department[]>(FALLBACK_DEPARTMENTS);
  const [departmentsLoading, setDepartmentsLoading] = useState(false);
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role] = useState('STUDENT');
  const [departmentId, setDepartmentId] = useState<number | ''>(1);
  const [semester, setSemester] = useState<number | ''>(3);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [successNotice, setSuccessNotice] = useState(false);

  useEffect(() => {
    let isMounted = true;
    api.get('/taxonomy/departments')
      .then((res) => {
        if (!isMounted) return;
        const list = res.data?.data;
        if (Array.isArray(list) && list.length > 0) {
          setDepartments(list);
          if (!departmentId) {
            setDepartmentId(list[0].id);
          }
        }
      })
      .catch((err) => {
        if (!isMounted) return;
        console.warn('Backend departments fetch info:', err);
      });

    return () => {
      isMounted = false;
    };
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (password.length < 6) {
      setError('Password must be at least 6 characters long');
      return;
    }

    setLoading(true);

    try {
      const user = await register(
        fullName.trim(),
        email.trim(),
        password,
        role,
        departmentId ? Number(departmentId) : undefined,
        semester ? Number(semester) : 1
      );

      setSuccessNotice(true);
      setTimeout(() => {
        if (user?.role === 'ADMIN') {
          navigate('/admin');
        } else {
          navigate('/dashboard');
        }
      }, 600);
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
      } else if (err.code === 'ECONNABORTED' || err.message?.includes('Network Error') || !err.response) {
        setError('Cannot connect to backend server. Please verify your connection.');
      } else {
        setError('Registration failed. This email may already be registered.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[calc(100vh-4.25rem)] flex items-center justify-center p-4 sm:p-6 lg:p-8 bg-slate-50 dark:bg-[#070B14]">
      <div className="max-w-5xl w-full grid grid-cols-1 lg:grid-cols-12 rounded-3xl bg-white dark:bg-[#0D1322] border border-slate-200/90 dark:border-slate-800 shadow-2xl overflow-hidden">

        {/* Left: StudyHub Information Panel (5 cols) */}
        <div className="lg:col-span-5 p-8 sm:p-10 bg-gradient-to-br from-[#0A1128] via-[#001F54] to-[#121B3A] text-white flex flex-col justify-between relative overflow-hidden">
          <div className="space-y-6 relative z-10">
            <div className="space-y-3">
              <div className="flex items-center gap-2">
                <span className="px-2.5 py-1 rounded-lg bg-cyan-500/20 text-cyan-300 border border-cyan-400/30 text-[10px] font-extrabold uppercase tracking-wider flex items-center gap-1.5 shadow-sm">
                  <GraduationCap size={14} className="text-amber-400" />
                  StudyHub
                </span>
                <span className="px-2.5 py-1 rounded-lg bg-white/10 text-slate-200 text-[10px] font-bold uppercase tracking-wider">
                  Academic Portal
                </span>
              </div>
              <h1 className="text-2xl sm:text-3xl font-black font-heading tracking-tight text-white leading-tight">
                Student Account Registration
              </h1>
              <p className="text-xs text-slate-300 leading-relaxed">
                Join your university academic knowledge repository to access department syllabus, peer-reviewed notes, AI doubt resolution, and collaborative study rooms.
              </p>
            </div>

            <div className="space-y-3 pt-2">
              <div className="flex items-center gap-2.5 text-xs text-slate-200 font-medium">
                <CheckCircle2 size={16} className="text-cyan-400 flex-shrink-0" />
                <span>Personalized curriculum matched to your department & semester</span>
              </div>
              <div className="flex items-center gap-2.5 text-xs text-slate-200 font-medium">
                <CheckCircle2 size={16} className="text-cyan-400 flex-shrink-0" />
                <span>Peer-reviewed notes, lab solutions & previous exams</span>
              </div>
              <div className="flex items-center gap-2.5 text-xs text-slate-200 font-medium">
                <CheckCircle2 size={16} className="text-cyan-400 flex-shrink-0" />
                <span>24/7 AI tutor for instant doubt explanations and formula sheets</span>
              </div>
              <div className="flex items-center gap-2.5 text-xs text-slate-200 font-medium">
                <CheckCircle2 size={16} className="text-cyan-400 flex-shrink-0" />
                <span>Synchronized study whiteboard and collaborative focus rooms</span>
              </div>
            </div>
          </div>

          <div className="pt-8 relative z-10 text-xs text-slate-400 border-t border-white/10 flex items-center justify-between">
            <span>Already registered?</span>
            <Link to="/login" className="font-bold text-cyan-400 hover:underline">
              Sign In to Portal →
            </Link>
          </div>

          {/* Background Ambient Glows */}
          <div className="absolute -bottom-16 -right-16 w-64 h-64 rounded-full bg-cyan-500/15 blur-3xl pointer-events-none" />
          <div className="absolute -top-16 -left-16 w-64 h-64 rounded-full bg-indigo-500/15 blur-3xl pointer-events-none" />
        </div>

        {/* Right: Registration Form (7 cols) */}
        <div className="lg:col-span-7 p-8 sm:p-12 flex flex-col justify-center space-y-5 bg-white dark:bg-[#0D1322]">
          <div className="space-y-1 border-b border-slate-100 dark:border-slate-800 pb-3">
            <h2 className="text-2xl font-black text-slate-900 dark:text-white font-heading">
              Create Student Profile
            </h2>
            <p className="text-xs text-slate-500 dark:text-slate-400">
              Provide your details to register and save your record into the database
            </p>
          </div>

          {successNotice && (
            <div className="p-3.5 rounded-2xl bg-emerald-50 dark:bg-emerald-950/60 border border-emerald-300 dark:border-emerald-800 text-emerald-700 dark:text-emerald-300 text-xs font-bold flex items-center gap-2">
              <CheckCircle2 size={16} className="flex-shrink-0" />
              <span>User successfully stored in database! Redirecting to your student dashboard...</span>
            </div>
          )}

          {error && (
            <div className="p-3.5 rounded-2xl bg-rose-50 dark:bg-rose-950/60 border border-rose-200 dark:border-rose-800 text-rose-700 dark:text-rose-300 text-xs font-semibold flex items-center gap-2">
              <AlertCircle size={16} className="flex-shrink-0" />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-3.5">
            <div className="space-y-1">
              <label className="text-xs font-bold text-slate-700 dark:text-slate-300 flex items-center gap-1.5">
                <User size={13} className="text-indigo-600 dark:text-cyan-400" /> Full Name
              </label>
              <input
                type="text"
                required
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                placeholder="e.g. Rahul Sharma"
                className="w-full px-4 py-2.5 text-sm rounded-xl bg-slate-50 dark:bg-slate-800/80 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-slate-100 placeholder-slate-400 focus:border-indigo-600 dark:focus:border-cyan-400 focus:bg-white dark:focus:bg-slate-900 focus:outline-none transition-all"
              />
            </div>

            <div className="space-y-1">
              <label className="text-xs font-bold text-slate-700 dark:text-slate-300 flex items-center gap-1.5">
                <Mail size={13} className="text-indigo-600 dark:text-cyan-400" /> University Email
              </label>
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="e.g. student@university.edu"
                className="w-full px-4 py-2.5 text-sm rounded-xl bg-slate-50 dark:bg-slate-800/80 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-slate-100 placeholder-slate-400 focus:border-indigo-600 dark:focus:border-cyan-400 focus:bg-white dark:focus:bg-slate-900 focus:outline-none transition-all"
              />
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div className="space-y-1">
                <label className="text-xs font-bold text-slate-700 dark:text-slate-300 flex items-center gap-1.5">
                  <Building2 size={13} className="text-indigo-600 dark:text-cyan-400" /> Department
                </label>
                <select
                  required
                  value={departmentId}
                  onChange={(e) => setDepartmentId(e.target.value ? Number(e.target.value) : '')}
                  className="w-full px-3.5 py-2.5 text-xs rounded-xl bg-slate-50 dark:bg-slate-800/80 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-slate-100 focus:border-indigo-600 dark:focus:border-cyan-400 focus:outline-none"
                >
                  {departments.map((d) => (
                    <option key={d.id} value={d.id}>
                      {d.code} - {d.name}
                    </option>
                  ))}
                </select>
              </div>

              <div className="space-y-1">
                <label className="text-xs font-bold text-slate-700 dark:text-slate-300 flex items-center gap-1.5">
                  <Calendar size={13} className="text-indigo-600 dark:text-cyan-400" /> Current Semester
                </label>
                <select
                  required
                  value={semester}
                  onChange={(e) => setSemester(e.target.value ? Number(e.target.value) : '')}
                  className="w-full px-3.5 py-2.5 text-xs rounded-xl bg-slate-50 dark:bg-slate-800/80 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-slate-100 focus:border-indigo-600 dark:focus:border-cyan-400 focus:outline-none"
                >
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
                <Lock size={13} className="text-indigo-600 dark:text-cyan-400" /> Password (min 6 chars)
              </label>
              <input
                type="password"
                required
                minLength={6}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                className="w-full px-4 py-2.5 text-sm rounded-xl bg-slate-50 dark:bg-slate-800/80 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-slate-100 placeholder-slate-400 focus:border-indigo-600 dark:focus:border-cyan-400 focus:bg-white dark:focus:bg-slate-900 focus:outline-none transition-all"
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-3.5 rounded-xl font-bold text-sm text-white bg-gradient-to-r from-indigo-600 to-blue-600 hover:from-indigo-700 hover:to-blue-700 dark:from-cyan-500 dark:to-indigo-600 dark:hover:from-cyan-600 dark:hover:to-indigo-700 shadow-lg shadow-indigo-600/25 transition-transform hover:scale-[1.01] active:scale-[0.99] disabled:opacity-60 flex items-center justify-center gap-2 mt-4"
            >
              {loading ? (
                <RefreshCw className="animate-spin" size={16} />
              ) : (
                <UserPlus size={16} />
              )}
              {loading ? 'Creating Student Account...' : 'Create Student Account'}
            </button>
          </form>

          <div className="pt-2 text-center text-xs text-slate-500 dark:text-slate-400 border-t border-slate-100 dark:border-slate-800 flex items-center justify-center gap-1.5">
            <span>Already have an account?</span>
            <Link to="/login" className="font-bold text-indigo-600 dark:text-cyan-400 hover:underline flex items-center gap-1">
              Sign In to StudyHub <ArrowRight size={13} />
            </Link>
          </div>
        </div>

      </div>
    </div>
  );
};
export default RegisterPage;
