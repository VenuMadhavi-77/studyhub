import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  BookOpen, Trash2, Edit3, ExternalLink, Download, RefreshCw, AlertCircle
} from 'lucide-react';
import api, { getDownloadUrl } from '../services/api';
import { Resource } from '../types';
import { useAuth } from '../context/AuthContext';
import { ReliabilityChip } from '../components/ReliabilityChip';
import { ResourceCardSkeleton } from '../components/Skeleton';
import { EmptyState } from '../components/EmptyState';

export const MyResourcesPage: React.FC = () => {
  const { user } = useAuth();
  const [resources, setResources] = useState<Resource[]>([]);
  const [loading, setLoading] = useState(true);
  const [totalPages, setTotalPages] = useState(1);
  const [page, setPage] = useState(0);
  const [deletingId, setDeletingId] = useState<number | null>(null);
  const [error, setError] = useState('');

  useEffect(() => {
    loadMyResources();
  }, [page]);

  const loadMyResources = async () => {
    try {
      setLoading(true);
      setError('');
      const res = await api.get('/resources/me', { params: { page, size: 12 } });
      const data = res.data.data;
      setResources(data.content || []);
      setTotalPages(data.totalPages || 1);
    } catch (err: any) {
      setError('Failed to load your resources');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: number, title: string) => {
    if (!window.confirm(`Delete "${title}"? This cannot be undone.`)) return;
    try {
      setDeletingId(id);
      await api.delete(`/resources/${id}`);
      setResources((prev) => prev.filter((r) => r.id !== id));
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to delete resource');
    } finally {
      setDeletingId(null);
    }
  };

  return (
    <div className="max-w-5xl mx-auto pb-12 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 dark:text-white flex items-center gap-2">
            <BookOpen size={24} className="text-indigo-500" /> My Uploaded Resources
          </h1>
          <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
            Manage resources you've shared with the community
          </p>
        </div>
        <Link to="/upload" className="btn btn-primary flex items-center gap-2 text-sm">
          + Upload New
        </Link>
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
          icon={<BookOpen size={40} className="text-slate-400" />}
          title="No resources yet"
          description="Share your notes, PDFs, or links with your peers."
          actionText="Upload Your First Resource"
          onAction={() => window.location.href = '/upload'}
        />
      ) : (
        <div className="space-y-3">
          {resources.map((r) => (
            <div key={r.id} className="glass-panel p-4 flex items-center gap-4">
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 mb-1 flex-wrap">
                  <span className={`badge ${r.status === 'APPROVED' ? 'badge-emerald' : r.status === 'REJECTED' ? 'badge-red' : 'badge-amber'} text-xs`}>
                    {r.status}
                  </span>
                  <ReliabilityChip score={r.reliabilityScore} label={r.reliabilityLabel} />
                  <span className="text-xs text-slate-500 dark:text-slate-400">{r.type}</span>
                </div>
                <Link
                  to={`/resources/${r.id}`}
                  className="font-semibold text-slate-900 dark:text-white hover:text-indigo-600 dark:hover:text-indigo-400 truncate block"
                >
                  {r.title}
                </Link>
                <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
                  {r.subjectName} · Sem {r.semester} · ⭐ {r.avgRating.toFixed(1)} ({r.ratingCount}) · {r.viewCount} views
                </p>
              </div>
              <div className="flex items-center gap-2 shrink-0">
                {r.externalUrl && (
                  <a href={r.externalUrl} target="_blank" rel="noopener noreferrer"
                    className="btn btn-secondary btn-sm" title="Open external link">
                    <ExternalLink size={14} />
                  </a>
                )}
                {r.filePath && (
                  <a href={getDownloadUrl(r.id)}
                    className="btn btn-secondary btn-sm" title="Download file">
                    <Download size={14} />
                  </a>
                )}
                <button
                  onClick={() => handleDelete(r.id, r.title)}
                  disabled={deletingId === r.id}
                  className="btn btn-secondary btn-sm text-red-500 hover:text-red-600"
                  title="Delete resource"
                >
                  {deletingId === r.id ? <RefreshCw size={14} className="animate-spin" /> : <Trash2 size={14} />}
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {totalPages > 1 && (
        <div className="flex justify-center gap-2 pt-4">
          <button
            className="btn btn-secondary btn-sm" disabled={page === 0}
            onClick={() => setPage((p) => p - 1)}
          >← Prev</button>
          <span className="px-3 py-1 text-sm text-slate-600 dark:text-slate-300">
            Page {page + 1} of {totalPages}
          </span>
          <button
            className="btn btn-secondary btn-sm" disabled={page >= totalPages - 1}
            onClick={() => setPage((p) => p + 1)}
          >Next →</button>
        </div>
      )}
    </div>
  );
};
