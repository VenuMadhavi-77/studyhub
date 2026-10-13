import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  UploadCloud, 
  FileText, 
  Sparkles, 
  CheckCircle2, 
  AlertCircle, 
  RefreshCw,
  X,
  BookOpen,
  ArrowLeft
} from 'lucide-react';
import api from '../services/api';
import { Department, Subject, ResourceType } from '../types';
import { useAuth } from '../context/AuthContext';

export const UploadResourcePage: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [file, setFile] = useState<File | null>(null);
  const [departments, setDepartments] = useState<Department[]>([]);
  const [subjects, setSubjects] = useState<Subject[]>([]);
  const [loadingSubjects, setLoadingSubjects] = useState(false);

  // Form Fields
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [departmentId, setDepartmentId] = useState<number | ''>('');
  const [subjectId, setSubjectId] = useState<number | ''>('');
  const [semester, setSemester] = useState<number>(1);
  const [resourceType, setResourceType] = useState<ResourceType>('NOTE');
  const [tagsInput, setTagsInput] = useState('');

  // Status & AI Extraction
  const [aiExtracting, setAiExtracting] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');
  const [successId, setSuccessId] = useState<number | null>(null);

  useEffect(() => {
    api.get('/taxonomy/departments')
      .then((res) => {
        const depts = res.data.data || [];
        setDepartments(depts);
        if (depts.length > 0 && !departmentId) {
          // Default to user's registered department, or CSE, or first department
          const preferred = depts.find((d: any) => d.id === user?.departmentId)
                         || depts.find((d: any) => d.code === 'CSE')
                         || depts[0];
          setDepartmentId(preferred.id);
        }
      })
      .catch((err) => console.error('Failed to load departments', err));
  }, [user]);

  useEffect(() => {
    if (departmentId) {
      setLoadingSubjects(true);
      api.get(`/taxonomy/departments/${departmentId}/subjects`)
        .then((res) => {
          let subs = res.data.data || [];
          if (subs.length > 0) {
            setSubjects(subs);
            setSubjectId(subs[0].id);
          } else {
            // Fallback: load all available subjects if department has no specific subjects
            api.get('/taxonomy/subjects').then((allRes) => {
              const allSubs = allRes.data.data || [];
              setSubjects(allSubs);
              if (allSubs.length > 0) setSubjectId(allSubs[0].id);
            }).catch(() => {});
          }
        })
        .catch((err) => {
          console.error('Failed to load subjects', err);
          api.get('/taxonomy/subjects').then((allRes) => {
            const allSubs = allRes.data.data || [];
            setSubjects(allSubs);
            if (allSubs.length > 0) setSubjectId(allSubs[0].id);
          }).catch(() => {});
        })
        .finally(() => setLoadingSubjects(false));
    } else {
      setSubjects([]);
      setSubjectId('');
    }
  }, [departmentId]);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      const selected = e.target.files[0];
      if (selected.type !== 'application/pdf') {
        setErrorMsg('Only PDF documents (.pdf) are permitted.');
        return;
      }
      if (selected.size > 50 * 1024 * 1024) {
        setErrorMsg('File size exceeds 50MB limit.');
        return;
      }
      setErrorMsg('');
      setFile(selected);
      if (!title) {
        const cleanName = selected.name.replace(/\.[^/.]+$/, '').replace(/[-_]/g, ' ');
        setTitle(cleanName.charAt(0).toUpperCase() + cleanName.slice(1));
      }
    }
  };

  const handleAiAutoExtract = async () => {
    if (!file) {
      setErrorMsg('Please select a PDF file first.');
      return;
    }

    setAiExtracting(true);
    setErrorMsg('');
    try {
      const formData = new FormData();
      formData.append('file', file);

      const res = await api.post('/ai/categorize-extract', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });

      const extracted = res.data.data;
      if (extracted) {
        if (extracted.suggestedTitle) setTitle(extracted.suggestedTitle);
        if (extracted.extractedSnippet || extracted.summary) setDescription(extracted.extractedSnippet || extracted.summary);
        if (extracted.suggestedSemester) setSemester(extracted.suggestedSemester);
        const resolvedType = extracted.suggestedType || extracted.resourceType;
        if (resolvedType) setResourceType(resolvedType as ResourceType);
        if (extracted.suggestedTags && Array.isArray(extracted.suggestedTags)) {
          setTagsInput(extracted.suggestedTags.join(', '));
        }
      }
    } catch (err: any) {
      console.error('AI Auto-extract failed', err);
      setErrorMsg(err.response?.data?.message || 'AI could not extract metadata from this PDF. Please fill in manually.');
    } finally {
      setAiExtracting(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!file) {
      setErrorMsg('Please choose a PDF file to upload.');
      return;
    }
    if (!departmentId) {
      setErrorMsg('Please select a department.');
      return;
    }
    if (!subjectId) {
      setErrorMsg('Please select a subject.');
      return;
    }

    setSubmitting(true);
    setErrorMsg('');

    try {
      const tagsArray = tagsInput.split(',').map((t) => t.trim()).filter(Boolean);

      const metadata = {
        title: title.trim(),
        description: description.trim() || `Class notes and study reference for ${title.trim()}`,
        type: resourceType,
        departmentId: Number(departmentId),
        subjectId: Number(subjectId),
        semester: Number(semester),
        tags: tagsArray
      };

      const formData = new FormData();
      formData.append('metadata', new Blob([JSON.stringify(metadata)], { type: 'application/json' }));
      formData.append('file', file);

      const res = await api.post('/resources', formData);

      const created = res.data.data;
      setSuccessId(created.id);
    } catch (err: any) {
      console.error('Resource upload failed', err);
      setErrorMsg(err.response?.data?.message || 'Failed to upload resource. Please verify fields.');
    } finally {
      setSubmitting(false);
    }
  };

  if (successId) {
    return (
      <div className="max-w-xl mx-auto my-12 p-8 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800 shadow-xl text-center space-y-5">
        <CheckCircle2 size={54} className="text-emerald-500 mx-auto" />
        <h2 className="text-2xl font-extrabold text-slate-900 dark:text-white font-heading">
          Resource Uploaded Successfully!
        </h2>
        <p className="text-sm text-slate-500 dark:text-slate-400 leading-relaxed">
          Your notes have been saved to the campus repository and are now available for fellow students to read, download, and review.
        </p>
        <div className="flex gap-3 justify-center pt-2">
          <button 
            onClick={() => navigate(`/resources/${successId}`)} 
            className="px-5 py-2.5 rounded-xl font-bold text-sm bg-indigo-600 hover:bg-indigo-700 text-white shadow-md shadow-indigo-600/25 transition-all"
          >
            View Notes Page
          </button>
          <button 
            onClick={() => { setSuccessId(null); setFile(null); setTitle(''); setDescription(''); }} 
            className="px-5 py-2.5 rounded-xl font-bold text-sm bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-300 transition-all"
          >
            Upload Another Note
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto my-8 px-4">
      <div className="p-6 sm:p-8 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200/80 dark:border-slate-800 shadow-lg space-y-6">
        
        {/* Header */}
        <div className="flex items-center justify-between border-b border-slate-100 dark:border-slate-800 pb-4">
          <div>
            <h1 className="text-2xl font-extrabold text-slate-900 dark:text-white font-heading">
              Upload Study Resource
            </h1>
            <p className="text-xs sm:text-sm text-slate-500 dark:text-slate-400 mt-1">
              Share handwritten notes, previous exam question papers (PYQs), lab manuals, or formula cheat sheets.
            </p>
          </div>
          <button 
            onClick={() => navigate('/browse')} 
            className="text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 p-2 rounded-xl hover:bg-slate-100 dark:hover:bg-slate-800"
          >
            <X size={20} />
          </button>
        </div>

        {errorMsg && (
          <div className="p-3.5 rounded-2xl bg-rose-50 dark:bg-rose-950/60 border border-rose-200 dark:border-rose-800 text-rose-700 dark:text-rose-300 text-xs font-semibold flex items-center gap-2">
            <AlertCircle size={16} className="flex-shrink-0" />
            <span>{errorMsg}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-5">
          
          {/* File Dropzone */}
          <div className="space-y-2">
            <label className="text-xs font-bold text-slate-700 dark:text-slate-300 flex items-center gap-1.5">
              <FileText size={14} className="text-indigo-600" /> PDF Document <span className="text-rose-500">*</span>
            </label>
            
            <div className="relative border-2 border-dashed border-slate-300 dark:border-slate-700 hover:border-indigo-500 rounded-2xl p-6 sm:p-8 text-center bg-slate-50/50 dark:bg-slate-800/40 transition-colors">
              <input 
                type="file" 
                accept="application/pdf"
                onChange={handleFileChange}
                className="absolute inset-0 opacity-0 cursor-pointer w-full h-full z-10"
              />
              
              {file ? (
                <div className="flex items-center justify-center gap-3">
                  <div className="w-12 h-12 rounded-xl bg-indigo-50 dark:bg-indigo-950/60 flex items-center justify-center text-indigo-600 dark:text-indigo-400 border border-indigo-200 dark:border-indigo-800">
                    <FileText size={26} />
                  </div>
                  <div className="text-left">
                    <div className="font-bold text-sm text-slate-900 dark:text-white max-w-xs truncate">{file.name}</div>
                    <div className="text-xs text-slate-500 dark:text-slate-400">
                      {(file.size / (1024 * 1024)).toFixed(2)} MB • Ready to upload
                    </div>
                  </div>
                  <button 
                    type="button" 
                    onClick={(e) => { e.stopPropagation(); setFile(null); }}
                    className="relative z-20 ml-2 p-1.5 rounded-lg text-slate-400 hover:text-rose-600 hover:bg-rose-50 dark:hover:bg-rose-950/40"
                  >
                    <X size={18} />
                  </button>
                </div>
              ) : (
                <div className="space-y-2 pointer-events-none">
                  <div className="w-12 h-12 rounded-2xl bg-indigo-50 dark:bg-indigo-950/60 flex items-center justify-center text-indigo-600 dark:text-indigo-400 mx-auto">
                    <UploadCloud size={24} />
                  </div>
                  <p className="font-bold text-sm text-slate-800 dark:text-slate-200">
                    Click to select or drag and drop your PDF here
                  </p>
                  <p className="text-xs text-slate-400">
                    Maximum file size: 50MB (PDF files only)
                  </p>
                </div>
              )}
            </div>

            {/* AI Auto-fill Button */}
            {file && (
              <div className="flex justify-end pt-1">
                <button
                  type="button"
                  onClick={handleAiAutoExtract}
                  disabled={aiExtracting}
                  className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-bold text-indigo-700 bg-indigo-50 hover:bg-indigo-100 dark:bg-indigo-950 dark:text-indigo-300 border border-indigo-200 dark:border-indigo-800 transition-colors"
                >
                  {aiExtracting ? <RefreshCw className="animate-spin" size={13} /> : <Sparkles size={13} />}
                  {aiExtracting ? 'AI Analyzing PDF...' : '✨ Auto-fill with AI'}
                </button>
              </div>
            )}
          </div>

          {/* Title */}
          <div className="space-y-1.5">
            <label className="text-xs font-bold text-slate-700 dark:text-slate-300">
              Resource Title <span className="text-rose-500">*</span>
            </label>
            <input 
              type="text" 
              required 
              placeholder="e.g. Operating Systems Unit 3 — CPU Scheduling & Semaphores Complete Notes" 
              value={title} 
              onChange={(e) => setTitle(e.target.value)}
              className="w-full px-4 py-2.5 rounded-xl text-sm bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-white placeholder-slate-400 focus:border-indigo-500 focus:outline-none"
            />
          </div>

          {/* Department & Subject Dropdowns */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="space-y-1.5">
              <label className="text-xs font-bold text-slate-700 dark:text-slate-300">
                Department <span className="text-rose-500">*</span>
              </label>
              <select 
                required
                value={departmentId} 
                onChange={(e) => setDepartmentId(e.target.value ? Number(e.target.value) : '')}
                className="w-full px-3.5 py-2.5 rounded-xl text-sm bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-white focus:border-indigo-500 focus:outline-none font-medium"
              >
                <option value="">-- Choose Department --</option>
                {departments.map((d) => (
                  <option key={d.id} value={d.id}>
                    {d.name} ({d.code})
                  </option>
                ))}
              </select>
            </div>

            <div className="space-y-1.5">
              <label className="text-xs font-bold text-slate-700 dark:text-slate-300">
                Subject <span className="text-rose-500">*</span>
              </label>
              <select 
                required
                disabled={!departmentId && subjects.length === 0}
                value={subjectId} 
                onChange={(e) => setSubjectId(e.target.value ? Number(e.target.value) : '')}
                className="w-full px-3.5 py-2.5 rounded-xl text-sm bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-white focus:border-indigo-500 focus:outline-none disabled:opacity-50 font-medium"
              >
                <option value="">
                  {loadingSubjects ? 'Loading subjects...' : subjects.length === 0 ? '-- No subjects found --' : '-- Choose Subject --'}
                </option>
                {subjects.map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.name} ({s.code})
                  </option>
                ))}
              </select>
            </div>
          </div>

          {/* Semester & Resource Type Dropdowns */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="space-y-1.5">
              <label className="text-xs font-bold text-slate-700 dark:text-slate-300">
                Semester <span className="text-rose-500">*</span>
              </label>
              <select 
                value={semester} 
                onChange={(e) => setSemester(Number(e.target.value))}
                className="w-full px-3.5 py-2.5 rounded-xl text-sm bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-white focus:border-indigo-500 focus:outline-none font-medium"
              >
                {[1, 2, 3, 4, 5, 6, 7, 8].map((s) => (
                  <option key={s} value={s}>Semester {s}</option>
                ))}
              </select>
            </div>

            <div className="space-y-1.5">
              <label className="text-xs font-bold text-slate-700 dark:text-slate-300">
                Resource Type <span className="text-rose-500">*</span>
              </label>
              <select 
                value={resourceType} 
                onChange={(e) => setResourceType(e.target.value as ResourceType)}
                className="w-full px-3.5 py-2.5 rounded-xl text-sm bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-white focus:border-indigo-500 focus:outline-none font-medium"
              >
                <option value="NOTE">Lecture Notes / Handwritten Notes</option>
                <option value="QUESTION_PAPER">Previous Exam Paper (PYQ)</option>
                <option value="LAB_MANUAL">Lab Manual & Code Experiments</option>
                <option value="ASSIGNMENT">Assignment Solutions</option>
                <option value="CHEATSHEET">Formula Sheet / Quick Revision</option>
                <option value="SYLLABUS">Syllabus & Blueprint</option>
                <option value="OTHER">Other Study Material</option>
              </select>
            </div>
          </div>

          {/* Description */}
          <div className="space-y-1.5">
            <label className="text-xs font-bold text-slate-700 dark:text-slate-300">
              Description & Key Topics Covered
            </label>
            <textarea 
              rows={3} 
              placeholder="e.g. Contains detailed derivations, solved numericals from past 3 years, and faculty lecture slides summary..."
              value={description} 
              onChange={(e) => setDescription(e.target.value)}
              className="w-full px-4 py-2.5 rounded-xl text-sm bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-white placeholder-slate-400 focus:border-indigo-500 focus:outline-none resize-y"
            />
          </div>

          {/* Tags */}
          <div className="space-y-1.5">
            <label className="text-xs font-bold text-slate-700 dark:text-slate-300">
              Tags (comma-separated keywords)
            </label>
            <input 
              type="text" 
              placeholder="e.g. os, cpu-scheduling, peterson-algorithm, midterms, derivations" 
              value={tagsInput} 
              onChange={(e) => setTagsInput(e.target.value)}
              className="w-full px-4 py-2.5 rounded-xl text-sm bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-900 dark:text-white placeholder-slate-400 focus:border-indigo-500 focus:outline-none"
            />
          </div>

          {/* Actions */}
          <div className="flex items-center justify-end gap-3 pt-3 border-t border-slate-100 dark:border-slate-800">
            <button 
              type="button" 
              onClick={() => navigate('/browse')} 
              className="px-5 py-2.5 rounded-xl font-bold text-sm bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-300 transition-colors"
            >
              Cancel
            </button>
            <button 
              type="submit" 
              disabled={submitting} 
              className="inline-flex items-center gap-2 px-6 py-2.5 rounded-xl font-bold text-sm bg-indigo-600 hover:bg-indigo-700 text-white shadow-md shadow-indigo-600/25 disabled:opacity-50 transition-all hover:scale-[1.02] active:scale-95"
            >
              {submitting ? <RefreshCw className="animate-spin" size={16} /> : <UploadCloud size={16} />}
              {submitting ? 'Publishing to Campus...' : 'Publish Resource'}
            </button>
          </div>

        </form>
      </div>
    </div>
  );
};
