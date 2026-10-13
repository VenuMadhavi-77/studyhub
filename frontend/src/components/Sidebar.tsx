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
  BookOpen
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
    { label: 'Dashboard', to: '/admin', icon: LayoutDashboard },
    { label: 'Users', to: '/admin?tab=users', icon: Users },
    { label: 'Resources', to: '/admin?tab=resources', icon: FileSpreadsheet },
    { label: 'Reports', to: '/admin?tab=reports', icon: Flag },
    { label: 'Categories', to: '/admin?tab=taxonomy', icon: FolderTree },
    { label: 'Statistics', to: '/admin?tab=stats', icon: BarChart3 },
    { label: 'Announcements', to: '/admin?tab=announcements', icon: Megaphone },
    { label: 'Admin Profile', to: '/profile', icon: User },
  ];

  const studentNavItems = [
    { label: 'Dashboard', to: '/dashboard', icon: LayoutDashboard },
    { label: 'Explore Catalog', to: '/browse', icon: Compass },
    { label: 'AI Assistant', to: '/ai-assistant', icon: Sparkles },
    { label: 'Doubt Forum', to: '/doubts', icon: HelpCircle },
    { label: 'Focus Timer & Rooms', to: '/focus', icon: Timer },
    { label: 'Study Whiteboard', to: '/whiteboard', icon: Edit3 },
    { label: 'Share Resource', to: '/upload', icon: UploadCloud },
    { label: 'My Resources', to: '/my-resources', icon: BookOpen },
    { label: 'My Bookmarks', to: '/bookmarks', icon: Bookmark },
    { label: 'My Profile & Goals', to: '/profile', icon: User },
  ];

  const navItems = isAdmin ? adminNavItems : studentNavItems;

  return (
    <aside className="w-64 flex-shrink-0 hidden md:flex flex-col justify-between h-[calc(100vh-4.25rem)] sticky top-[4.25rem] bg-white/90 dark:bg-slate-900/90 backdrop-blur-md border-r border-slate-200/80 dark:border-slate-800 p-4 z-20">
      <div className="space-y-6">
        {isAdmin && (
          <div className="px-3 py-1.5 rounded-xl bg-slate-900 text-amber-300 text-xs font-bold flex items-center gap-2 border border-amber-400/20">
            <ShieldAlert size={15} />
            <span>Staff Administration</span>
          </div>
        )}

        {/* Navigation links */}
        <nav className="space-y-1.5" aria-label="Sidebar Navigation">
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) =>
                  `flex items-center justify-between px-3.5 py-2.5 rounded-xl font-medium text-sm transition-all group ${
                    isActive
                      ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/25 font-semibold'
                      : 'text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800/60 hover:text-slate-900 dark:hover:text-white'
                  }`
                }
              >
                <div className="flex items-center gap-3">
                  <Icon size={19} className="transition-transform group-hover:scale-110" />
                  <span>{item.label}</span>
                </div>
              </NavLink>
            );
          })}
        </nav>
      </div>

      {/* Mini Study Streak / Focus Card - Students Only */}
      {!isAdmin && (
        <div className="p-3.5 rounded-2xl bg-gradient-to-br from-indigo-50 to-amber-50/60 dark:from-slate-800/90 dark:to-indigo-950/40 border border-indigo-100 dark:border-indigo-900/40">
          <div className="flex items-center justify-between mb-2">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 rounded-xl bg-amber-500 text-white flex items-center justify-center shadow-sm">
                <Flame size={18} className="animate-pulse" />
              </div>
              <div>
                <p className="text-xs font-bold text-slate-900 dark:text-slate-100">Study Streak</p>
                <p className="text-[11px] text-slate-500 dark:text-slate-400">Keep up the daily habit</p>
              </div>
            </div>
          </div>

          <NavLink
            to="/focus"
            className="mt-2.5 flex items-center justify-between w-full py-1.5 px-3 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-semibold shadow-sm transition-transform hover:scale-[1.02]"
          >
            <span>Start 25m Focus</span>
            <ChevronRight size={14} />
          </NavLink>
        </div>
      )}
    </aside>
  );
};
export default Sidebar;
