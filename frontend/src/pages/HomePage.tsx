import React from 'react';
import { Link } from 'react-router-dom';
import {
  BookOpen,
  Sparkles,
  ShieldCheck,
  Timer,
  Edit3,
  Users,
  Compass,
  ArrowRight,
  CheckCircle2,
  Star,
  Flame,
  Award,
  ChevronRight
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export const HomePage: React.FC = () => {
  const { isAuthenticated } = useAuth();

  const features = [
    {
      icon: <Compass className="text-indigo-600 dark:text-indigo-400" size={26} />,
      title: 'Multi-Platform Resource Catalog',
      desc: 'Access the best of YouTube courses, LeetCode sheets, PW Skills, CodeWithHarry, NPTEL, and original verified StudyHub cheatsheets in one unified search.',
    },
    {
      icon: <Sparkles className="text-amber-500" size={26} />,
      title: 'AI Revision & Smart Summaries',
      desc: 'Instant Gemini AI-powered executive summaries, exam-focused key formulas, and personalized recommendation algorithms tuned to your semester.',
    },
    {
      icon: <Timer className="text-orange-500" size={26} />,
      title: 'Focus Timer & Study Rooms',
      desc: 'Pomodoro timer with server-verified streaks, ambient white-noise sounds, and real-time collaborative study rooms with campus peers.',
    },
    {
      icon: <ShieldCheck className="text-emerald-500" size={26} />,
      title: 'Bayesian Reliability Rating',
      desc: 'Mathematical Bayesian review scores protect you from low quality materials. Every resource is reviewed and quality-audited by student peers.',
    },
    {
      icon: <Edit3 className="text-violet-500" size={26} />,
      title: 'Collaborative Whiteboard',
      desc: 'Solve complex problems together with real-time vector canvas, live cursor synchronization, and instant diagram sharing.',
    },
    {
      icon: <Users className="text-blue-500" size={26} />,
      title: 'Peer Doubt Forum',
      desc: 'StackOverflow-style academic doubt resolution with code snippets, community upvoting, and verified answers.',
    },
  ];

  return (
    <div className="space-y-20 pb-20 max-w-7xl mx-auto">
      {/* 1. Hero Section */}
      <section className="relative pt-12 pb-8 text-center space-y-6">
        <div className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full bg-indigo-50 dark:bg-indigo-950/60 border border-indigo-200/80 dark:border-indigo-800 text-xs font-bold text-indigo-700 dark:text-indigo-300 shadow-sm animate-bounce">
          <Sparkles size={14} className="text-amber-500" />
          <span>Next-Gen Collaborative EdTech Platform</span>
        </div>

        <h1 className="text-4xl sm:text-6xl font-black text-slate-900 dark:text-white font-heading tracking-tight max-w-4xl mx-auto leading-[1.15]">
          Master Engineering & Ace Placements with{' '}
          <span className="bg-gradient-to-r from-indigo-600 via-violet-600 to-amber-500 bg-clip-text text-transparent">
            StudyHub
          </span>
        </h1>

        <p className="text-base sm:text-lg text-slate-600 dark:text-slate-300 max-w-2xl mx-auto leading-relaxed">
          The ultimate platform for engineering students. Curated courses from YouTube, LeetCode, PW & NPTEL, original verified study notes, AI summaries, and live focus rooms.
        </p>

        <div className="flex flex-wrap items-center justify-center gap-4 pt-4">
          <Link
            to={isAuthenticated ? '/dashboard' : '/register'}
            className="inline-flex items-center gap-2 px-8 py-4 rounded-2xl font-extrabold text-base text-white bg-indigo-600 hover:bg-indigo-700 shadow-xl shadow-indigo-600/30 transition-all hover:scale-105 active:scale-95"
          >
            {isAuthenticated ? 'Go to Student Dashboard' : 'Get Started Free'} <ArrowRight size={18} />
          </Link>

          <Link
            to="/browse"
            className="inline-flex items-center gap-2 px-6 py-4 rounded-2xl font-bold text-base text-slate-700 dark:text-slate-200 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 hover:bg-slate-50 transition-colors"
          >
            <Compass size={18} /> Explore Catalog
          </Link>
        </div>
      </section>

      {/* 2. Stats Strip */}
      <section className="grid grid-cols-2 md:grid-cols-4 gap-4 p-6 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800 shadow-sm text-center">
        <div>
          <p className="text-3xl sm:text-4xl font-black text-indigo-600 dark:text-indigo-400">10,000+</p>
          <p className="text-xs font-semibold text-slate-500 dark:text-slate-400 mt-1 uppercase tracking-wider">Engineering Students</p>
        </div>
        <div>
          <p className="text-3xl sm:text-4xl font-black text-amber-500">500+</p>
          <p className="text-xs font-semibold text-slate-500 dark:text-slate-400 mt-1 uppercase tracking-wider">Curated Resources</p>
        </div>
        <div>
          <p className="text-3xl sm:text-4xl font-black text-emerald-500">4.8★</p>
          <p className="text-xs font-semibold text-slate-500 dark:text-slate-400 mt-1 uppercase tracking-wider">Bayesian Reliability</p>
        </div>
        <div>
          <p className="text-3xl sm:text-4xl font-black text-violet-500">100%</p>
          <p className="text-xs font-semibold text-slate-500 dark:text-slate-400 mt-1 uppercase tracking-wider">Free & Verified</p>
        </div>
      </section>

      {/* 3. Features Grid */}
      <section className="space-y-8">
        <div className="text-center space-y-2 max-w-2xl mx-auto">
          <h2 className="text-2xl sm:text-3xl font-extrabold text-slate-900 dark:text-white font-heading">
            Engineered for Academic Excellence
          </h2>
          <p className="text-sm text-slate-500 dark:text-slate-400">
            Everything you need from your 1st semester up to placement season in one unified workspace.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {features.map((feat, idx) => (
            <div
              key={idx}
              className="p-6 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800 shadow-sm hover:shadow-md transition-all space-y-3"
            >
              <div className="w-12 h-12 rounded-2xl bg-slate-50 dark:bg-slate-800 flex items-center justify-center shadow-inner">
                {feat.icon}
              </div>
              <h3 className="font-bold text-lg text-slate-900 dark:text-white">{feat.title}</h3>
              <p className="text-xs text-slate-600 dark:text-slate-400 leading-relaxed">{feat.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* 4. How it Works */}
      <section className="p-8 sm:p-12 rounded-3xl bg-gradient-to-br from-indigo-900 via-slate-900 to-indigo-950 text-white space-y-8 shadow-xl">
        <div className="text-center space-y-2 max-w-2xl mx-auto">
          <h2 className="text-2xl sm:text-3xl font-extrabold font-heading">How StudyHub Works</h2>
          <p className="text-xs sm:text-sm text-indigo-200">Three simple steps to unlock your academic potential</p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="p-6 rounded-2xl bg-white/5 border border-white/10 backdrop-blur-md space-y-3">
            <span className="w-8 h-8 rounded-full bg-amber-400 text-slate-950 font-black text-sm flex items-center justify-center">
              1
            </span>
            <h3 className="font-bold text-base text-white">Set Your Academic Profile</h3>
            <p className="text-xs text-indigo-200 leading-relaxed">
              Select your department, current semester, and target goals (placements, exams, CP, higher studies).
            </p>
          </div>

          <div className="p-6 rounded-2xl bg-white/5 border border-white/10 backdrop-blur-md space-y-3">
            <span className="w-8 h-8 rounded-full bg-indigo-400 text-slate-950 font-black text-sm flex items-center justify-center">
              2
            </span>
            <h3 className="font-bold text-base text-white">Get Smart Personalized Feeds</h3>
            <p className="text-xs text-indigo-200 leading-relaxed">
              Our rule-based scoring matches you with the highest-reliability courses and original notes.
            </p>
          </div>

          <div className="p-6 rounded-2xl bg-white/5 border border-white/10 backdrop-blur-md space-y-3">
            <span className="w-8 h-8 rounded-full bg-emerald-400 text-slate-950 font-black text-sm flex items-center justify-center">
              3
            </span>
            <h3 className="font-bold text-base text-white">Focus & Collaborate Live</h3>
            <p className="text-xs text-indigo-200 leading-relaxed">
              Run 25m Pomodoros, build streaks, resolve doubts, and practice live on the whiteboard.
            </p>
          </div>
        </div>
      </section>

      {/* 5. Footer */}
      <footer className="pt-12 border-t border-slate-200 dark:border-slate-800 text-center text-xs text-slate-400 space-y-3">
        <div className="flex items-center justify-center gap-2">
          <BookOpen size={16} className="text-indigo-600" />
          <span className="font-bold text-slate-700 dark:text-slate-300">StudyHub Academic Platform</span>
        </div>
        <p>© 2026 StudyHub. Built for collaborative engineering learning. Open educational resources.</p>
      </footer>
    </div>
  );
};
