import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Bookmark, RefreshCw, AlertCircle } from 'lucide-react';
import { toggleBookmark } from '../services/api';
import api from '../services/api';
import { Resource } from '../types';
import { ReliabilityChip } from '../components/ReliabilityChip';
import { PlatformBadge, LevelBadge } from '../components/Badge';
import { ResourceCardSkeleton } from '../components/Skeleton';
import { EmptyState } from '../components/EmptyState';
import { StarRating } from '../components/StarRating';

export const BookmarksPage: React.FC = () => {
  const [resources, setResources] = useState<Resource[]>([]);
  const [loading, setLoading] = useState(true);
  const [removingId, setRemovingId] = useState<number | null>(null);
  const [error, setError] = useState('');

  useEffect(() => {
    loadBookmarks();
  }, []);

  const loadBookmarks = async () => {
    try {
      setLoading(true);
      setError('');
      const res = await api.get('/bookmarks');
      setResources(res.data.data?.content || res.data.data || []);
    } catch (err: any) {
      setError('Failed to load bookmarks');
    } finally {
      setLoading(false);
    }
  };

  const handleRemoveBookmark = async (id: number) => {
    try {
      setRemovingId(id);
      await toggleBookmark(id);
      setResources((prev) => prev.filter((r) => r.id !== id));
    } catch (err) {
      alert('Failed to remove bookmark');
    } finally {
      setRemovingId(null);
    }
  };

  return (
    <div className="max-w-5xl mx-auto pb-12 space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2">
          <Bookmark size={24} className="text-indigo-500" /> My Bookmarks
        </h1>
        <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
          Resources you've saved for later review
        </p>
      </div>

      {error && (
        <div className="flex items-center gap-2 p-3 rounded-xl bg-red-50 dark:bg-red-900/20 text-red-600 dark:text-red-400 text-sm">
          <AlertCircle size={16} /> {error}
        </div>
      )}

      {loading ? (
        <div className="grid gap-4">
          {[...Array(6)].map((_, i) => <ResourceCardSkeleton key={i} />)}
        </div>
      ) : resources.length === 0 ? (
        <EmptyState
          icon={<Bookmark size={40} className="text-slate-400" />}
          title="No bookmarks yet"
          description="Bookmark any resource from the browse page to save it here."
          actionText="Browse Resources"
          onAction={() => window.location.href = '/browse'}
        />
      ) : (
        <div className="grid sm:grid-cols-2 gap-4">
          {resources.map((r) => (
            <div key={r.id} className="glass-panel p-4 flex flex-col gap-3 group hover:border-indigo-400/50 transition-colors">
              <div className="flex items-start justify-between gap-2">
                <div className="flex items-center gap-2 flex-wrap">
                  <PlatformBadge platform={r.sourcePlatform} />
                  <LevelBadge level={r.level} />
                  <ReliabilityChip score={r.reliabilityScore} label={r.reliabilityLabel} />
                </div>
                <button
                  onClick={() => handleRemoveBookmark(r.id)}
                  disabled={removingId === r.id}
                  className="shrink-0 text-indigo-500 hover:text-red-500 transition-colors"
                  title="Remove bookmark"
                >
                  {removingId === r.id
                    ? <RefreshCw size={16} className="animate-spin" />
                    : <Bookmark size={16} fill="currentColor" />}
                </button>
              </div>

              <div>
                <Link
                  to={`/resources/${r.id}`}
                  className="font-semibold text-slate-900 dark:text-white hover:text-indigo-600 dark:hover:text-indigo-400 line-clamp-2 leading-snug"
                >
                  {r.title}
                </Link>
                <p className="text-xs text-slate-500 dark:text-slate-400 mt-1 line-clamp-2">{r.description}</p>
              </div>

              <div className="flex items-center justify-between text-xs text-slate-500 dark:text-slate-400 pt-1 border-t border-slate-100 dark:border-white/5">
                <span>{r.subjectName}</span>
                <StarRating rating={r.avgRating} size="sm" />
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
