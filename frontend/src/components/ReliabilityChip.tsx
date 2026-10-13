import React from 'react';
import { ShieldCheck, Award, AlertCircle, Sparkles } from 'lucide-react';

interface ReliabilityChipProps {
  score?: number;
  label?: string;
  isVerified?: boolean;
  size?: 'xs' | 'sm' | 'md';
  className?: string;
}

export const ReliabilityChip: React.FC<ReliabilityChipProps> = ({
  score = 0,
  label = 'New - not enough reviews',
  isVerified = false,
  size = 'sm',
  className = '',
}) => {
  const sizeClasses = {
    xs: 'px-2 py-0.5 text-[10px]',
    sm: 'px-2.5 py-1 text-xs',
    md: 'px-3 py-1.5 text-sm',
  }[size];

  if (label === 'Highly reliable') {
    return (
      <span
        title={`Bayesian Reliability Score: ${score.toFixed(1)} / 5.0 (High community confidence)`}
        className={`inline-flex items-center gap-1 font-semibold rounded-full bg-emerald-50 text-emerald-700 border border-emerald-300 dark:bg-emerald-950/60 dark:text-emerald-300 dark:border-emerald-800 ${sizeClasses} ${className}`}
      >
        <ShieldCheck size={size === 'xs' ? 12 : 14} className="text-emerald-600 dark:text-emerald-400" />
        Highly Reliable {score > 0 && <span className="opacity-80">({score.toFixed(1)}★)</span>}
      </span>
    );
  }

  if (label === 'Good') {
    return (
      <span
        title={`Bayesian Reliability Score: ${score.toFixed(1)} / 5.0`}
        className={`inline-flex items-center gap-1 font-semibold rounded-full bg-indigo-50 text-indigo-700 border border-indigo-300 dark:bg-indigo-950/60 dark:text-indigo-300 dark:border-indigo-800 ${sizeClasses} ${className}`}
      >
        <Award size={size === 'xs' ? 12 : 14} className="text-indigo-600 dark:text-indigo-400" />
        Good Reliability {score > 0 && <span className="opacity-80">({score.toFixed(1)}★)</span>}
      </span>
    );
  }

  if (label === 'Mixed') {
    return (
      <span
        title="Community reviews show varied experiences"
        className={`inline-flex items-center gap-1 font-semibold rounded-full bg-amber-50 text-amber-800 border border-amber-300 dark:bg-amber-950/60 dark:text-amber-300 dark:border-amber-800 ${sizeClasses} ${className}`}
      >
        <AlertCircle size={size === 'xs' ? 12 : 14} className="text-amber-600 dark:text-amber-400" />
        Mixed Reviews
      </span>
    );
  }

  return (
    <span
      title="Less than 3 reviews submitted so far"
      className={`inline-flex items-center gap-1 font-medium rounded-full bg-slate-100 text-slate-600 border border-slate-200 dark:bg-slate-800 dark:text-slate-400 dark:border-slate-700 ${sizeClasses} ${className}`}
    >
      <Sparkles size={size === 'xs' ? 11 : 13} className="text-slate-400" />
      {isVerified ? 'Verified Material' : 'New Resource'}
    </span>
  );
};
