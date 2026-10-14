import React from 'react';
import { Link } from 'react-router-dom';
import {
  Sparkles,
  ShieldCheck,
  Timer,
  Edit3,
  Users,
  Compass,
  ArrowRight,
  CheckCircle2,
  GraduationCap,
  Code2,
  Terminal,
  Cpu,
  Database
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { getConnectionInfo } from '../services/api';

export const HomePage: React.FC = () => {
  const { isAuthenticated } = useAuth();
  const connectionInfo = getConnectionInfo();

  const features = [
    {
      icon: <Terminal className="text-cyan-500" size={26} />,
      title: 'InStacks Coding Labs & DSA',
      desc: 'Practice Data Structures, Algorithms, and company-specific coding assessments with real-time test cases and multi-language support.',
    },
    {
      icon: <Sparkles className="text-amber-400" size={26} />,
      title: '24/7 AI Doubt Resolution',
      desc: 'Instant Gemini AI-powered tutor that understands Anurag University syllabus, textbook topics, and provides exam formulas.',
    },
    {
      icon: <Compass className="text-indigo-500" size={26} />,
      title: 'Curated Multi-Platform Library',
      desc: 'Direct access to verified YouTube courses, LeetCode sheets, PW Skills, NPTEL playlists, and faculty-approved semester notes.',
    },
    {
      icon: <ShieldCheck className="text-emerald-500" size={26} />,
      title: 'Bayesian Quality Reliability',
      desc: 'Every note and video is rated by engineering peers with Bayesian algorithms, ensuring you only study high-scoring materials.',
    },
    {
      icon: <Timer className="text-orange-500" size={26} />,
      title: 'Focus Labs & Study Rooms',
      desc: 'Pomodoro timer with verified habit streaks, ambient study audio, and real-time collaborative focus rooms with classmates.',
    },
    {
      icon: <Edit3 className="text-violet-500" size={26} />,
      title: 'Interactive Study Whiteboard',
      desc: 'Collaboratively solve complex diagrams, finite automata, graph algorithms, and architectural models on a synchronized canvas.',
    },
  ];

  return (
    <div className="space-y-20 pb-20 max-w-7xl mx-auto">
      {/* 1. Hero Section */}
      <section className="relative pt-10 pb-8 text-center space-y-6">
        <div className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full bg-slate-900 text-white border border-cyan-500/30 text-xs font-bold shadow-md">
          <GraduationCap size={15} className="text-amber-400" />
          <span className="text-cyan-300">ANURAG UNIVERSITY</span>
          <span className="text-slate-400">•</span>
          <span className="text-slate-200">INSTACKS PORTAL</span>
        </div>

        <h1 className="text-4xl sm:text-6xl font-black text-slate-900 dark:text-white font-heading tracking-tight max-w-4xl mx-auto leading-[1.15]">
          Next-Gen Learning, Assessment & Career Readiness for{' '}
          <span className="bg-gradient-to-r from-cyan-500 via-indigo-500 to-amber-400 bg-clip-text text-transparent">
            Anurag University
          </span>
        </h1>

        <p className="text-base sm:text-lg text-slate-600 dark:text-slate-300 max-w-2xl mx-auto leading-relaxed">
          The official unified digital platform for engineering students. Comprehensive courseware, AI doubt clearance, coding labs, and campus placement acceleration.
        </p>

        <div className="flex flex-wrap items-center justify-center gap-4 pt-3">
          <Link
            to={isAuthenticated ? '/dashboard' : '/register'}
            className="inline-flex items-center gap-2 px-8 py-4 rounded-2xl font-black text-sm sm:text-base text-slate-950 bg-gradient-to-r from-amber-400 via-amber-300 to-amber-400 hover:from-amber-300 hover:to-amber-200 shadow-xl shadow-amber-400/20 transition-all hover:scale-105 active:scale-95"
          >
            {isAuthenticated ? 'Go to Student Dashboard' : 'Create Student Account'} <ArrowRight size={18} />
          </Link>

          <Link
            to="/browse"
            className="inline-flex items-center gap-2 px-6 py-4 rounded-2xl font-bold text-sm sm:text-base text-slate-800 dark:text-slate-200 bg-white dark:bg-[#0D1322] border border-slate-200 dark:border-slate-800 hover:bg-slate-50 dark:hover:bg-[#131B2E] transition-colors"
          >
            <Compass size={18} /> Explore Course Notes
          </Link>
        </div>

        {/* Database indicator */}
        <div className="pt-2 flex items-center justify-center">
          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-300 dark:border-emerald-800 text-[11px] font-mono text-emerald-700 dark:text-emerald-300">
            <Database size={12} />
            <span>Target: {connectionInfo.isLocal ? 'Local Database (localhost:8080)' : 'Production Cloud DB'}</span>
          </span>
        </div>
      </section>

      {/* 2. Institutional Stats Strip */}
      <section className="grid grid-cols-2 md:grid-cols-4 gap-4 p-6 rounded-3xl bg-white dark:bg-[#0D1322] border border-slate-200/80 dark:border-slate-800 shadow-sm text-center">
        <div>
          <p className="text-3xl sm:text-4xl font-black text-cyan-600 dark:text-cyan-400">15,000+</p>
          <p className="text-xs font-semibold text-slate-500 dark:text-slate-400 mt-1 uppercase tracking-wider">AU Students</p>
        </div>
        <div>
          <p className="text-3xl sm:text-4xl font-black text-amber-500">950+</p>
          <p className="text-xs font-semibold text-slate-500 dark:text-slate-400 mt-1 uppercase tracking-wider">Labs & Tests</p>
        </div>
        <div>
          <p className="text-3xl sm:text-4xl font-black text-emerald-500">4.9★</p>
          <p className="text-xs font-semibold text-slate-500 dark:text-slate-400 mt-1 uppercase tracking-wider">Peer Reliability</p>
        </div>
        <div>
          <p className="text-3xl sm:text-4xl font-black text-indigo-500">100%</p>
          <p className="text-xs font-semibold text-slate-500 dark:text-slate-400 mt-1 uppercase tracking-wider">Placement Ready</p>
        </div>
      </section>

      {/* 3. Features Grid */}
      <section className="space-y-8">
        <div className="text-center space-y-2 max-w-2xl mx-auto">
          <h2 className="text-2xl sm:text-3xl font-black text-slate-900 dark:text-white font-heading">
            Built for Academic Excellence & Placements
          </h2>
          <p className="text-sm text-slate-500 dark:text-slate-400">
            Everything an Anurag University student needs from Semester 1 through Campus Placement drives.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {features.map((feat, idx) => (
            <div
              key={idx}
              className="p-6 rounded-3xl bg-white dark:bg-[#0D1322] border border-slate-200/80 dark:border-slate-800 shadow-sm hover:border-cyan-500/40 transition-all space-y-3"
            >
              <div className="w-12 h-12 rounded-2xl bg-slate-50 dark:bg-[#131B2E] flex items-center justify-center shadow-inner">
                {feat.icon}
              </div>
              <h3 className="font-bold text-base text-slate-900 dark:text-white">{feat.title}</h3>
              <p className="text-xs text-slate-600 dark:text-slate-400 leading-relaxed">{feat.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* 4. Student CTA Banner */}
      <section className="p-8 sm:p-12 rounded-3xl bg-gradient-to-br from-[#0A1128] via-[#001F54] to-[#121B3A] text-white space-y-6 shadow-xl border border-cyan-500/20 text-center">
        <div className="max-w-2xl mx-auto space-y-3">
          <h2 className="text-2xl sm:text-3xl font-black font-heading text-white">
            Ready to Begin Your Semester Journey?
          </h2>
          <p className="text-xs sm:text-sm text-slate-300">
            Sign in with your institutional credentials or create a new profile. Your account and study logs will be saved directly into the institution's database.
          </p>
        </div>

        <div className="flex flex-wrap items-center justify-center gap-3 pt-2">
          <Link
            to="/register"
            className="px-6 py-3 rounded-xl font-black text-sm text-slate-950 bg-amber-400 hover:bg-amber-300 shadow-md transition-all hover:scale-105"
          >
            Register Student Profile
          </Link>
          <Link
            to="/login"
            className="px-6 py-3 rounded-xl font-bold text-sm text-cyan-300 bg-white/10 hover:bg-white/15 border border-white/20 transition-colors"
          >
            Sign In to Portal
          </Link>
        </div>
      </section>
    </div>
  );
};
export default HomePage;
