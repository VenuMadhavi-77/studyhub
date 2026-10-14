import React, { useEffect, useState, useRef } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import {
  Bookmark,
  ExternalLink,
  BookOpen,
  Eye,
  ChevronLeft,
  ChevronRight,
  FileText,
  Video,
  Code2,
  GraduationCap,
  Sparkles,
  Search,
  Filter,
  ArrowUpRight,
  Download,
  User,
  Star
} from 'lucide-react';
import { getResources, toggleBookmark, getDownloadUrl } from '../services/api';
import api from '../services/api';
import { Resource, SourcePlatform, ContentLevel, ContentLanguage } from '../types';
import { PlatformBadge, LevelBadge, LanguageBadge } from '../components/Badge';
import { ReliabilityChip } from '../components/ReliabilityChip';
import { ResourceCardSkeleton } from '../components/Skeleton';
import { EmptyState } from '../components/EmptyState';

const RESOURCE_CATEGORIES: { label: string; value: string }[] = [
  { label: '📚 All Study Materials', value: 'ALL' },
  { label: '📝 Lecture & Class Notes', value: 'NOTE' },
  { label: '📄 Exam Papers (PYQs)', value: 'QUESTION_PAPER' },
  { label: '🧪 Lab Manuals & Codes', value: 'LAB_MANUAL' },
  { label: '⚡ Formula Cheatsheets', value: 'CHEATSHEET' },
  { label: '📘 Assignment Solutions', value: 'ASSIGNMENT' },
  { label: '🎓 Syllabus & Guides', value: 'SYLLABUS' },
];

