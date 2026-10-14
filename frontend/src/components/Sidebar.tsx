import React from 'react';
import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  Compass,
  HelpCircle,
  Timer,
  Edit3,
  UploadCloud,
  User,
  ShieldAlert,
  Flame,
  ChevronRight,
  Users,
  FileSpreadsheet,
  Flag,
  FolderTree,
  BarChart3,
  Megaphone,
  Bookmark,
  Sparkles,
  BookOpen,
  GraduationCap,
  Code2,
  Terminal,
  Cpu
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';

interface SidebarProps {
  collapsed?: boolean;
  onToggle?: () => void;
}

export const Sidebar: React.FC<SidebarProps> = () => {
  const { user } = useAuth();
  const isAdmin = user?.role === 'ADMIN';

  const adminNavItems = [
    { label: 'Admin Console', to: '/admin', icon: LayoutDashboard },
    { label: 'Student Directory', to: '/admin?tab=users', icon: Users },
    { label: 'Curriculum & Notes', to: '/admin?tab=resources', icon: FileSpreadsheet },
    { label: 'Academic Moderation', to: '/admin?tab=reports', icon: Flag },
    { label: 'Departments & Tags', to: '/admin?tab=taxonomy', icon: FolderTree },
    { label: 'Platform Metrics', to: '/admin?tab=stats', icon: BarChart3 },
    { label: 'AU Broadcasts', to: '/admin?tab=announcements', icon: Megaphone },
    { label: 'Staff Profile', to: '/profile', icon: User },
  ];

  const studentSections = [
    {
      title: 'ACADEMIC CORE',
      items: [
        { label: 'Student Dashboard', to: '/dashboard', icon: LayoutDashboard },
        { label: 'Course Catalog & Notes', to: '/browse', icon: Compass },
        { label: 'AI Doubt Assistant', to: '/ai-assistant', icon: Sparkles, badge: 'AI 24/7' },
        { label: 'Doubt Clearance Forum', to: '/doubts', icon: HelpCircle },
      ]
    },
    {
      title: 'PRACTICE & LABS',
      items: [
        { label: 'Focus & Study Rooms', to: '/focus', icon: Timer },
        { label: 'Interactive Whiteboard', to: '/whiteboard', icon: Edit3 },
      ]
    },
    {
      title: 'STUDENT PORTAL',
      items: [
        { label: 'Upload Study Material', to: '/upload', icon: UploadCloud },
        { label: 'My Notes & Uploads', to: '/my-resources', icon: BookOpen },
        { label: 'Saved Bookmarks', to: '/bookmarks', icon: Bookmark },
        { label: 'Profile & Targets', to: '/profile', icon: User },
      ]
    }
  ];

  return (
    <aside className="w-64 flex-shrink-0 hidden md:flex flex-col justify-between h-[calc(100vh-4.5rem)] sticky top-[4.5rem] bg-white/95 dark:bg-[#0A0F1D]/95 backdrop-blur-md border-r border-slate-200/90 dark:border-[#1E293B] p-3.5 z-20 overflow-y-auto">
      <div className="space-y-5">
        {/* StudyHub Portal Pill */}
        <div className="px-3 py-2 rounded-xl bg-gradient-to-r from-[#001F54] to-indigo-950 text-white border border-cyan-500/20 shadow-sm flex items-center justify-between">
          <div className="flex items-center gap-2">
            <GraduationCap size={16} className="text-amber-400" />
            <div className="text-left">
              <p className="text-[11px] font-black tracking-wide text-white uppercase">StudyHub</p>
              <p className="text-[9px] text-cyan-300 font-mono">Academic Portal</p>
            </div>
          </div>
          <span className="text-[9px] px-1.5 py-0.5 rounded bg-cyan-500/20 text-cyan-300 font-bold">PORTAL</span>
        </div>

        {isAdmin ? (
          <div className="space-y-1.5">
            <div className="px-3 py-1 text-[10px] font-black uppercase tracking-wider text-amber-500">
              Staff Administration
            </div>
            <nav className="space-y-1" aria-label="Admin Navigation">
              {adminNavItems.map((item) => {
                const Icon = item.icon;
                return (
                  <NavLink
                    key={item.to}
                    to={item.to}
                    className={({ isActive }) =>
                      `flex items-center justify-between px-3 py-2 rounded-xl text-xs font-semibold transition-all group ${
                        isActive
                          ? 'bg-gradient-to-r from-indigo-600 to-cyan-600 text-white shadow-md shadow-indigo-600/25'
                          : 'text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-[#131B2E] hover:text-slate-900 dark:hover:text-white'
                      }`
                    }
                  >
                    <div className="flex items-center gap-2.5">
                      <Icon size={16} className="transition-transform group-hover:scale-110" />
                      <span>{item.label}</span>
                    </div>
                  </NavLink>
                );
              })}
            </nav>
          </div>
        ) : (
          <div className="space-y-4">
            {studentSections.map((sec, idx) => (
              <div key={idx} className="space-y-1">
                <div className="px-3 text-[10px] font-black uppercase tracking-wider text-slate-400 dark:text-slate-500">
                  {sec.title}
                </div>
                <nav className="space-y-0.5" aria-label={sec.title}>
                  {sec.items.map((item) => {
                    const Icon = item.icon;
                    return (
                      <NavLink
                        key={item.to}
                        to={item.to}
                        className={({ isActive }) =>
                          `flex items-center justify-between px-3 py-2 rounded-xl text-xs font-semibold transition-all group ${
                            isActive
                              ? 'bg-gradient-to-r from-indigo-600 to-cyan-600 text-white shadow-md shadow-indigo-600/20'
                              : 'text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-[#131B2E] hover:text-slate-900 dark:hover:text-white'
                          }`
                        }
                      >
                        <div className="flex items-center gap-2.5">
                          <Icon size={16} className="transition-transform group-hover:scale-110" />
                          <span>{item.label}</span>
                        </div>
                        {item.badge && (
                          <span className="text-[9px] px-1.5 py-0.2 rounded-full bg-cyan-400/10 text-cyan-500 dark:text-cyan-300 border border-cyan-400/30 font-bold">
                            {item.badge}
                          </span>
                        )}
                      </NavLink>
                    );
                  })}
                </nav>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Study Streak Card - Students Only */}
      {!isAdmin && (
        <div className="mt-4 p-3 rounded-2xl bg-gradient-to-br from-indigo-50 to-cyan-50/60 dark:from-[#131B2E] dark:to-[#0A1128] border border-indigo-100 dark:border-cyan-900/40">
          <div className="flex items-center justify-between mb-2">
            <div className="flex items-center gap-2">
              <div className="w-7 h-7 rounded-lg bg-amber-500 text-white flex items-center justify-center shadow-sm">
                <Flame size={15} className="animate-pulse" />
              </div>
              <div>
                <p className="text-xs font-bold text-slate-900 dark:text-slate-100">Daily Study Streak</p>
                <p className="text-[10px] text-slate-500 dark:text-slate-400">Keep daily habit alive</p>
              </div>
            </div>
          </div>

          <NavLink
            to="/focus"
            className="flex items-center justify-between w-full py-1.5 px-2.5 rounded-lg bg-gradient-to-r from-cyan-600 to-indigo-600 hover:from-cyan-500 hover:to-indigo-500 text-white text-[11px] font-bold shadow-sm transition-all"
          >
            <span>Launch 25m Focus Lab</span>
            <ChevronRight size={13} />
          </NavLink>
        </div>
      )}
    </aside>
  );
};
export default Sidebar;
