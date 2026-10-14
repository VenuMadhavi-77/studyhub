import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  Sparkles,
  Flame,
  Timer,
  BookOpen,
  ArrowRight,
  TrendingUp,
  Bookmark,
  Briefcase,
  Layers,
  ChevronRight,
  Award,
  Clock,
  Compass,
  Zap,
  GraduationCap,
  Code2,
  Terminal,
  Cpu,
  CheckCircle2,
  Database
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { getRecommendations, getResources, getFocusStats, toggleBookmark, getConnectionInfo } from '../services/api';
import { Resource, ResourceRecommendation, FocusStats } from '../types';
import { PlatformBadge, LevelBadge } from '../components/Badge';
import { ReliabilityChip } from '../components/ReliabilityChip';
import { StarRating } from '../components/StarRating';
import { ResourceCardSkeleton } from '../components/Skeleton';
import { EmptyState } from '../components/EmptyState';

export const DashboardPage: React.FC = () => {
  const { user } = useAuth();
  const [recommendations, setRecommendations] = useState<ResourceRecommendation[]>([]);
  const [placementResources, setPlacementResources] = useState<Resource[]>([]);
  const [trendingResources, setTrendingResources] = useState<Resource[]>([]);
  const [focusStats, setFocusStats] = useState<FocusStats | null>(null);
  const [loading, setLoading] = useState(true);

  const connectionInfo = getConnectionInfo();

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      setLoading(true);
      const [recs, placements, trending, focus] = await Promise.all([
        getRecommendations().catch(() => []),
        getResources({ query: 'placement' }).then((r) => r.content || []).catch(() => []),
        getResources({ page: 0, size: 6 }).then((r) => r.content || []).catch(() => []),
        getFocusStats().catch(() => null),
      ]);

      setRecommendations(recs);
      setPlacementResources(placements.slice(0, 4));
      setTrendingResources(trending.slice(0, 4));
      setFocusStats(focus);
    } catch (err) {
      console.error('Failed to load dashboard', err);
    } finally {
      setLoading(false);
    }
  };

  const handleBookmarkToggle = async (id: number) => {
    try {
      await toggleBookmark(id);
      loadDashboardData();
    } catch (err) {
      console.error('Failed to toggle bookmark', err);
    }
  };

  const subjects = [
    { name: 'Data Structures & Algorithms', code: 'CS201', progress: '78%', color: 'from-cyan-500 to-blue-600', icon: '⚡' },
    { name: 'Database Management Systems', code: 'CS301', progress: '85%', color: 'from-emerald-500 to-teal-600', icon: '🗄️' },
    { name: 'Operating Systems & Architecture', code: 'CS302', progress: '64%', color: 'from-amber-500 to-orange-600', icon: '⚙️' },
    { name: 'Computer Networks & Protocols', code: 'CS303', progress: '72%', color: 'from-indigo-500 to-violet-600', icon: '🌐' },
    { name: 'Full Stack Web & Cloud Computing', code: 'IT301', progress: '91%', color: 'from-rose-500 to-pink-600', icon: '🚀' },
    { name: 'Campus Placement Prep & Aptitude', code: 'GEN101', progress: '80%', color: 'from-purple-500 to-indigo-600', icon: '💼' },
  ];

  const rollNumber = user?.id ? `23AU1A05${String(user.id).padStart(2, '0')}` : '23AU1A0501';

  return (
    <div className="space-y-8 max-w-7xl mx-auto pb-12">
      {/* 1. Anurag InStacks Student Portal Header Banner */}
      <div className="relative overflow-hidden rounded-3xl bg-gradient-to-r from-[#0A1128] via-[#001F54] to-[#121B3A] text-white p-6 sm:p-8 shadow-xl border border-cyan-500/20">
        <div className="relative z-10 max-w-3xl space-y-4">
          <div className="flex flex-wrap items-center gap-2">
            <span className="px-2.5 py-1 rounded-lg bg-amber-400 text-slate-950 text-[10px] font-black uppercase tracking-wider flex items-center gap-1 shadow-sm">
              <GraduationCap size={13} />
              ANURAG UNIVERSITY
            </span>
            <span className="px-2.5 py-1 rounded-lg bg-cyan-500/20 text-cyan-300 border border-cyan-400/30 text-[10px] font-extrabold uppercase tracking-wider">
              InStacks Student Hub
            </span>
            <span className="px-2.5 py-1 rounded-lg bg-emerald-500/20 text-emerald-300 border border-emerald-400/30 text-[10px] font-mono font-bold flex items-center gap-1">
              <Database size={11} /> {connectionInfo.isLocal ? 'Local DB Connected' : 'Live DB'}
            </span>
          </div>

          <div>
            <h1 className="text-2xl sm:text-3xl lg:text-4xl font-black tracking-tight font-heading leading-tight text-white">
              Welcome back, {user?.fullName || 'Student'}! 🚀
            </h1>
            <div className="flex flex-wrap items-center gap-2 pt-1 text-xs text-cyan-200">
              <span className="font-mono bg-white/10 px-2 py-0.5 rounded border border-white/15">
                Roll No: {rollNumber}
              </span>
              <span>•</span>
              <span className="font-semibold text-white">
                {user?.departmentName || 'Computer Science & Engineering'}
              </span>
              <span>•</span>
              <span className="bg-cyan-500/20 px-2 py-0.5 rounded text-cyan-300 font-bold">
                Semester {user?.semester || 3}
              </span>
            </div>
            {user?.goals && (
              <p className="mt-2 text-xs text-amber-300 font-medium flex items-center gap-1.5">
                <span>🎯 Semester Target:</span> <span>{user.goals}</span>
              </p>
            )}
          </div>

          <div className="pt-2 flex flex-wrap gap-3">
            <Link
              to="/focus"
              className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-white font-bold text-xs shadow-md transition-all hover:scale-[1.02]"
            >
              <Timer size={15} />
              Launch Focus Lab
            </Link>
            <Link
              to="/ai-assistant"
              className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-white/15 hover:bg-white/20 text-white font-semibold text-xs backdrop-blur-md border border-white/20 transition-colors"
            >
              <Sparkles size={15} className="text-amber-300" />
              Ask AI Doubt Assistant
            </Link>
            <Link
              to="/browse"
              className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-white/10 hover:bg-white/15 text-slate-200 font-semibold text-xs border border-white/15 transition-colors"
            >
              <Compass size={15} />
              Browse University Notes
            </Link>
          </div>
        </div>

        {/* Ambient Portal Glows */}
        <div className="absolute -right-16 -bottom-16 w-80 h-80 rounded-full bg-cyan-500/15 blur-3xl pointer-events-none" />
        <div className="absolute right-1/4 -top-16 w-64 h-64 rounded-full bg-indigo-500/20 blur-3xl pointer-events-none" />
      </div>

      {/* 2. InStacks Performance Metrics Strip */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="p-4 sm:p-5 rounded-2xl bg-white dark:bg-[#0D1322] border border-slate-200/80 dark:border-slate-800 shadow-sm flex items-center gap-4">
          <div className="w-12 h-12 rounded-2xl bg-amber-50 dark:bg-amber-950/40 text-amber-500 flex items-center justify-center font-bold text-xl shadow-inner border border-amber-300/20">
            <Flame size={24} className="animate-pulse" />
          </div>
          <div>
            <p className="text-[11px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider">AU Habit Streak</p>
            <p className="text-xl sm:text-2xl font-black text-slate-900 dark:text-white">
              {focusStats?.streakDays || 0} <span className="text-xs font-semibold text-slate-400">days</span>
            </p>
          </div>
        </div>

        <div className="p-4 sm:p-5 rounded-2xl bg-white dark:bg-[#0D1322] border border-slate-200/80 dark:border-slate-800 shadow-sm flex items-center gap-4">
          <div className="w-12 h-12 rounded-2xl bg-cyan-50 dark:bg-cyan-950/40 text-cyan-600 dark:text-cyan-400 flex items-center justify-center font-bold text-xl shadow-inner border border-cyan-400/20">
            <Timer size={24} />
          </div>
          <div>
            <p className="text-[11px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider">Today's Focus</p>
            <p className="text-xl sm:text-2xl font-black text-slate-900 dark:text-white">
              {focusStats?.todayMinutes || 0} <span className="text-xs font-semibold text-slate-400">mins</span>
            </p>
          </div>
        </div>

        <div className="p-4 sm:p-5 rounded-2xl bg-white dark:bg-[#0D1322] border border-slate-200/80 dark:border-slate-800 shadow-sm flex items-center gap-4">
          <div className="w-12 h-12 rounded-2xl bg-emerald-50 dark:bg-emerald-950/40 text-emerald-600 dark:text-emerald-400 flex items-center justify-center font-bold text-xl shadow-inner border border-emerald-400/20">
            <CheckCircle2 size={24} />
          </div>
          <div>
            <p className="text-[11px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider">Labs & Notes</p>
            <p className="text-xl sm:text-2xl font-black text-slate-900 dark:text-white">
              {focusStats?.completedSessionsCount || 12} <span className="text-xs font-semibold text-slate-400">completed</span>
            </p>
          </div>
        </div>

        <div className="p-4 sm:p-5 rounded-2xl bg-white dark:bg-[#0D1322] border border-slate-200/80 dark:border-slate-800 shadow-sm flex items-center gap-4">
          <div className="w-12 h-12 rounded-2xl bg-indigo-50 dark:bg-indigo-950/40 text-indigo-600 dark:text-indigo-400 flex items-center justify-center font-bold text-xl shadow-inner border border-indigo-400/20">
            <Award size={24} />
          </div>
          <div>
            <p className="text-[11px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider">Placement Readiness</p>
            <p className="text-xl sm:text-2xl font-black text-slate-900 dark:text-white">
              88% <span className="text-xs font-semibold text-emerald-500">Tier-1 Track</span>
            </p>
          </div>
        </div>
      </div>

      {/* 3. InStacks Quick Action Modules */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Terminal size={18} className="text-cyan-500" />
            <h2 className="text-lg font-black text-slate-900 dark:text-white font-heading">
              InStacks Learning & Assessment Modules
            </h2>
          </div>
          <span className="text-xs text-slate-400">Anurag University Digital Stack</span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <Link
            to="/ai-assistant"
            className="p-5 rounded-2xl bg-gradient-to-br from-indigo-900/10 to-cyan-900/10 dark:bg-[#0D1322] border border-indigo-200/60 dark:border-slate-800 hover:border-cyan-500/50 transition-all group flex flex-col justify-between space-y-3"
          >
            <div className="flex items-center justify-between">
              <div className="w-10 h-10 rounded-xl bg-cyan-500/20 text-cyan-400 flex items-center justify-center">
                <Sparkles size={20} />
              </div>
              <span className="text-[10px] font-black uppercase tracking-wider px-2 py-0.5 rounded-full bg-cyan-500/20 text-cyan-300">
                Active 24/7
              </span>
            </div>
            <div>
              <h3 className="font-bold text-sm text-slate-900 dark:text-white group-hover:text-cyan-400 transition-colors">
                AI Syllabus & Doubt Solver
              </h3>
              <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
                Ask doubts on OS, DBMS, DSA, or get step-by-step code explanations aligned with AU curriculum.
              </p>
            </div>
            <div className="text-xs font-bold text-cyan-600 dark:text-cyan-400 flex items-center gap-1 group-hover:translate-x-1 transition-transform">
              <span>Open AI Tutor</span> <ArrowRight size={13} />
            </div>
          </Link>

          <Link
            to="/whiteboard"
            className="p-5 rounded-2xl bg-gradient-to-br from-indigo-900/10 to-cyan-900/10 dark:bg-[#0D1322] border border-indigo-200/60 dark:border-slate-800 hover:border-cyan-500/50 transition-all group flex flex-col justify-between space-y-3"
          >
            <div className="flex items-center justify-between">
              <div className="w-10 h-10 rounded-xl bg-indigo-500/20 text-indigo-400 flex items-center justify-center">
                <Code2 size={20} />
              </div>
              <span className="text-[10px] font-black uppercase tracking-wider px-2 py-0.5 rounded-full bg-indigo-500/20 text-indigo-300">
                Interactive
              </span>
            </div>
            <div>
              <h3 className="font-bold text-sm text-slate-900 dark:text-white group-hover:text-indigo-400 transition-colors">
                Collaborative Lab Whiteboard
              </h3>
              <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
                Diagram system designs, trees, graphs, and work on coding problems collaboratively with peers.
              </p>
            </div>
            <div className="text-xs font-bold text-indigo-600 dark:text-indigo-400 flex items-center gap-1 group-hover:translate-x-1 transition-transform">
              <span>Launch Whiteboard</span> <ArrowRight size={13} />
            </div>
          </Link>

          <Link
            to="/doubts"
            className="p-5 rounded-2xl bg-gradient-to-br from-indigo-900/10 to-cyan-900/10 dark:bg-[#0D1322] border border-indigo-200/60 dark:border-slate-800 hover:border-cyan-500/50 transition-all group flex flex-col justify-between space-y-3"
          >
            <div className="flex items-center justify-between">
              <div className="w-10 h-10 rounded-xl bg-amber-500/20 text-amber-400 flex items-center justify-center">
                <Layers size={20} />
              </div>
              <span className="text-[10px] font-black uppercase tracking-wider px-2 py-0.5 rounded-full bg-amber-500/20 text-amber-300">
                Peer Forum
              </span>
            </div>
            <div>
              <h3 className="font-bold text-sm text-slate-900 dark:text-white group-hover:text-amber-400 transition-colors">
                Anurag Doubt Clearance Forum
              </h3>
              <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
                Post questions, get verified faculty & peer answers, and upvote the top academic solutions.
              </p>
            </div>
            <div className="text-xs font-bold text-amber-600 dark:text-amber-400 flex items-center gap-1 group-hover:translate-x-1 transition-transform">
              <span>View Discussions</span> <ArrowRight size={13} />
            </div>
          </Link>
        </div>
      </div>

      {/* 4. Subject Tracks & Progress Grid */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-lg font-black text-slate-900 dark:text-white font-heading">
              Current Semester Course Tracks
            </h2>
            <p className="text-xs text-slate-500 dark:text-slate-400">
              Department of {user?.departmentName || 'Computer Science & Engineering'} • Semester {user?.semester || 3}
            </p>
          </div>
          <Link to="/browse" className="text-xs font-bold text-cyan-600 dark:text-cyan-400 hover:underline">
            All Subjects →
          </Link>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {subjects.map((subj, idx) => (
            <Link
              key={idx}
              to={`/browse?q=${encodeURIComponent(subj.name)}`}
              className="p-5 rounded-2xl bg-white dark:bg-[#0D1322] border border-slate-200/80 dark:border-slate-800 hover:border-cyan-500/40 shadow-sm transition-all group"
            >
              <div className="flex items-center justify-between mb-3">
                <span className="text-2xl">{subj.icon}</span>
                <span className="font-mono text-[10px] font-bold px-2 py-0.5 rounded bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-300">
                  {subj.code}
                </span>
              </div>
              <h3 className="font-bold text-sm text-slate-900 dark:text-white group-hover:text-cyan-400 transition-colors">
                {subj.name}
              </h3>
              <div className="mt-3 space-y-1">
                <div className="flex items-center justify-between text-[11px] text-slate-400">
                  <span>Track Mastery</span>
                  <span className="font-bold text-slate-700 dark:text-slate-200">{subj.progress}</span>
                </div>
                <div className="w-full h-1.5 rounded-full bg-slate-100 dark:bg-slate-800 overflow-hidden">
                  <div
                    className={`h-full rounded-full bg-gradient-to-r ${subj.color}`}
                    style={{ width: subj.progress }}
                  />
                </div>
              </div>
            </Link>
          ))}
        </div>
      </div>

      {/* 5. Recommended Resources & Placement Catalog */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-lg font-black text-slate-900 dark:text-white font-heading">
              Placement Readiness & Practice Catalog
            </h2>
            <p className="text-xs text-slate-500 dark:text-slate-400">
              Curated materials from LeetCode, YouTube, PW Skills, and Anurag faculty
            </p>
          </div>
          <Link to="/browse?q=placement" className="text-xs font-bold text-cyan-600 dark:text-cyan-400 hover:underline">
            View Placement Hub →
          </Link>
        </div>

        {loading ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            {[1, 2, 3, 4].map((i) => (
              <ResourceCardSkeleton key={i} />
            ))}
          </div>
        ) : placementResources.length > 0 ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            {placementResources.map((res) => (
              <div
                key={res.id}
                className="p-4 rounded-2xl bg-white dark:bg-[#0D1322] border border-slate-200/80 dark:border-slate-800 shadow-sm flex flex-col justify-between space-y-3 hover:border-cyan-500/40 transition-all group"
              >
                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <PlatformBadge platform={res.sourcePlatform} />
                    <button
                      onClick={() => handleBookmarkToggle(res.id)}
                      className="text-slate-400 hover:text-amber-500 transition-colors"
                      aria-label="Bookmark"
                    >
                      <Bookmark size={15} className={res.bookmarked ? 'fill-amber-400 text-amber-400' : ''} />
                    </button>
                  </div>
                  <Link to={`/resources/${res.id}`} className="block">
                    <h3 className="font-bold text-xs sm:text-sm text-slate-900 dark:text-white group-hover:text-cyan-400 transition-colors line-clamp-2">
                      {res.title}
                    </h3>
                  </Link>
                  <p className="text-[11px] text-slate-500 dark:text-slate-400 line-clamp-2">
                    {res.description}
                  </p>
                </div>

                <div className="pt-2 border-t border-slate-100 dark:border-slate-800/80 flex items-center justify-between text-[11px]">
                  <ReliabilityChip score={res.reliabilityScore} label={res.reliabilityLabel} isVerified={res.verified} />
                  <Link
                    to={`/resources/${res.id}`}
                    className="font-bold text-cyan-600 dark:text-cyan-400 hover:underline flex items-center gap-0.5"
                  >
                    Study <ChevronRight size={12} />
                  </Link>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <EmptyState
            title="No placement resources found"
            description="Check back shortly or explore the full catalog."
            actionText="Explore Catalog"
            onAction={() => { window.location.href = '/browse'; }}
          />
        )}
      </div>
    </div>
  );
};
export default DashboardPage;