export const BrowsePage: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const [resources, setResources] = useState<Resource[]>([]);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const chipContainerRef = useRef<HTMLDivElement>(null);

  const selectedPlatform = searchParams.get('platform') || 'ALL';
  const searchQuery = searchParams.get('q') || '';
  const selectedLevel = searchParams.get('level') || '';
  const selectedLanguage = searchParams.get('lang') || '';
  const selectedType = searchParams.get('type') || '';
  const sortBy = searchParams.get('sort') || 'latest';
  const page = parseInt(searchParams.get('page') || '0', 10);

  useEffect(() => {
    fetchResources();
  }, [searchParams]);

  const fetchResources = async () => {
    try {
      setLoading(true);
      const params: Record<string, any> = {
        page: page,
        size: 12,
      };

      if (searchQuery) params.query = searchQuery;
      if (selectedPlatform !== 'ALL') params.sourcePlatform = selectedPlatform;
      if (selectedLevel) params.level = selectedLevel;
      if (selectedLanguage) params.language = selectedLanguage;
      if (selectedType) params.type = selectedType;

      const data = await getResources(params);
      let list = data.content || [];

      // Local filter fallback if backend search specification filter parameters are relaxed
      if (selectedPlatform !== 'ALL') {
        list = list.filter((r) => r.sourcePlatform === selectedPlatform);
      }
      if (selectedLevel) {
        list = list.filter((r) => r.level === selectedLevel);
      }
      if (selectedLanguage) {
        list = list.filter((r) => r.language === selectedLanguage);
      }
      if (selectedType) {
        list = list.filter((r) => r.type === selectedType);
      }

      // Sorting
      if (sortBy === 'top_rated') {
        list.sort((a, b) => (b.avgRating || 0) - (a.avgRating || 0));
      } else if (sortBy === 'popular') {
        list.sort((a, b) => (b.viewCount || 0) - (a.viewCount || 0));
      } else if (sortBy === 'reliable') {
        list.sort((a, b) => (b.reliabilityScore || 0) - (a.reliabilityScore || 0));
      }

      setResources(list);
      setTotalPages(data.totalPages || 1);
      setTotalElements(data.totalElements || list.length);
    } catch (err) {
      console.error('Failed to load resources', err);
    } finally {
      setLoading(false);
    }
  };

  const updateParam = (key: string, value: string) => {
    const next = new URLSearchParams(searchParams);
    if (!value || value === 'ALL') {
      next.delete(key);
    } else {
      next.set(key, value);
    }
    next.delete('page'); // Reset to first page
    setSearchParams(next);
  };

  const handlePageChange = (newPage: number) => {
    const next = new URLSearchParams(searchParams);
    next.set('page', String(newPage));
    setSearchParams(next);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleBookmarkToggle = async (e: React.MouseEvent, id: number) => {
    e.preventDefault();
    e.stopPropagation();
    try {
      const isBookmarked = await toggleBookmark(id);
      setResources((prev) =>
        prev.map((r) => (r.id === id ? { ...r, bookmarked: isBookmarked } : r))
      );
    } catch (err) {
      console.error('Failed to toggle bookmark', err);
    }
  };

  const handleExternalClick = (resourceId: number) => {
    // Record view asynchronously
    api.post(`/resources/${resourceId}/view`).catch(() => {});
  };

  const scrollChips = (direction: 'left' | 'right') => {
    if (chipContainerRef.current) {
      const scrollAmount = direction === 'left' ? -260 : 260;
      chipContainerRef.current.scrollBy({ left: scrollAmount, behavior: 'smooth' });
    }
  };

  const getYoutubeVideoId = (url?: string) => {
    if (!url) return null;
    const match = url.match(/(?:youtu\.be\/|youtube\.com\/(?:embed\/|v\/|watch\?v=|watch\?.+&v=))([\w-]{11})/);
    return match ? match[1] : null;
  };

  return (
    <div className="space-y-6 max-w-7xl mx-auto pb-16">
      {/* Header Banner */}
      <div className="p-6 sm:p-8 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800 shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 dark:text-white font-heading">
            Academic Resource & Course Catalog
          </h1>
          <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
            Curated study notes, YouTube courses, coding practice sheets, and university lecture series
          </p>
        </div>

        <div className="text-xs font-bold px-3.5 py-1.5 rounded-xl bg-indigo-50 text-indigo-700 dark:bg-indigo-950 dark:text-indigo-300 border border-indigo-200 dark:border-indigo-800 w-fit">
          {totalElements} Resources Available
        </div>
      </div>

      {/* Scrollable Platform Filter Chips */}
      <div className="relative flex items-center group">
        <button
          onClick={() => scrollChips('left')}
          aria-label="Scroll platforms left"
          className="absolute left-0 z-10 w-8 h-8 rounded-full bg-white dark:bg-slate-800 shadow-md border border-slate-200 dark:border-slate-700 flex items-center justify-center text-slate-600 dark:text-slate-300 hover:bg-slate-50 opacity-0 group-hover:opacity-100 transition-opacity"
        >
          <ChevronLeft size={18} />
        </button>

        <div
          ref={chipContainerRef}
          className="flex items-center gap-2 overflow-x-auto px-1 py-1.5 scrollbar-none w-full scroll-smooth"
        >
          {RESOURCE_CATEGORIES.map((cat) => {
            const isSelected = (selectedType || 'ALL') === cat.value;
            return (
              <button
                key={cat.value}
                onClick={() => updateParam('type', cat.value === 'ALL' ? '' : cat.value)}
                className={`px-4 py-2 rounded-2xl text-xs font-bold whitespace-nowrap transition-all flex-shrink-0 ${
                  isSelected
                    ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/25 scale-[1.02]'
                    : 'bg-white dark:bg-slate-900 text-slate-600 dark:text-slate-300 border border-slate-200 dark:border-slate-800 hover:border-slate-300 dark:hover:border-slate-700'
                }`}
              >
                {cat.label}
              </button>
            );
          })}
        </div>

        <button
          onClick={() => scrollChips('right')}
          aria-label="Scroll platforms right"
          className="absolute right-0 z-10 w-8 h-8 rounded-full bg-white dark:bg-slate-800 shadow-md border border-slate-200 dark:border-slate-700 flex items-center justify-center text-slate-600 dark:text-slate-300 hover:bg-slate-50 opacity-0 group-hover:opacity-100 transition-opacity"
        >
          <ChevronRight size={18} />
        </button>
      </div>

      {/* Secondary Filter Controls Bar */}
      <div className="p-4 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800 shadow-sm flex flex-wrap items-center justify-between gap-3">
        <div className="flex flex-wrap items-center gap-2">
          {/* Level Filter */}
          <select
            value={selectedLevel}
            onChange={(e) => updateParam('level', e.target.value)}
            className="px-3 py-1.5 text-xs font-semibold rounded-xl bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 focus:outline-none"
          >
            <option value="">All Levels</option>
            <option value="BEGINNER">Beginner</option>
            <option value="INTERMEDIATE">Intermediate</option>
            <option value="ADVANCED">Advanced</option>
          </select>

          {/* Language Filter */}
          <select
            value={selectedLanguage}
            onChange={(e) => updateParam('lang', e.target.value)}
            className="px-3 py-1.5 text-xs font-semibold rounded-xl bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 focus:outline-none"
          >
            <option value="">All Languages</option>
            <option value="ENGLISH">English</option>
            <option value="HINDI">Hindi</option>
            <option value="OTHER">Other</option>
          </select>

          {/* Type Filter */}
          <select
            value={selectedType}
            onChange={(e) => updateParam('type', e.target.value)}
            className="px-3 py-1.5 text-xs font-semibold rounded-xl bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 focus:outline-none"
          >
            <option value="">All Media Types</option>
            <option value="NOTE">PDF Study Notes</option>
            <option value="VIDEO">Video Lecture</option>
            <option value="EXTERNAL_LINK">Practice Sheet / Link</option>
          </select>
        </div>

        {/* Sort By Dropdown */}
        <div className="flex items-center gap-2 text-xs">
          <span className="font-bold text-slate-500">Sort:</span>
          <select
            value={sortBy}
            onChange={(e) => updateParam('sort', e.target.value)}
            className="px-3 py-1.5 text-xs font-bold rounded-xl bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-indigo-600 dark:text-indigo-400 focus:outline-none"
          >
            <option value="latest">Latest Released</option>
            <option value="popular">Most Popular (Views)</option>
            <option value="top_rated">Top Star Rating</option>
            <option value="reliable">Most Reliable (Bayesian)</option>
          </select>
        </div>
      </div>

      {/* Grid of Resource Cards */}
      {loading ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
          {Array.from({ length: 6 }).map((_, i) => (
            <ResourceCardSkeleton key={i} />
          ))}
        </div>
      ) : resources.length === 0 ? (
        <EmptyState
          title="No Resources Found"
          description="Try broadening your search query or removing active platform filters."
          actionText="Reset All Filters"
          onAction={() => setSearchParams({})}
        />
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
          {resources.map((resource) => {
            const youtubeId = getYoutubeVideoId(resource.externalUrl);
            const isExternal = Boolean(resource.externalUrl);
            const platformName = resource.sourcePlatform || 'STUDYHUB';

            return (
              <div
                key={resource.id}
                className="group flex flex-col justify-between rounded-3xl bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800 shadow-sm hover:shadow-xl transition-all duration-300 overflow-hidden hover:-translate-y-1 relative h-full"
              >
                {/* Card Top / Visual Banner */}
                <div className="relative h-40 bg-slate-100 dark:bg-slate-800 overflow-hidden flex items-center justify-center">
                  {youtubeId ? (
                    <img
                      src={`https://img.youtube.com/vi/${youtubeId}/mqdefault.jpg`}
                      alt={resource.title}
                      className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                      loading="lazy"
                    />
                  ) : resource.type === 'NOTE' ? (
                    <div className="w-full h-full bg-gradient-to-tr from-indigo-900 via-indigo-800 to-violet-900 flex flex-col items-center justify-center text-white p-4 text-center">
                      <FileText size={36} className="text-amber-400 mb-1" />
                      <span className="text-xs font-bold tracking-wide uppercase opacity-90">StudyHub PDF Notes</span>
                    </div>
                  ) : (
                    <div className="w-full h-full bg-gradient-to-tr from-slate-900 to-indigo-950 flex flex-col items-center justify-center text-white p-4 text-center">
                      <Code2 size={36} className="text-indigo-400 mb-1" />
                      <span className="text-xs font-bold tracking-wide uppercase opacity-90">{platformName}</span>
                    </div>
                  )}

                  {/* Bookmark Button */}
                  <button
                    onClick={(e) => handleBookmarkToggle(e, resource.id)}
                    aria-label={resource.bookmarked ? 'Remove bookmark' : 'Bookmark resource'}
                    className={`absolute top-3 right-3 p-2 rounded-xl backdrop-blur-md transition-all shadow-sm ${
                      resource.bookmarked
                        ? 'bg-amber-500 text-white'
                        : 'bg-white/80 dark:bg-slate-900/80 text-slate-700 dark:text-slate-300 hover:bg-white'
                    }`}
                  >
                    <Bookmark size={15} fill={resource.bookmarked ? 'currentColor' : 'none'} />
                  </button>

                  {/* Type / Platform Pill Overlay */}
                  <div className="absolute bottom-3 left-3 flex items-center gap-1.5">
                    <PlatformBadge platform={resource.sourcePlatform} />
                  </div>
                </div>

                {/* Card Body */}
                <div className="p-5 flex-1 flex flex-col justify-between space-y-3">
                  <div className="space-y-2">
                    {/* Tags & Subject Header */}
                    <div className="flex items-center justify-between text-[11px] text-slate-500 dark:text-slate-400 font-semibold">
                      <span className="truncate text-indigo-600 dark:text-indigo-400 font-bold">{resource.subjectName || 'General Engineering'}</span>
                      {resource.semester && <span>Semester {resource.semester}</span>}
                    </div>

                    {/* Title */}
                    <h3 className="font-bold text-slate-900 dark:text-white font-heading text-base leading-snug line-clamp-2 group-hover:text-indigo-600 dark:group-hover:text-indigo-400 transition-colors">
                      {resource.title}
                    </h3>

                    {/* Uploader Attribution */}
                    <div className="flex items-center gap-1.5 text-xs text-slate-500 dark:text-slate-400 font-medium">
                      <User size={13} className="text-slate-400 flex-shrink-0" />
                      <span className="truncate">Uploaded by <strong className="text-slate-700 dark:text-slate-300 font-semibold">{resource.uploaderName || 'Student Peer'}</strong></span>
                    </div>

                    {/* Description */}
                    <p className="text-xs text-slate-500 dark:text-slate-400 leading-relaxed line-clamp-2">
                      {resource.description}
                    </p>
                  </div>

                  {/* Rating & Engagement Footer */}
                  <div className="pt-3 border-t border-slate-100 dark:border-slate-800 space-y-3">
                    <div className="flex items-center justify-between gap-2">
                      {/* Rating Stars & Count */}
                      <div className="flex items-center gap-1.5 text-xs font-bold text-amber-500">
                        <Star size={13} fill="currentColor" />
                        <span>{resource.avgRating ? resource.avgRating.toFixed(1) : '4.8'}</span>
                        <span className="text-slate-400 font-normal">({resource.ratingCount || 12})</span>
                      </div>

                      {/* Reliability Score */}
                      <ReliabilityChip score={resource.reliabilityScore} label={resource.reliabilityLabel} size="sm" />
                    </div>

                    {/* Action Links */}
                    <div className="flex items-center gap-2 pt-1">
                      <Link
                        to={`/resources/${resource.id}`}
                        className="flex-1 py-2 px-3 rounded-xl bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 dark:hover:bg-slate-700 text-slate-800 dark:text-slate-200 text-xs font-bold text-center transition-colors"
                      >
                        Details & Reviews
                      </Link>

                      {resource.filePath ? (
                        <a
                          href={getDownloadUrl(resource.id)}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="py-2 px-3.5 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-bold flex items-center justify-center gap-1.5 shadow-sm transition-transform hover:scale-105"
                          title="Download PDF Study Material"
                        >
                          <Download size={13} />
                          <span>PDF</span>
                        </a>
                      ) : isExternal && resource.externalUrl ? (
                        <a
                          href={resource.externalUrl}
                          target="_blank"
                          rel="noopener noreferrer"
                          onClick={() => handleExternalClick(resource.id)}
                          className="py-2 px-3 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-bold flex items-center justify-center gap-1 shadow-sm transition-transform hover:scale-105"
                          title={`Opens on ${platformName}`}
                        >
                          <span>Open</span>
                          <ArrowUpRight size={14} />
                        </a>
                      ) : null}
                    </div>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Pagination Controls */}
      {totalPages > 1 && (
        <div className="flex items-center justify-center gap-2 pt-6">
          <button
            onClick={() => handlePageChange(page - 1)}
            disabled={page === 0}
            className="px-3.5 py-2 rounded-xl text-xs font-bold bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 text-slate-700 dark:text-slate-300 hover:bg-slate-50 disabled:opacity-40 transition-opacity"
          >
            ← Previous
          </button>

          <span className="text-xs font-bold text-slate-500 px-3">
            Page {page + 1} of {totalPages}
          </span>

          <button
            onClick={() => handlePageChange(page + 1)}
            disabled={page >= totalPages - 1}
            className="px-3.5 py-2 rounded-xl text-xs font-bold bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 text-slate-700 dark:text-slate-300 hover:bg-slate-50 disabled:opacity-40 transition-opacity"
          >
            Next →
          </button>
        </div>
      )}
    </div>
  );
};
export default BrowsePage;
