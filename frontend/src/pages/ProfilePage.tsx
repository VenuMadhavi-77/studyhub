import React, { useState, useEffect } from 'react';
import {
  User as UserIcon,
  Mail,
  BookOpen,
  Layers,
  Award,
  Save,
  CheckCircle2,
  AlertCircle,
  Target,
  Globe,
  Flame,
  Sparkles,
  Lock,
  KeyRound,
  ShieldCheck,
  RefreshCw
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { Department, ContentLanguage, ContentLevel } from '../types';
import api from '../services/api';

const AVAILABLE_GOALS = [
  { id: 'placements', label: '💼 Placements & Coding Interviews', desc: 'DSA, Aptitude, LeetCode' },
  { id: 'semester exams', label: '📝 University Semester Exams', desc: 'Core notes & PYQs' },
  { id: 'competitive coding', label: '⚡ Competitive Programming', desc: 'CodeChef, Codeforces' },
  { id: 'projects', label: '🚀 Full Stack & Capstone Projects', desc: 'React, Java, Web Dev' },
  { id: 'higher studies', label: '🎓 GATE & Higher Studies', desc: 'Conceptual theory' },
];

export const ProfilePage: React.FC = () => {
  const { user, updateProfile } = useAuth();
  const isAdmin = user?.role === 'ADMIN';

  const [departments, setDepartments] = useState<Department[]>([]);
  const [fullName, setFullName] = useState(user?.fullName || '');
  const [departmentId, setDepartmentId] = useState<number | ''>(user?.departmentId || '');
  const [semester, setSemester] = useState<number | ''>(user?.semester || '');
  const [selectedGoals, setSelectedGoals] = useState<string[]>([]);
  const [preferredLanguage, setPreferredLanguage] = useState<ContentLanguage>('ENGLISH');
  const [level, setLevel] = useState<ContentLevel>('BEGINNER');

  const [saving, setSaving] = useState(false);
  const [successMsg, setSuccessMsg] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  // Password change state
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [changingPass, setChangingPass] = useState(false);
  const [passSuccess, setPassSuccess] = useState(false);
  const [passError, setPassError] = useState('');

  useEffect(() => {
    if (!isAdmin) {
      api.get('/taxonomy/departments')
        .then((res) => setDepartments(res.data.data || []))
        .catch((err) => console.error('Failed to load departments', err));
    }
  }, [isAdmin]);

  useEffect(() => {
    if (user) {
      setFullName(user.fullName || '');
      setDepartmentId(user.departmentId || '');
      setSemester(user.semester || '');
      if (user.goals) {
        setSelectedGoals(user.goals.split(',').map((g) => g.trim()).filter(Boolean));
      }
      if (user.preferredLanguage) {
        setPreferredLanguage(user.preferredLanguage);
      }
      if (user.level) {
        setLevel(user.level);
      }
    }
  }, [user]);

  const toggleGoal = (goalId: string) => {
    if (selectedGoals.includes(goalId)) {
      setSelectedGoals(selectedGoals.filter((g) => g !== goalId));
    } else {
      setSelectedGoals([...selectedGoals, goalId]);
    }
  };

  const handleProfileSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!fullName.trim()) return;

    setSaving(true);
    setErrorMsg('');
    setSuccessMsg(false);

    try {
      if (isAdmin) {
        await updateProfile({
          fullName: fullName.trim(),
        });
      } else {
        await updateProfile({
          fullName: fullName.trim(),
          departmentId: departmentId ? Number(departmentId) : undefined,
          semester: semester ? Number(semester) : undefined,
          goals: selectedGoals.join(','),
          preferredLanguage,
          level,
        });
      }
      setSuccessMsg(true);
      setTimeout(() => setSuccessMsg(false), 3000);
    } catch (err: any) {
      setErrorMsg(err.response?.data?.message || 'Failed to update profile');
    } finally {
      setSaving(false);
    }
  };

  const handlePasswordChange = async (e: React.FormEvent) => {
    e.preventDefault();
    setPassError('');
    setPassSuccess(false);

    if (newPassword.length < 8) {
      setPassError('New password must be at least 8 characters long');
      return;
    }
    if (newPassword !== confirmPassword) {
      setPassError('New passwords do not match');
      return;
    }

    setChangingPass(true);
    try {
      await api.put('/users/me/password', {
        currentPassword,
        newPassword
      });
      setPassSuccess(true);
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
      setTimeout(() => setPassSuccess(false), 3000);
    } catch (err: any) {
      setPassError(err.response?.data?.message || 'Failed to change password. Verify your current password.');
    } finally {
      setChangingPass(false);
    }
  };

  const getInitials = (name?: string) => {
    if (!name) return 'U';
    const parts = name.trim().split(' ');
    if (parts.length >= 2) return `${parts[0][0]}${parts[1][0]}`.toUpperCase();
    return name.slice(0, 2).toUpperCase();
  };

  return (
    <div className="max-w-4xl mx-auto space-y-8 pb-16">
      {/* Profile Header Card */}
      <div className="p-6 sm:p-8 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800 shadow-sm flex flex-col sm:flex-row items-center sm:items-start gap-6">
        <div className="w-20 h-20 rounded-3xl bg-gradient-to-tr from-indigo-600 via-violet-600 to-indigo-700 text-white font-black text-2xl flex items-center justify-center shadow-lg shadow-indigo-600/30 flex-shrink-0">
          {getInitials(user?.fullName)}
        </div>

        <div className="space-y-1.5 text-center sm:text-left flex-1">
          <div className="flex flex-wrap items-center justify-center sm:justify-start gap-2">
            <h1 className="text-2xl font-extrabold text-slate-900 dark:text-white font-heading">{user?.fullName}</h1>
            <span className={`px-2.5 py-0.5 rounded-full text-xs font-bold uppercase tracking-wider border ${
              isAdmin
                ? 'bg-rose-50 text-rose-700 dark:bg-rose-950 dark:text-rose-300 border-rose-200 dark:border-rose-800'
                : 'bg-indigo-50 text-indigo-700 dark:bg-indigo-950 dark:text-indigo-300 border-indigo-200 dark:border-indigo-800'
            }`}>
              {user?.role}
            </span>
          </div>

          <div className="flex flex-wrap items-center justify-center sm:justify-start gap-4 text-xs text-slate-500 dark:text-slate-400">
            <span className="flex items-center gap-1.5">
              <Mail size={14} /> {user?.email}
            </span>
            {!isAdmin && user?.departmentName && (
              <span className="flex items-center gap-1.5">
                <BookOpen size={14} /> {user?.departmentName}
              </span>
            )}
            {!isAdmin && user?.semester && (
              <span className="flex items-center gap-1.5">
                <Layers size={14} /> Semester {user?.semester}
              </span>
            )}
          </div>
        </div>
      </div>

      {/* Main Form */}
      <form onSubmit={handleProfileSubmit} className="space-y-6">
        {/* Basic Identity Card */}
        <div className="p-6 sm:p-8 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800 shadow-sm space-y-6">
          <div className="flex items-center gap-2.5 border-b border-slate-100 dark:border-slate-800 pb-4">
            <div className="w-8 h-8 rounded-xl bg-indigo-50 text-indigo-600 dark:bg-indigo-950 dark:text-indigo-400 flex items-center justify-center">
              <UserIcon size={18} />
            </div>
            <div>
              <h2 className="text-lg font-bold text-slate-900 dark:text-white font-heading">
                {isAdmin ? 'Administrator Details' : 'Student Academic Details'}
              </h2>
              <p className="text-xs text-slate-500 dark:text-slate-400">
                {isAdmin ? 'Manage your account name and credentials' : 'Your department and semester calibrate your personalized feed'}
              </p>
            </div>
          </div>

          {successMsg && (
            <div className="p-3.5 rounded-2xl bg-emerald-50 dark:bg-emerald-950/60 border border-emerald-200 text-emerald-700 dark:text-emerald-300 text-xs font-semibold flex items-center gap-2">
              <CheckCircle2 size={16} className="flex-shrink-0" />
              <span>Profile updated successfully!</span>
            </div>
          )}

          {errorMsg && (
            <div className="p-3.5 rounded-2xl bg-rose-50 dark:bg-rose-950/60 border border-rose-200 text-rose-700 dark:text-rose-300 text-xs font-semibold flex items-center gap-2">
              <AlertCircle size={16} className="flex-shrink-0" />
              <span>{errorMsg}</span>
            </div>
          )}

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="space-y-1.5">
              <label className="text-xs font-bold text-slate-700 dark:text-slate-300">Full Name</label>
              <input
                type="text"
                required
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                className="w-full px-4 py-2.5 rounded-xl text-sm bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-slate-100 focus:border-indigo-500 focus:bg-white dark:focus:bg-slate-900 focus:outline-none transition-all"
              />
            </div>

            <div className="space-y-1.5">
              <label className="text-xs font-bold text-slate-700 dark:text-slate-300">Institutional Email</label>
              <input
                type="email"
                disabled
                value={user?.email || ''}
                className="w-full px-4 py-2.5 rounded-xl text-sm bg-slate-100 dark:bg-slate-800/50 border border-slate-200 dark:border-slate-700 text-slate-500 cursor-not-allowed"
              />
            </div>

            {!isAdmin && (
              <>
                <div className="space-y-1.5">
                  <label className="text-xs font-bold text-slate-700 dark:text-slate-300">Department</label>
                  <select
                    value={departmentId}
                    onChange={(e) => setDepartmentId(e.target.value ? Number(e.target.value) : '')}
                    className="w-full px-4 py-2.5 rounded-xl text-sm bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-slate-100 focus:border-indigo-500 focus:bg-white dark:focus:bg-slate-900 focus:outline-none transition-all"
                  >
                    <option value="">Select Department</option>
                    {departments.map((d) => (
                      <option key={d.id} value={d.id}>
                        {d.name} ({d.code})
                      </option>
                    ))}
                  </select>
                </div>

                <div className="space-y-1.5">
                  <label className="text-xs font-bold text-slate-700 dark:text-slate-300">Current Semester</label>
                  <select
                    value={semester}
                    onChange={(e) => setSemester(e.target.value ? Number(e.target.value) : '')}
                    className="w-full px-4 py-2.5 rounded-xl text-sm bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-slate-100 focus:border-indigo-500 focus:bg-white dark:focus:bg-slate-900 focus:outline-none transition-all"
                  >
                    <option value="">Select Semester</option>
                    {[1, 2, 3, 4, 5, 6, 7, 8].map((s) => (
                      <option key={s} value={s}>
                        Semester {s}
                      </option>
                    ))}
                  </select>
                </div>
              </>
            )}
          </div>
        </div>

        {/* Student Study Goals & Personalization Preferences - Student Only */}
        {!isAdmin && (
          <div className="p-6 sm:p-8 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800 shadow-sm space-y-6">
            <div className="flex items-center gap-2.5 border-b border-slate-100 dark:border-slate-800 pb-4">
              <div className="w-8 h-8 rounded-xl bg-amber-50 text-amber-600 dark:bg-amber-950 dark:text-amber-400 flex items-center justify-center">
                <Target size={18} />
              </div>
              <div>
                <h2 className="text-lg font-bold text-slate-900 dark:text-white font-heading">Study Goals & Learning Path</h2>
                <p className="text-xs text-slate-500 dark:text-slate-400">
                  Select your primary objectives for personalized recommendations
                </p>
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              {AVAILABLE_GOALS.map((goal) => {
                const active = selectedGoals.includes(goal.id);
                return (
                  <button
                    key={goal.id}
                    type="button"
                    onClick={() => toggleGoal(goal.id)}
                    className={`p-3.5 rounded-2xl border text-left transition-all ${
                      active
                        ? 'border-indigo-600 bg-indigo-50/70 dark:bg-indigo-950/40 text-indigo-900 dark:text-indigo-200 shadow-sm'
                        : 'border-slate-200 dark:border-slate-800 hover:border-slate-300 dark:hover:border-slate-700 text-slate-700 dark:text-slate-300'
                    }`}
                  >
                    <div className="flex items-center justify-between">
                      <p className="text-xs font-bold">{goal.label}</p>
                      <div className={`w-4 h-4 rounded-md border flex items-center justify-center ${
                        active ? 'bg-indigo-600 border-indigo-600 text-white' : 'border-slate-300 dark:border-slate-600'
                      }`}>
                        {active && <CheckCircle2 size={12} />}
                      </div>
                    </div>
                    <p className="text-[11px] text-slate-500 dark:text-slate-400 mt-1">{goal.desc}</p>
                  </button>
                );
              })}
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-2 border-t border-slate-100 dark:border-slate-800">
              <div className="space-y-1.5">
                <label className="text-xs font-bold text-slate-700 dark:text-slate-300 flex items-center gap-1.5">
                  <Award size={14} /> Proficiency Level
                </label>
                <select
                  value={level}
                  onChange={(e) => setLevel(e.target.value as ContentLevel)}
                  className="w-full px-4 py-2.5 rounded-xl text-sm bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-slate-100 focus:border-indigo-500 focus:bg-white dark:focus:bg-slate-900 focus:outline-none transition-all"
                >
                  <option value="BEGINNER">Beginner (Foundations & Core concepts)</option>
                  <option value="INTERMEDIATE">Intermediate (Problem Solving & Projects)</option>
                  <option value="ADVANCED">Advanced (Placements & System Design)</option>
                </select>
              </div>

              <div className="space-y-1.5">
                <label className="text-xs font-bold text-slate-700 dark:text-slate-300 flex items-center gap-1.5">
                  <Globe size={14} /> Video / Course Language Preference
                </label>
                <select
                  value={preferredLanguage}
                  onChange={(e) => setPreferredLanguage(e.target.value as ContentLanguage)}
                  className="w-full px-4 py-2.5 rounded-xl text-sm bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-slate-100 focus:border-indigo-500 focus:bg-white dark:focus:bg-slate-900 focus:outline-none transition-all"
                >
                  <option value="ENGLISH">English (MIT OCW, Abdul Bari, NPTEL)</option>
                  <option value="HINDI">Hindi (Gate Smashers, Striver, PW, Love Babbar)</option>
                  <option value="OTHER">Any / Regional</option>
                </select>
              </div>
            </div>
          </div>
        )}

        <button
          type="submit"
          disabled={saving}
          className="px-6 py-3 rounded-2xl font-bold text-sm text-white bg-indigo-600 hover:bg-indigo-700 shadow-md shadow-indigo-600/25 transition-transform hover:scale-105 active:scale-95 disabled:opacity-60 flex items-center gap-2"
        >
          {saving ? <RefreshCw className="animate-spin" size={16} /> : <Save size={16} />}
          <span>{saving ? 'Saving...' : 'Save Profile Changes'}</span>
        </button>
      </form>

      {/* Change Password Card */}
      <div className="p-6 sm:p-8 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800 shadow-sm space-y-6">
        <div className="flex items-center gap-2.5 border-b border-slate-100 dark:border-slate-800 pb-4">
          <div className="w-8 h-8 rounded-xl bg-violet-50 text-violet-600 dark:bg-violet-950 dark:text-violet-400 flex items-center justify-center">
            <KeyRound size={18} />
          </div>
          <div>
            <h2 className="text-lg font-bold text-slate-900 dark:text-white font-heading">Security & Password</h2>
            <p className="text-xs text-slate-500 dark:text-slate-400">Update your account password</p>
          </div>
        </div>

        {passSuccess && (
          <div className="p-3.5 rounded-2xl bg-emerald-50 dark:bg-emerald-950/60 border border-emerald-200 text-emerald-700 dark:text-emerald-300 text-xs font-semibold flex items-center gap-2">
            <CheckCircle2 size={16} className="flex-shrink-0" />
            <span>Password changed successfully!</span>
          </div>
        )}

        {passError && (
          <div className="p-3.5 rounded-2xl bg-rose-50 dark:bg-rose-950/60 border border-rose-200 text-rose-700 dark:text-rose-300 text-xs font-semibold flex items-center gap-2">
            <AlertCircle size={16} className="flex-shrink-0" />
            <span>{passError}</span>
          </div>
        )}

        <form onSubmit={handlePasswordChange} className="space-y-4 max-w-md">
          <div className="space-y-1.5">
            <label className="text-xs font-bold text-slate-700 dark:text-slate-300">Current Password</label>
            <input
              type="password"
              required
              value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)}
              placeholder="••••••••"
              className="w-full px-4 py-2.5 rounded-xl text-sm bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-slate-100 focus:border-indigo-500 focus:bg-white dark:focus:bg-slate-900 focus:outline-none transition-all"
            />
          </div>

          <div className="space-y-1.5">
            <label className="text-xs font-bold text-slate-700 dark:text-slate-300">New Password (min 8 characters)</label>
            <input
              type="password"
              required
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              placeholder="••••••••"
              className="w-full px-4 py-2.5 rounded-xl text-sm bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-slate-100 focus:border-indigo-500 focus:bg-white dark:focus:bg-slate-900 focus:outline-none transition-all"
            />
          </div>

          <div className="space-y-1.5">
            <label className="text-xs font-bold text-slate-700 dark:text-slate-300">Confirm New Password</label>
            <input
              type="password"
              required
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              placeholder="••••••••"
              className="w-full px-4 py-2.5 rounded-xl text-sm bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-slate-100 focus:border-indigo-500 focus:bg-white dark:focus:bg-slate-900 focus:outline-none transition-all"
            />
          </div>

          <button
            type="submit"
            disabled={changingPass}
            className="px-5 py-2.5 rounded-xl font-bold text-xs text-white bg-slate-900 hover:bg-slate-800 dark:bg-slate-700 dark:hover:bg-slate-600 transition-colors disabled:opacity-60 flex items-center gap-2"
          >
            {changingPass ? <RefreshCw className="animate-spin" size={14} /> : <Lock size={14} />}
            <span>{changingPass ? 'Updating...' : 'Change Password'}</span>
          </button>
        </form>
      </div>
    </div>
  );
};
export default ProfilePage;
