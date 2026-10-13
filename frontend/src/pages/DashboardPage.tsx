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
  Zap
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { getRecommendations, getResources, getFocusStats, toggleBookmark } from '../services/api';
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
    { name: 'Data Structures & Algorithms', code: 'CS201', color: 'from-blue-500 to-indigo-600', icon: '⚡' },
    { name: 'Database Management', code: 'CS301', color: 'from-emerald-500 to-teal-600', icon: '🗄️' },
    { name: 'Operating Systems', code: 'CS302', color: 'from-amber-500 to-orange-600', icon: '⚙️' },
    { name: 'Computer Networks', code: 'CS303', color: 'from-purple-500 to-violet-600', icon: '🌐' },
    { name: 'Full Stack Web Dev', code: 'IT301', color: 'from-rose-500 to-pink-600', icon: '🚀' },
    { name: 'Placements & Aptitude', code: 'GEN101', color: 'from-indigo-500 to-purple-600', icon: '💼' },
  ];

  return (
    <div className="space-y-8 max-w-7xl mx-auto pb-12">
      {/* 1. Greeting & Hero Banner */}
      <div className="relative overflow-hidden rounded-3xl bg-gradient-to-r from-indigo-900 via-indigo-800 to-violet-900 text-white p-6 sm:p-8 shadow-xl shadow-indigo-950/20">
        <div className="relative z-10 max-w-2xl space-y-3">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-white/10 backdrop-blur-md text-xs font-semibold text-indigo-200 border border-white/15">
            <Sparkles size={14} className="text-amber-400" />
            <span>AI-Powered Academic Hub</span>
          </div>

          <h1 className="text-2xl sm:text-3xl lg:text-4xl font-extrabold tracking-tight font-heading leading-tight">
            Welcome back, {user?.fullName || 'Student'}! 🚀
          </h1>
          <p className="text-sm sm:text-base text-indigo-200 leading-relaxed">
            {user?.departmentName ? `${user.departmentName} • Semester ${user.semester || 1}` : 'Ready to level up your engineering skills today?'}
            {user?.goals && <span className="block mt-1 text-xs text-amber-300 font-medium">🎯 Target Goals: {user.goals}</span>}
          </p>

          <div className="pt-2 flex flex-wrap gap-3">
            <Link
              to="/focus"
              className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-amber-500 hover:bg-amber-400 text-slate-950 font-bold text-sm shadow-md transition-transform hover:scale-105 active:scale-95"
            >
              <Timer size={16} />
              Start Focus Session
            </Link>
            <Link
              to="/browse"
              className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-white/15 hover:bg-white/20 text-white font-semibold text-sm backdrop-blur-md border border-white/20 transition-colors"
            >
              <Compass size={16} />
              Explore Resource Catalog
            </Link>
          </div>
        </div>

        {/* Decorative background glow */}
        <div className="absolute -right-16 -bottom-16 w-80 h-80 rounded-full bg-indigo-500/20 blur-3xl pointer-events-none" />
        <div className="absolute right-1/4 -top-16 w-64 h-64 rounded-full bg-violet-500/20 blur-3xl pointer-events-none" />
      </div>

      {/* 2. Quick Study Stats Strip */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="p-4 sm:p-5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800 shadow-sm flex items-center gap-4">
          <div className="w-12 h-12 rounded-2xl bg-amber-50 dark:bg-amber-950/60 text-amber-500 flex items-center justify-center font-bold text-xl shadow-inner">
            <Flame size={24} className="animate-bounce" />
          </div>
          <div>
            <p className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider">Streak</p>
            <p className="text-xl sm:text-2xl font-extrabold text-slate-900 dark:text-white">
              {focusStats?.streakDays || 0} <span className="text-xs font-normal text-slate-400">days</span>
            </p>
          </div>
        </div>

        <div className="p-4 sm:p-5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800 shadow-sm flex items-center gap-4">
          <div className="w-12 h-12 rounded-2xl bg-indigo-50 dark:bg-indigo-950/60 text-indigo-600 dark:text-indigo-400 flex items-center justify-center font-bold text-xl shadow-inner">
            <Timer size={24} />
          </div>
          <div>
            <p className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider">Today's Focus</p>
            <p className="text-xl sm:text-2xl font-extrabold text-slate-900 dark:text-white">
              {focusStats?.todayMinutes || 0} <span className="text-xs font-normal text-slate-400">mins</span>
            </p>
          </div>
        </div>

        <div className="p-4 sm:p-5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800 shadow-sm flex items-center gap-4">
          <div className="w-12 h-12 rounded-2xl bg-violet-50 dark:bg-violet-950/60 text-violet-600 dark:text-violet-400 flex items-center justify-center font-bold text-xl shadow-inner">
            <Clock size={24} />
          </div>
          <div>
            <p className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider">Weekly Study</p>
            <p className="text-xl sm:text-2xl font-extrabold text-slate-900 dark:text-white">
              {focusStats?.thisWeekMinutes || 0} <span className="text-xs font-normal text-slate-400">mins</span>
            </p>
          </div>
        </div>

        <div className="p-4 sm:p-5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800 shadow-sm flex items-center gap-4">
          <div className="w-12 h-12 rounded-2xl bg-emerald-50 dark:bg-emerald-950/60 text-emerald-600 dark:text-emerald-400 flex items-center justify-center font-bold text-xl shadow-inner">
            <Award size={24} />
          </div>
          <div>
            <p className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider">Completed Sessions</p>
            <p className="text-xl sm:text-2xl font-extrabold text-slate-900 dark:text-white">
              {focusStats?.completedSessionsCount || 0}
            </p>
          </div>
        </div>
      </div>

      {/* 3. Subject Quick Navigation */}
      <section className="space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-lg sm:text-xl font-bold text-slate-900 dark:text-white font-heading flex items-center gap-2">
            <Layers size={20} className="text-indigo-600" />
            Core Academic Subjects
          </h2>
          <Link to="/browse" className="text-xs font-bold text-indigo-600 hover:text-indigo-700 flex items-center gap-1">
            View Catalog <ChevronRight size={14} />
          </Link>
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3">
          {subjects.map((sub) => (
            <Link
              key={sub.code}
              to={`/browse?q=${encodeURIComponent(sub.name)}`}
              className="p-3.5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800 hover:border-indigo-500 dark:hover:border-indigo-500 shadow-sm hover:shadow-md transition-all group text-left"
            >
              <div className="text-2xl mb-2 group-hover:scale-110 transition-transform">{sub.icon}</div>
              <p className="text-xs font-bold text-slate-900 dark:text-white line-clamp-1 group-hover:text-indigo-600">
                {sub.name}
              </p>
              <p className="text-[10px] text-slate-400 uppercase font-semibold mt-0.5">{sub.code}</p>
            </Link>
          ))}
        </div>
      </section>

      {/* 4. Personalized Recommendations Row */}
      <section className="space-y-4">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-lg sm:text-xl font-bold text-slate-900 dark:text-white font-heading flex items-center gap-2">
              <Sparkles size={20} className="text-amber-500" />
              Recommended for You
            </h2>
            <p className="text-xs text-slate-500 dark:text-slate-400">
              Personalized based on your goals, semester, and study activity
            </p>
          </div>
          <Link to="/browse" className="text-xs font-bold text-indigo-600 hover:text-indigo-700 flex items-center gap-1">
            See More <ArrowRight size={14} />
          </Link>
        </div>

        {loading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
            <ResourceCardSkeleton />
            <ResourceCardSkeleton />
            <ResourceCardSkeleton />
          </div>
        ) : recommendations.length > 0 ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
            {recommendations.slice(0, 6).map(({ resource, reason }) => (
              <div
                key={resource.id}
                className="flex flex-col justify-between p-5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800 shadow-sm card-hover-effect relative overflow-hidden"
              >
                {/* Algorithmic Reason Banner */}
                <div className="mb-3 px-2.5 py-1 rounded-lg bg-indigo-50/80 dark:bg-indigo-950/60 border border-indigo-100 dark:border-indigo-900/60 text-[11px] font-semibold text-indigo-700 dark:text-indigo-300 flex items-center gap-1.5">
                  <Zap size={12} className="text-amber-500" />
                  <span className="line-clamp-1">{reason}</span>
                </div>

                <div className="space-y-2.5">
                  <div className="flex items-center justify-between gap-2">
                    <PlatformBadge platform={resource.sourcePlatform} />
                    <ReliabilityChip score={resource.reliabilityScore} label={resource.reliabilityLabel} size="xs" />
                  </div>

                  <Link to={`/resources/${resource.id}`} className="block group">
                    <h3 className="font-bold text-base text-slate-900 dark:text-white group-hover:text-indigo-600 transition-colors line-clamp-2 leading-snug">
                      {resource.title}
                    </h3>
                  </Link>

                  <p className="text-xs text-slate-500 dark:text-slate-400 line-clamp-2 leading-relaxed">
                    {resource.description || 'Verified comprehensive study resource for college learners.'}
                  </p>

                  <div className="flex flex-wrap gap-1.5 pt-1">
                    <LevelBadge level={resource.level} />
                    {resource.bestFor && (
                      <span className="text-[10px] font-medium px-2 py-0.5 rounded-full bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-300 line-clamp-1 max-w-[180px]">
                        🎯 {resource.bestFor}
                      </span>
                    )}
                  </div>
                </div>

                <div className="pt-4 mt-4 border-t border-slate-100 dark:border-slate-800 flex items-center justify-between">
                  <StarRating rating={resource.avgRating} reviewsCount={resource.ratingCount} size="sm" />
                  <Link
                    to={`/resources/${resource.id}`}
                    className="inline-flex items-center gap-1 text-xs font-bold text-indigo-600 hover:text-indigo-700"
                  >
                    Study Now <ChevronRight size={14} />
                  </Link>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <EmptyState
            title="Explore recommendations"
            description="Start bookmarking resources or set your target goals in profile to receive custom AI study recommendations."
            actionText="Browse Resource Catalog"
            onAction={() => (window.location.href = '/browse')}
          />
        )}
      </section>

      {/* 5. Best for Placements Row */}
      <section className="space-y-4">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-lg sm:text-xl font-bold text-slate-900 dark:text-white font-heading flex items-center gap-2">
              <Briefcase size={20} className="text-indigo-600" />
              Best for Placements & Technical Interviews
            </h2>
            <p className="text-xs text-slate-500 dark:text-slate-400">
              High-yield curated sheets, LeetCode plans, and interview preparation materials
            </p>
          </div>
          <Link to="/browse?q=placement" className="text-xs font-bold text-indigo-600 hover:text-indigo-700 flex items-center gap-1">
            View All <ChevronRight size={14} />
          </Link>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          {placementResources.map((res) => (
            <div
              key={res.id}
              className="flex flex-col justify-between p-4 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800 shadow-sm hover:shadow-md transition-all"
            >
              <div className="space-y-2">
                <PlatformBadge platform={res.sourcePlatform} />
                <Link to={`/resources/${res.id}`} className="block group">
                  <h3 className="font-bold text-sm text-slate-900 dark:text-white group-hover:text-indigo-600 line-clamp-2">
                    {res.title}
                  </h3>
                </Link>
                <p className="text-xs text-slate-500 dark:text-slate-400 line-clamp-2">
                  {res.bestFor || res.description}
                </p>
              </div>

              <div className="pt-3 mt-3 border-t border-slate-100 dark:border-slate-800 flex items-center justify-between">
                <StarRating rating={res.avgRating} reviewsCount={res.ratingCount} size="sm" />
                <Link to={`/resources/${res.id}`} className="text-xs font-bold text-indigo-600">
                  Open →
                </Link>
              </div>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
};
