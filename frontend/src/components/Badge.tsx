import React from 'react';
import { SourcePlatform, ContentLevel, ContentLanguage } from '../types';

interface BadgeProps {
  children?: React.ReactNode;
  variant?: 'primary' | 'secondary' | 'accent' | 'success' | 'warning' | 'danger' | 'neutral' | 'outline';
  size?: 'xs' | 'sm' | 'md';
  className?: string;
}

export const Badge: React.FC<BadgeProps> = ({
  children,
  variant = 'primary',
  size = 'sm',
  className = '',
}) => {
  const sizeClasses = {
    xs: 'px-2 py-0.5 text-[10px] font-semibold tracking-wide',
    sm: 'px-2.5 py-1 text-xs font-semibold',
    md: 'px-3 py-1.5 text-sm font-semibold',
  }[size];

  const variantClasses = {
    primary: 'bg-indigo-50 text-indigo-700 border border-indigo-200/70 dark:bg-indigo-950/60 dark:text-indigo-300 dark:border-indigo-800/60',
    secondary: 'bg-violet-50 text-violet-700 border border-violet-200/70 dark:bg-violet-950/60 dark:text-violet-300 dark:border-violet-800/60',
    accent: 'bg-amber-50 text-amber-700 border border-amber-200/70 dark:bg-amber-950/60 dark:text-amber-300 dark:border-amber-800/60',
    success: 'bg-emerald-50 text-emerald-700 border border-emerald-200/70 dark:bg-emerald-950/60 dark:text-emerald-300 dark:border-emerald-800/60',
    warning: 'bg-orange-50 text-orange-700 border border-orange-200/70 dark:bg-orange-950/60 dark:text-orange-300 dark:border-orange-800/60',
    danger: 'bg-rose-50 text-rose-700 border border-rose-200/70 dark:bg-rose-950/60 dark:text-rose-300 dark:border-rose-800/60',
    neutral: 'bg-slate-100 text-slate-700 border border-slate-200 dark:bg-slate-800 dark:text-slate-300 dark:border-slate-700',
    outline: 'border border-slate-300 text-slate-700 dark:border-slate-600 dark:text-slate-300 bg-transparent',
  }[variant];

  return (
    <span className={`inline-flex items-center gap-1 rounded-full ${sizeClasses} ${variantClasses} ${className}`}>
      {children}
    </span>
  );
};

export const PlatformBadge: React.FC<{ platform: SourcePlatform; className?: string }> = ({ platform, className = '' }) => {
  switch (platform) {
    case 'YOUTUBE':
      return (
        <span className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-red-50 text-red-700 border border-red-200 dark:bg-red-950/60 dark:text-red-300 dark:border-red-900/60 ${className}`}>
          <span className="w-1.5 h-1.5 rounded-full bg-red-500 animate-pulse"></span>
          YouTube
        </span>
      );
    case 'PW':
      return (
        <span className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-purple-50 text-purple-700 border border-purple-200 dark:bg-purple-950/60 dark:text-purple-300 dark:border-purple-900/60 ${className}`}>
          PW Skills
        </span>
      );
    case 'CODEWITHHARRY':
      return (
        <span className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200 dark:bg-emerald-950/60 dark:text-emerald-300 dark:border-emerald-900/60 ${className}`}>
          CodeWithHarry
        </span>
      );
    case 'LEETCODE':
      return (
        <span className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-amber-50 text-amber-700 border border-amber-200 dark:bg-amber-950/60 dark:text-amber-300 dark:border-amber-900/60 ${className}`}>
          LeetCode
        </span>
      );
    case 'CODECHEF':
      return (
        <span className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-amber-100 text-amber-900 border border-amber-300 dark:bg-amber-900/40 dark:text-amber-200 ${className}`}>
          CodeChef
        </span>
      );
    case 'GFG':
      return (
        <span className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-green-50 text-green-700 border border-green-200 dark:bg-green-950/60 dark:text-green-300 dark:border-green-900/60 ${className}`}>
          GeeksforGeeks
        </span>
      );
    case 'NPTEL':
      return (
        <span className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-blue-50 text-blue-700 border border-blue-200 dark:bg-blue-950/60 dark:text-blue-300 dark:border-blue-900/60 ${className}`}>
          NPTEL / IIT
        </span>
      );
    case 'MIT_OCW':
      return (
        <span className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-slate-900 text-white border border-slate-700 dark:bg-slate-800 dark:text-slate-100 ${className}`}>
          MIT OCW
        </span>
      );
    case 'STUDYHUB':
    default:
      return (
        <span className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-indigo-50 text-indigo-700 border border-indigo-200 dark:bg-indigo-950/60 dark:text-indigo-300 dark:border-indigo-800/60 ${className}`}>
          ✨ StudyHub Original
        </span>
      );
  }
};

export const LevelBadge: React.FC<{ level: ContentLevel }> = ({ level }) => {
  switch (level) {
    case 'BEGINNER':
      return <Badge variant="success" size="xs">🟢 Beginner</Badge>;
    case 'INTERMEDIATE':
      return <Badge variant="accent" size="xs">🟡 Intermediate</Badge>;
    case 'ADVANCED':
      return <Badge variant="secondary" size="xs">🟣 Advanced</Badge>;
  }
};

export const LanguageBadge: React.FC<{ language: ContentLanguage }> = ({ language }) => {
  return (
    <Badge variant="neutral" size="xs">
      🌐 {language === 'ENGLISH' ? 'English' : language === 'HINDI' ? 'Hindi' : 'Other'}
    </Badge>
  );
};
