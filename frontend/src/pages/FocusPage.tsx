import React, { useState, useEffect, useRef } from 'react';
import {
  Timer,
  Play,
  Pause,
  RotateCcw,
  CheckCircle2,
  Flame,
  Volume2,
  VolumeX,
  Award,
  Users,
  Sparkles,
  BookOpen,
  ArrowRight,
  TrendingUp
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import {
  startFocusSession,
  endFocusSession,
  getFocusStats,
  getFocusLeaderboard,
  getSubjects
} from '../services/api';
import { FocusStats, FocusLeaderboardEntry, Subject, FocusMode } from '../types';

export const FocusPage: React.FC = () => {
  const { user } = useAuth();
  const [mode, setMode] = useState<FocusMode>('POMODORO');
  const [plannedMinutes, setPlannedMinutes] = useState(25);
  const [timeLeftSeconds, setTimeLeftSeconds] = useState(25 * 60);
  const [isRunning, setIsRunning] = useState(false);
  const [activeSessionId, setActiveSessionId] = useState<number | null>(null);
  const [selectedSubjectId, setSelectedSubjectId] = useState<number | undefined>();
  const [subjects, setSubjects] = useState<Subject[]>([]);

  // Stats & Leaderboard
  const [stats, setStats] = useState<FocusStats | null>(null);
  const [leaderboard, setLeaderboard] = useState<FocusLeaderboardEntry[]>([]);

  // Ambient sound state (Web Audio API synthesized noise)
  const [ambientSound, setAmbientSound] = useState(false);
  const audioCtxRef = useRef<AudioContext | null>(null);
  const noiseNodeRef = useRef<AudioNode | null>(null);

  // Live Study Room
  const [roomCode, setRoomCode] = useState('STUDY-2026');
  const [joinedRoom, setJoinedRoom] = useState(false);
  const [roomParticipants, setRoomParticipants] = useState<any[]>([
    { name: 'Alex Rivera', status: 'FOCUSING', mins: 42, avatar: 'A' },
    { name: 'Priya Sharma', status: 'FOCUSING', mins: 28, avatar: 'P' },
    { name: 'Rahul Verma', status: 'ON_BREAK', mins: 15, avatar: 'R' },
  ]);

  useEffect(() => {
    loadFocusData();
    getSubjects().then(setSubjects).catch(() => []);
  }, []);

  useEffect(() => {
    let interval: any = null;
    if (isRunning && timeLeftSeconds > 0) {
      interval = setInterval(() => {
        setTimeLeftSeconds((prev) => prev - 1);
      }, 1000);
    } else if (timeLeftSeconds === 0 && isRunning) {
      handleCompleteSession();
    }
    return () => clearInterval(interval);
  }, [isRunning, timeLeftSeconds]);

  const loadFocusData = async () => {
    try {
      const [st, lb] = await Promise.all([
        getFocusStats().catch(() => null),
        getFocusLeaderboard().catch(() => []),
      ]);
      setStats(st);
      setLeaderboard(lb);

      if (st?.activeSession) {
        setActiveSessionId(st.activeSession.id);
        setIsRunning(true);
      }
    } catch (err) {
      console.error('Failed to load focus stats', err);
    }
  };

  const handleModeChange = (newMode: FocusMode, minutes: number) => {
    if (isRunning) return;
    setMode(newMode);
    setPlannedMinutes(minutes);
    setTimeLeftSeconds(minutes * 60);
  };

  const handleStartTimer = async () => {
    try {
      if (!activeSessionId) {
        const session = await startFocusSession({
          subjectId: selectedSubjectId,
          mode,
          plannedMinutes,
        });
        setActiveSessionId(session.id);
      }
      setIsRunning(true);
      if (ambientSound) startAmbientAudio();
    } catch (err) {
      console.error('Failed to start focus session', err);
      setIsRunning(true);
    }
  };

  const handlePauseTimer = () => {
    setIsRunning(false);
    stopAmbientAudio();
  };

  const handleResetTimer = () => {
    setIsRunning(false);
    setTimeLeftSeconds(plannedMinutes * 60);
    stopAmbientAudio();
  };

  const handleCompleteSession = async () => {
    setIsRunning(false);
    stopAmbientAudio();
    if (activeSessionId) {
      try {
        await endFocusSession(activeSessionId);
        setActiveSessionId(null);
      } catch (err) {
        console.error('Failed to end focus session', err);
      }
    }
    loadFocusData();
    setTimeLeftSeconds(plannedMinutes * 60);
  };

  // Synthesized Ambient Rain Sound using Web Audio API (100% offline, zero asset dependencies)
  const startAmbientAudio = () => {
    try {
      const AudioCtx = window.AudioContext || (window as any).webkitAudioContext;
      if (!AudioCtx) return;
      const ctx = new AudioCtx();
      audioCtxRef.current = ctx;

      const bufferSize = ctx.sampleRate * 2;
      const buffer = ctx.createBuffer(1, bufferSize, ctx.sampleRate);
      const data = buffer.getChannelData(0);
      let lastOut = 0.0;

      for (let i = 0; i < bufferSize; i++) {
        const white = Math.random() * 2 - 1;
        // Pink / Brown noise filter for relaxing rain simulation
        data[i] = (lastOut + 0.02 * white) / 1.02;
        lastOut = data[i];
        data[i] *= 0.15; // gentle volume
      }

      const noise = ctx.createBufferSource();
      noise.buffer = buffer;
      noise.loop = true;

      const gain = ctx.createGain();
      gain.gain.setValueAtTime(0.08, ctx.currentTime);

      noise.connect(gain);
      gain.connect(ctx.destination);
      noise.start();
      noiseNodeRef.current = noise;
    } catch (e) {
      console.warn('Web Audio error', e);
    }
  };

  const stopAmbientAudio = () => {
    try {
      if (noiseNodeRef.current) {
        (noiseNodeRef.current as any).stop();
        noiseNodeRef.current = null;
      }
      if (audioCtxRef.current) {
        audioCtxRef.current.close();
        audioCtxRef.current = null;
      }
    } catch (e) {}
  };

  const toggleAmbientSound = () => {
    if (ambientSound) {
      setAmbientSound(false);
      stopAmbientAudio();
    } else {
      setAmbientSound(true);
      if (isRunning) startAmbientAudio();
    }
  };

  const minutes = Math.floor(timeLeftSeconds / 60);
  const seconds = timeLeftSeconds % 60;
  const totalSeconds = plannedMinutes * 60;
  const progressPercent = ((totalSeconds - timeLeftSeconds) / totalSeconds) * 100;
  const strokeDashoffset = 440 - (440 * progressPercent) / 100;

  return (
    <div className="space-y-8 max-w-6xl mx-auto pb-16">
      {/* Header Banner */}
      <div className="p-6 sm:p-8 rounded-3xl bg-gradient-to-r from-amber-500 via-orange-500 to-indigo-600 text-white shadow-xl shadow-orange-950/20 flex flex-col md:flex-row items-center justify-between gap-6">
        <div className="space-y-2 text-center md:text-left">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-white/20 backdrop-blur-md text-xs font-bold text-white">
            <Timer size={14} /> Focus Study Hub
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold font-heading">
            Deep Work & Pomodoro Study Room
          </h1>
          <p className="text-xs sm:text-sm text-amber-100 max-w-xl">
            Build unshakeable focus habits. Consecutive days with at least 10 minutes of verified study build your streak!
          </p>
        </div>

        <div className="flex items-center gap-4 bg-white/10 backdrop-blur-md p-4 rounded-2xl border border-white/20">
          <div className="w-12 h-12 rounded-xl bg-amber-400 text-amber-950 flex items-center justify-center font-bold text-2xl shadow-md">
            <Flame size={26} className="animate-pulse" />
          </div>
          <div>
            <p className="text-xs uppercase font-bold tracking-wider text-amber-200">Current Streak</p>
            <p className="text-2xl font-black text-white">{stats?.streakDays || 0} Days 🔥</p>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Left 2 Cols: Timer & Room */}
        <div className="lg:col-span-2 space-y-6">
          {/* Circular Timer Box */}
          <div className="p-8 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800 shadow-sm flex flex-col items-center justify-center relative overflow-hidden">
            {/* Mode Switcher Tabs */}
            <div className="flex items-center gap-1.5 p-1 rounded-2xl bg-slate-100 dark:bg-slate-800 mb-8 text-xs font-bold">
              <button
                type="button"
                onClick={() => handleModeChange('POMODORO', 25)}
                className={`px-4 py-2 rounded-xl transition-all ${
                  mode === 'POMODORO'
                    ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/25'
                    : 'text-slate-600 dark:text-slate-400 hover:text-slate-900'
                }`}
              >
                Pomodoro (25m)
              </button>
              <button
                type="button"
                onClick={() => handleModeChange('TIMER', 50)}
                className={`px-4 py-2 rounded-xl transition-all ${
                  mode === 'TIMER'
                    ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/25'
                    : 'text-slate-600 dark:text-slate-400 hover:text-slate-900'
                }`}
              >
                Deep Session (50m)
              </button>
              <button
                type="button"
                onClick={() => handleModeChange('STOPWATCH', 15)}
                className={`px-4 py-2 rounded-xl transition-all ${
                  mode === 'STOPWATCH'
                    ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/25'
                    : 'text-slate-600 dark:text-slate-400 hover:text-slate-900'
                }`}
              >
                Quick Sprint (15m)
              </button>
            </div>

            {/* Circular Progress Timer */}
            <div className="relative w-64 h-64 flex items-center justify-center my-4">
              <svg className="w-full h-full transform -rotate-90" viewBox="0 0 160 160">
                {/* Background Ring */}
                <circle
                  cx="80"
                  cy="80"
                  r="70"
                  stroke="currentColor"
                  strokeWidth="8"
                  fill="transparent"
                  className="text-slate-100 dark:text-slate-800"
                />
                {/* Active Progress Ring */}
                <circle
                  cx="80"
                  cy="80"
                  r="70"
                  stroke="currentColor"
                  strokeWidth="8"
                  strokeDasharray="440"
                  strokeDashoffset={strokeDashoffset}
                  strokeLinecap="round"
                  fill="transparent"
                  className="text-indigo-600 dark:text-indigo-400 transition-all duration-500 ease-out"
                />
              </svg>

              <div className="absolute flex flex-col items-center justify-center">
                <span className="text-5xl font-black tracking-tight font-heading text-slate-900 dark:text-white">
                  {String(minutes).padStart(2, '0')}:{String(seconds).padStart(2, '0')}
                </span>
                <span className="text-xs font-semibold text-slate-400 mt-1 uppercase tracking-widest">
                  {isRunning ? 'FOCUSING' : 'PAUSED'}
                </span>
              </div>
            </div>

            {/* Subject Selector & Ambient Sound */}
            <div className="flex flex-wrap items-center justify-center gap-3 my-4">
              <select
                value={selectedSubjectId || ''}
                onChange={(e) => setSelectedSubjectId(Number(e.target.value) || undefined)}
                disabled={isRunning}
                className="px-3 py-1.5 rounded-xl bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-200 text-xs font-semibold border-none focus:ring-1 focus:ring-indigo-500"
              >
                <option value="">📚 General Engineering Studies</option>
                {subjects.map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.name} ({s.code})
                  </option>
                ))}
              </select>

              <button
                type="button"
                onClick={toggleAmbientSound}
                className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-semibold transition-colors ${
                  ambientSound
                    ? 'bg-amber-100 dark:bg-amber-950 text-amber-800 dark:text-amber-300 border border-amber-300 dark:border-amber-800'
                    : 'bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400'
                }`}
              >
                {ambientSound ? <Volume2 size={14} className="text-amber-600" /> : <VolumeX size={14} />}
                Ambient Rain {ambientSound ? 'ON' : 'OFF'}
              </button>
            </div>

            {/* Controls */}
            <div className="flex items-center gap-3 mt-4">
              {!isRunning ? (
                <button
                  type="button"
                  onClick={handleStartTimer}
                  className="inline-flex items-center gap-2 px-8 py-3.5 rounded-2xl bg-indigo-600 hover:bg-indigo-700 text-white font-extrabold text-base shadow-lg shadow-indigo-600/30 transition-transform hover:scale-105 active:scale-95"
                >
                  <Play size={18} className="fill-white" /> Start Studying
                </button>
              ) : (
                <button
                  type="button"
                  onClick={handlePauseTimer}
                  className="inline-flex items-center gap-2 px-6 py-3.5 rounded-2xl bg-amber-500 hover:bg-amber-600 text-slate-950 font-extrabold text-base shadow-lg shadow-amber-500/25 transition-transform hover:scale-105 active:scale-95"
                >
                  <Pause size={18} className="fill-slate-950" /> Pause
                </button>
              )}

              <button
                type="button"
                onClick={handleCompleteSession}
                className="inline-flex items-center gap-2 px-5 py-3.5 rounded-2xl bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-sm shadow-md transition-transform hover:scale-105 active:scale-95"
              >
                <CheckCircle2 size={18} /> Finish & Log
              </button>

              <button
                type="button"
                onClick={handleResetTimer}
                aria-label="Reset Timer"
                className="p-3.5 rounded-2xl bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-300 hover:bg-slate-200 transition-colors"
              >
                <RotateCcw size={18} />
              </button>
            </div>
          </div>

          {/* Live Collaborative Study Room */}
          <div className="p-6 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800 shadow-sm space-y-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Users size={20} className="text-indigo-600" />
                <h3 className="text-base font-bold text-slate-900 dark:text-white">Live Virtual Study Room</h3>
              </div>
              <span className="text-xs font-semibold px-2.5 py-0.5 rounded-full bg-emerald-50 text-emerald-700 border border-emerald-200 dark:bg-emerald-950 dark:text-emerald-300">
                🟢 {roomParticipants.length} Peers Studying Now
              </span>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 pt-2">
              {roomParticipants.map((p, idx) => (
                <div
                  key={idx}
                  className="p-3 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200/80 dark:border-slate-700/60 flex items-center gap-3"
                >
                  <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-indigo-600 to-violet-600 text-white font-bold text-sm flex items-center justify-center">
                    {p.avatar}
                  </div>
                  <div>
                    <p className="text-xs font-bold text-slate-900 dark:text-white">{p.name}</p>
                    <p className="text-[10px] text-emerald-600 font-semibold">{p.status} ({p.mins}m)</p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Right 1 Col: Weekly Leaderboard & Stats */}
        <div className="space-y-6">
          {/* Weekly Top Studiers */}
          <div className="p-6 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800 shadow-sm space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-base font-bold text-slate-900 dark:text-white flex items-center gap-2">
                <Award size={18} className="text-amber-500" /> Weekly Champions
              </h3>
              <span className="text-[11px] text-slate-400 font-semibold">Campus Ranking</span>
            </div>

            <div className="space-y-2.5">
              {leaderboard.length > 0 ? (
                leaderboard.slice(0, 5).map((entry) => (
                  <div
                    key={entry.userId}
                    className="flex items-center justify-between p-3 rounded-2xl bg-slate-50 dark:bg-slate-800/50 border border-slate-100 dark:border-slate-800"
                  >
                    <div className="flex items-center gap-3">
                      <span
                        className={`w-6 h-6 rounded-full text-xs font-black flex items-center justify-center ${
                          entry.rank === 1
                            ? 'bg-amber-400 text-amber-950'
                            : entry.rank === 2
                            ? 'bg-slate-300 text-slate-800'
                            : entry.rank === 3
                            ? 'bg-amber-600 text-white'
                            : 'bg-slate-100 dark:bg-slate-800 text-slate-500'
                        }`}
                      >
                        {entry.rank}
                      </span>
                      <div>
                        <p className="text-xs font-bold text-slate-900 dark:text-white">{entry.fullName}</p>
                        <p className="text-[10px] text-slate-400">{entry.departmentName}</p>
                      </div>
                    </div>

                    <div className="text-right">
                      <p className="text-xs font-bold text-indigo-600 dark:text-indigo-400">{entry.totalMinutes}m</p>
                      <span className="text-[10px] text-amber-500 font-semibold">{entry.streakDays}d 🔥</span>
                    </div>
                  </div>
                ))
              ) : (
                <p className="text-xs text-slate-400 italic py-4">Leaderboard opens with your first session!</p>
              )}
            </div>
          </div>

          {/* Subject Breakdown Card */}
          {stats?.subjectBreakdownMinutes && Object.keys(stats.subjectBreakdownMinutes).length > 0 && (
            <div className="p-6 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800 shadow-sm space-y-3">
              <h3 className="text-sm font-bold text-slate-900 dark:text-white flex items-center gap-2">
                <BookOpen size={16} className="text-indigo-600" /> Subject Focus Time
              </h3>
              <div className="space-y-2">
                {Object.entries(stats.subjectBreakdownMinutes).map(([subj, mins]) => (
                  <div key={subj} className="space-y-1">
                    <div className="flex justify-between text-xs font-semibold">
                      <span className="text-slate-700 dark:text-slate-300">{subj}</span>
                      <span className="text-indigo-600">{mins}m</span>
                    </div>
                    <div className="w-full h-2 rounded-full bg-slate-100 dark:bg-slate-800 overflow-hidden">
                      <div
                        className="h-full bg-indigo-600 rounded-full"
                        style={{ width: `${Math.min(100, (mins / (stats.totalMinutes || 1)) * 100)}%` }}
                      />
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
