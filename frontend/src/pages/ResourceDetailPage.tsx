import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import {
  Sparkles,
  ExternalLink,
  Download,
  Bookmark,
  ThumbsUp,
  ShieldCheck,
  CheckCircle2,
  FileText,
  Video,
  ArrowLeft,
  Send,
  MessageSquare,
  AlertCircle,
  Link2,
  HelpCircle
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import {
  getResourceById,
  getResourceReviews,
  submitReview,
  toggleHelpfulVote,
  toggleBookmark,
  toggleUsefulVote,
  getDownloadUrl
} from '../services/api';
import api from '../services/api';
import { Resource, Review } from '../types';
import { PlatformBadge, LevelBadge, LanguageBadge } from '../components/Badge';
import { ReliabilityChip } from '../components/ReliabilityChip';
import { StarRating } from '../components/StarRating';
import { Skeleton } from '../components/Skeleton';

export const ResourceDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const { user, isAuthenticated } = useAuth();
  const [resource, setResource] = useState<Resource | null>(null);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [loading, setLoading] = useState(true);

  // Review Form State
  const [reviewRating, setReviewRating] = useState(5);
  const [reviewText, setReviewText] = useState('');
  const [submittingReview, setSubmittingReview] = useState(false);
  const [reviewError, setReviewError] = useState<string | null>(null);
  const [reviewSuccess, setReviewSuccess] = useState<string | null>(null);

  useEffect(() => {
    if (id) {
      loadData(id);
    }
  }, [id]);

  const loadData = async (resourceId: string) => {
    try {
      setLoading(true);
      const [resData, reviewsData] = await Promise.all([
        getResourceById(resourceId),
        getResourceReviews(Number(resourceId)).catch(() => []),
      ]);
      setResource(resData);
      setReviews(reviewsData);
    } catch (err) {
      console.error('Failed to load resource details', err);
    } finally {
      setLoading(false);
    }
  };

  const [generatingAiSummary, setGeneratingAiSummary] = useState(false);

  const handleGenerateAiSummary = async () => {
    if (!resource) return;
    try {
      setGeneratingAiSummary(true);
      const res = await api.post(`/ai/summarize/${resource.id}`);
      const data = res.data.data;
      if (data) {
        setResource({
          ...resource,
          aiSummary: data.shortSummary || data.summary,
          aiKeyPoints: Array.isArray(data.keyPoints) ? data.keyPoints.join(' • ') : undefined,
        });
      }
    } catch (err: any) {
      console.error('Failed to generate AI summary', err);
    } finally {
      setGeneratingAiSummary(false);
    }
  };

  const handleReviewSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!id || !resource) return;
    if (reviewText.trim().length < 20) {
      setReviewError('Review text must be at least 20 characters long.');
      return;
    }

    try {
      setSubmittingReview(true);
      setReviewError(null);
      await submitReview(Number(id), { rating: reviewRating, text: reviewText.trim() });
      setReviewSuccess('Thank you! Your review has been submitted and factored into the reliability score.');
      setReviewText('');
      loadData(id);
    } catch (err: any) {
      const msg = err?.response?.data?.message || 'Failed to submit review. You may have already reviewed this resource.';
      setReviewError(msg);
    } finally {
      setSubmittingReview(false);
    }
  };

  const handleHelpfulVote = async (reviewId: number) => {
    if (!isAuthenticated) return;
    try {
      await toggleHelpfulVote(reviewId);
      if (id) loadData(id);
    } catch (err) {
      console.error('Failed to toggle helpful vote', err);
    }
  };

  const handleExternalClick = () => {
    if (id) {
      api.post(`/resources/${id}/view`).catch(() => {});
    }
  };

  const getYoutubeEmbedUrl = (url?: string) => {
    if (!url) return null;
    // Check for playlist parameter
    if (url.includes('list=')) {
      try {
        const u = new URL(url);
        const listId = u.searchParams.get('list');
        if (listId) return `https://www.youtube-nocookie.com/embed/videoseries?list=${listId}`;
      } catch (ignored) {}
    }
    const match = url.match(/(?:youtu\.be\/|youtube\.com\/(?:embed\/|v\/|watch\?v=|watch\?.+&v=))([\w-]{11})/);
    if (match && match[1]) {
      return `https://www.youtube-nocookie.com/embed/${match[1]}`;
    }
    return null;
  };

  if (loading) {
    return (
      <div className="space-y-6 max-w-5xl mx-auto py-8">
        <Skeleton className="h-8 w-48" />
        <Skeleton className="h-96 w-full rounded-3xl" />
        <Skeleton className="h-48 w-full rounded-2xl" />
      </div>
    );
  }

  if (!resource) {
    return (
      <div className="text-center py-16">
        <h2 className="text-xl font-bold text-slate-800 dark:text-slate-100">Resource not found</h2>
        <Link to="/browse" className="text-indigo-600 font-semibold mt-2 inline-block">
          ← Back to Catalog
        </Link>
      </div>
    );
  }

  const youtubeEmbedUrl = getYoutubeEmbedUrl(resource.externalUrl);
  const platformName = resource.sourcePlatform || 'StudyHub';

  return (
    <div className="space-y-8 max-w-5xl mx-auto pb-16">
      {/* Back Link */}
      <Link to="/browse" className="inline-flex items-center gap-1.5 text-xs font-bold text-slate-500 hover:text-indigo-600 transition-colors">
        <ArrowLeft size={16} /> Back to Catalog
      </Link>

      {/* Main Header & Title Card */}
      <div className="p-6 sm:p-8 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800 shadow-sm space-y-4">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div className="flex flex-wrap items-center gap-2">
            <PlatformBadge platform={resource.sourcePlatform} />
            <LevelBadge level={resource.level} />
            <LanguageBadge language={resource.language} />
            {resource.verified && (
              <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200 dark:bg-emerald-950 dark:text-emerald-300">
                <CheckCircle2 size={13} /> Verified by {resource.verifiedBy || 'StudyHub'}
              </span>
            )}
          </div>
          <ReliabilityChip score={resource.reliabilityScore} label={resource.reliabilityLabel} size="md" />
        </div>

        <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 dark:text-white font-heading leading-tight">
          {resource.title}
        </h1>

        <p className="text-sm text-slate-600 dark:text-slate-300 leading-relaxed max-w-3xl">
          {resource.description}
        </p>

        {resource.bestFor && (
          <div className="p-3 rounded-xl bg-amber-50 dark:bg-amber-950/40 border border-amber-200 dark:border-amber-900/60 text-xs font-semibold text-amber-900 dark:text-amber-200">
            🎯 Best For: {resource.bestFor}
          </div>
        )}

        <div className="flex flex-wrap items-center justify-between gap-4 pt-4 border-t border-slate-100 dark:border-slate-800">
          <StarRating rating={resource.avgRating} reviewsCount={resource.ratingCount} size="md" />

          <div className="flex items-center gap-3">
            {resource.externalUrl && (
              <a
                href={resource.externalUrl}
                target="_blank"
                rel="noopener noreferrer"
                onClick={handleExternalClick}
                className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl font-bold text-sm text-white bg-indigo-600 hover:bg-indigo-700 shadow-md shadow-indigo-600/25 transition-transform hover:scale-105 active:scale-95"
              >
                <ExternalLink size={16} /> Open on {platformName}
              </a>
            )}

            {resource.filePath && (
              <a
                href={getDownloadUrl(resource.id)}
                className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl font-semibold text-sm text-slate-700 dark:text-slate-200 bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 transition-colors"
              >
                <Download size={16} /> Download PDF Notes
              </a>
            )}
          </div>
        </div>
      </div>

      {/* Embedded Video Player or PDF Viewer */}
      {youtubeEmbedUrl ? (
        <div className="rounded-3xl overflow-hidden bg-black shadow-xl aspect-video w-full border border-slate-800">
          <iframe
            src={youtubeEmbedUrl}
            title={resource.title}
            className="w-full h-full"
            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
            allowFullScreen
          />
        </div>
      ) : resource.filePath ? (
        <div className="p-6 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800 space-y-3">
          <div className="flex items-center justify-between">
            <h3 className="text-base font-bold text-slate-900 dark:text-white flex items-center gap-2">
              <FileText size={18} className="text-indigo-600" /> Embedded PDF Preview
            </h3>
            <span className="text-xs text-slate-400 font-medium">StudyHub Native Document Reader</span>
          </div>
          <iframe
            src={`${getDownloadUrl(resource.id)}#toolbar=0`}
            title="PDF Preview"
            className="w-full h-[540px] rounded-2xl border border-slate-200 dark:border-slate-800 bg-slate-50"
          />
        </div>
      ) : resource.externalUrl ? (
        <div className="p-8 rounded-3xl bg-gradient-to-tr from-indigo-900 to-slate-900 text-white border border-indigo-700/50 shadow-lg flex flex-col items-center justify-center text-center space-y-4">
          <Link2 size={40} className="text-amber-400" />
          <div className="space-y-1 max-w-md">
            <h3 className="text-lg font-bold">External Resource on {platformName}</h3>
            <p className="text-xs text-indigo-200">
              This interactive problem set, coding track, or lecture is hosted externally.
            </p>
          </div>
          <a
            href={resource.externalUrl}
            target="_blank"
            rel="noopener noreferrer"
            onClick={handleExternalClick}
            className="px-6 py-3 rounded-2xl bg-amber-500 hover:bg-amber-600 text-slate-950 font-bold text-sm shadow-md transition-transform hover:scale-105"
          >
            Open Resource on {platformName} →
          </a>
        </div>
      ) : null}

      {/* AI Key Points & Revision Summary */}
      <div className="p-6 rounded-3xl bg-gradient-to-br from-indigo-50/90 to-violet-50/60 dark:from-slate-900 dark:to-indigo-950/40 border border-indigo-200/70 dark:border-indigo-900/60 space-y-3 shadow-sm">
        <div className="flex items-center justify-between flex-wrap gap-2">
          <div className="flex items-center gap-2 text-indigo-700 dark:text-indigo-300 font-bold text-base">
            <Sparkles size={20} className="text-amber-500" />
            AI Executive Summary & Revision Points
          </div>
          {isAuthenticated && (
            <button
              onClick={handleGenerateAiSummary}
              disabled={generatingAiSummary}
              className="text-xs px-3.5 py-1.5 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-semibold flex items-center gap-1.5 transition-all shadow-sm hover:shadow disabled:opacity-50"
            >
              <Sparkles size={13} className={generatingAiSummary ? 'animate-spin' : ''} />
              {generatingAiSummary ? 'Analyzing with Gemini...' : (resource.aiSummary ? 'Regenerate with AI' : 'Generate with Gemini AI')}
            </button>
          )}
        </div>
        {resource.aiSummary ? (
          <>
            <p className="text-sm text-slate-700 dark:text-slate-300 leading-relaxed">{resource.aiSummary}</p>
            {resource.aiKeyPoints && (
              <div className="pt-2 border-t border-indigo-100 dark:border-indigo-900/40">
                <p className="text-xs font-semibold text-indigo-900 dark:text-indigo-200 mb-1">Key Revision Concepts:</p>
                <p className="text-xs text-slate-600 dark:text-slate-400">{resource.aiKeyPoints}</p>
              </div>
            )}
          </>
        ) : (
          <p className="text-xs text-slate-500 dark:text-slate-400 italic">
            No AI summary generated for this document yet. Click the button above to generate an instant executive summary, key concepts, and exam revision notes powered by Google Gemini.
          </p>
        )}
      </div>

      {/* Reviews & Reliability System */}
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-xl font-bold text-slate-900 dark:text-white font-heading flex items-center gap-2">
              <MessageSquare size={20} className="text-indigo-600" />
              Student Reviews & Reliability ({reviews.length})
            </h2>
            <p className="text-xs text-slate-500 dark:text-slate-400">
              Reliability score is computed via Bayesian weighted average of community peer reviews
            </p>
          </div>
        </div>

        {/* Submit Review Box */}
        {isAuthenticated && user && user.id !== resource.uploaderId && (
          <form onSubmit={handleReviewSubmit} className="p-6 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800 shadow-sm space-y-4">
            <h3 className="text-sm font-bold text-slate-900 dark:text-white">Leave a Peer Review</h3>

            {reviewError && (
              <div className="p-3 rounded-xl bg-rose-50 text-rose-700 text-xs font-semibold flex items-center gap-2">
                <AlertCircle size={15} /> <span>{reviewError}</span>
              </div>
            )}

            {reviewSuccess && (
              <div className="p-3 rounded-xl bg-emerald-50 text-emerald-700 text-xs font-semibold flex items-center gap-2">
                <CheckCircle2 size={15} /> <span>{reviewSuccess}</span>
              </div>
            )}

            <div className="flex items-center gap-2">
              <span className="text-xs font-bold text-slate-700 dark:text-slate-300">Rating:</span>
              <div className="flex items-center gap-1">
                {[1, 2, 3, 4, 5].map((star) => (
                  <button
                    key={star}
                    type="button"
                    onClick={() => setReviewRating(star)}
                    className="text-lg text-amber-400 hover:scale-125 transition-transform"
                  >
                    {star <= reviewRating ? '★' : '☆'}
                  </button>
                ))}
              </div>
            </div>

            <textarea
              required
              rows={3}
              value={reviewText}
              onChange={(e) => setReviewText(e.target.value)}
              placeholder="Write a constructive review (minimum 20 characters) explaining why this resource was helpful..."
              className="w-full p-3.5 text-xs rounded-2xl bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-slate-100 focus:outline-none focus:border-indigo-500"
            />

            <button
              type="submit"
              disabled={submittingReview}
              className="px-5 py-2.5 rounded-xl font-bold text-xs text-white bg-indigo-600 hover:bg-indigo-700 shadow-sm disabled:opacity-60 flex items-center gap-1.5"
            >
              <Send size={14} /> {submittingReview ? 'Submitting...' : 'Post Review'}
            </button>
          </form>
        )}

        {/* Reviews List */}
        <div className="space-y-3">
          {reviews.length === 0 ? (
            <div className="p-8 text-center rounded-3xl bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-800">
              <p className="text-xs text-slate-500">No student reviews yet. Be the first to share your evaluation!</p>
            </div>
          ) : (
            reviews.map((rev) => (
              <div
                key={rev.id}
                className="p-5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800 shadow-sm space-y-2"
              >
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <span className="text-xs font-bold text-slate-900 dark:text-slate-100">{rev.userName || 'Student Peer'}</span>
                    {rev.userRole === 'STUDENT' && (
                      <span className="text-[10px] px-2 py-0.5 rounded-full bg-emerald-50 text-emerald-700 font-semibold border border-emerald-200">
                        Verified Student
                      </span>
                    )}
                  </div>
                  <StarRating rating={rev.rating} size="sm" showText={false} />
                </div>

                <p className="text-xs text-slate-600 dark:text-slate-300 leading-relaxed">{rev.text}</p>

                <div className="flex items-center justify-between pt-2 text-[11px] text-slate-400">
                  <span>{new Date(rev.createdAt).toLocaleDateString()}</span>
                  <button
                    onClick={() => handleHelpfulVote(rev.id)}
                    className={`flex items-center gap-1.5 px-2.5 py-1 rounded-lg transition-colors ${
                      rev.votedHelpfulByCurrentUser
                        ? 'bg-indigo-50 text-indigo-700 dark:bg-indigo-950 dark:text-indigo-300'
                        : 'hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-500'
                    }`}
                  >
                    <ThumbsUp size={12} fill={rev.votedHelpfulByCurrentUser ? 'currentColor' : 'none'} />
                    <span>Helpful ({rev.helpfulCount || 0})</span>
                  </button>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
};
export default ResourceDetailPage;
