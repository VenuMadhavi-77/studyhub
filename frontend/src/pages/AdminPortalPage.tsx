import React, { useState, useEffect } from 'react';
import { 
  ShieldCheck, 
  Users, 
  FileText, 
  AlertTriangle, 
  CheckCircle, 
  XCircle, 
  Layers, 
  RefreshCw, 
  Plus, 
  Eye, 
  Lock, 
  Unlock,
  Trash2,
  Folder,
  BookOpen,
  Target,
  ExternalLink,
  Wrench,
  ArrowLeft,
  Check,
  X
} from 'lucide-react';
import api from '../services/api';
import { AdminStats, Resource, User, Department, DepartmentDetail, SourcePlatform } from '../types';
import { useAuth } from '../context/AuthContext';

const PLATFORM_OPTIONS: SourcePlatform[] = [
  'STUDYHUB',
  'YOUTUBE',
  'NPTEL',
  'MIT_OCW',
  'LEETCODE',
  'CODECHEF',
  'GFG',
  'PW',
  'CODEWITHHARRY',
  'OTHER'
];

export const AdminPortalPage: React.FC = () => {
  const { user } = useAuth();
  const [stats, setStats] = useState<AdminStats | null>(null);
  const [activeTab, setActiveTab] = useState<'moderation' | 'reports' | 'users' | 'taxonomy'>('moderation');
  
  // Moderation state
  const [pendingResources, setPendingResources] = useState<Resource[]>([]);
  const [loading, setLoading] = useState(true);
  const [rejectModalOpen, setRejectModalOpen] = useState(false);
  const [selectedResourceId, setSelectedResourceId] = useState<number | null>(null);
  const [rejectionReason, setRejectionReason] = useState('');

  // Reports state
  const [reports, setReports] = useState<any[]>([]);

  // Users state
  const [usersList, setUsersList] = useState<User[]>([]);

  // Taxonomy state
  const [departments, setDepartments] = useState<Department[]>([]);
  const [newDeptName, setNewDeptName] = useState('');
  const [newDeptCode, setNewDeptCode] = useState('');
  const [selectedDeptId, setSelectedDeptId] = useState<number | null>(null);
  const [deptDetail, setDeptDetail] = useState<DepartmentDetail | null>(null);
  const [deptDetailLoading, setDeptDetailLoading] = useState(false);

  // Sub-taxonomy form state
  const [newSubjectName, setNewSubjectName] = useState('');
  const [newSubjectCode, setNewSubjectCode] = useState('');
  const [newSubjectSemester, setNewSubjectSemester] = useState(1);

  const [newGoalKey, setNewGoalKey] = useState('');
  const [newGoalLabel, setNewGoalLabel] = useState('');
  const [newGoalDesc, setNewGoalDesc] = useState('');
  const [newGoalIsShared, setNewGoalIsShared] = useState(false);

  const [newPlatform, setNewPlatform] = useState<SourcePlatform>('YOUTUBE');
  const [newPlatformPriority, setNewPlatformPriority] = useState(10);

  const [newToolName, setNewToolName] = useState('');

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    setLoading(true);
    try {
      const statsRes = await api.get('/admin/stats');
      setStats(statsRes.data.data);

      const pendingRes = await api.get('/admin/resources/pending');
      setPendingResources(pendingRes.data.data.content || []);

      const reportsRes = await api.get('/admin/reports');
      setReports(reportsRes.data.data.content || []);

      const usersRes = await api.get('/admin/users');
      setUsersList(usersRes.data.data.content || []);

      const deptRes = await api.get('/admin/taxonomy/departments');
      setDepartments(deptRes.data.data || []);
    } catch (err) {
      console.error('Failed to load admin dashboard', err);
    } finally {
      setLoading(false);
    }
  };

  const loadDepartmentDetails = async (id: number) => {
    setDeptDetailLoading(true);
    try {
      const res = await api.get(`/taxonomy/departments/${id}/details`);
      setDeptDetail(res.data.data);
      setSelectedDeptId(id);
    } catch (err) {
      alert('Failed to load department details');
    } finally {
      setDeptDetailLoading(false);
    }
  };

  const handleApproveResource = async (id: number) => {
    try {
      await api.patch(`/admin/resources/${id}/moderation`, {
        status: 'APPROVED',
        rejectionReason: null
      });
      loadDashboardData();
    } catch (err) {
      alert('Failed to approve resource');
    }
  };

  const handleRejectResource = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedResourceId || !rejectionReason.trim()) return;
    try {
      await api.patch(`/admin/resources/${selectedResourceId}/moderation`, {
        status: 'REJECTED',
        rejectionReason: rejectionReason.trim()
      });
      setRejectModalOpen(false);
      setRejectionReason('');
      setSelectedResourceId(null);
      loadDashboardData();
    } catch (err) {
      alert('Failed to reject resource');
    }
  };

  const handleResolveReport = async (reportId: number, status: string) => {
    try {
      await api.patch(`/admin/reports/${reportId}/resolve`, {
        status,
        resolutionNotes: 'Resolved by administrator'
      });
      loadDashboardData();
    } catch (err) {
      alert('Failed to resolve report');
    }
  };

  const handleToggleUserStatus = async (targetUser: User) => {
    const newStatus = targetUser.status === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE';
    try {
      await api.patch(`/admin/users/${targetUser.id}/status`, {
        status: newStatus,
        reason: 'Administrator updated account status'
      });
      loadDashboardData();
    } catch (err) {
      alert('Failed to update user status');
    }
  };

  // --- Department CRUD ---
  const handleAddDepartment = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newDeptName.trim() || !newDeptCode.trim()) return;
    try {
      await api.post('/admin/taxonomy/departments', {
        name: newDeptName.trim(),
        code: newDeptCode.trim().toUpperCase()
      });
      setNewDeptName('');
      setNewDeptCode('');
      loadDashboardData();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to add department');
    }
  };

  const handleToggleDepartmentStatus = async (deptId: number, currentActive: boolean) => {
    try {
      await api.patch(`/admin/taxonomy/departments/${deptId}/status`, null, {
        params: { active: !currentActive }
      });
      loadDashboardData();
      if (selectedDeptId === deptId) {
        loadDepartmentDetails(deptId);
      }
    } catch (err) {
      alert('Failed to update department status');
    }
  };

  // --- Subject CRUD ---
  const handleAddSubject = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedDeptId || !newSubjectName.trim() || !newSubjectCode.trim()) return;
    try {
      await api.post('/admin/taxonomy/subjects', {
        departmentId: selectedDeptId,
        name: newSubjectName.trim(),
        code: newSubjectCode.trim().toUpperCase(),
        semester: Number(newSubjectSemester)
      });
      setNewSubjectName('');
      setNewSubjectCode('');
      loadDepartmentDetails(selectedDeptId);
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to add subject');
    }
  };

  const handleToggleSubjectStatus = async (subjId: number, currentActive: boolean) => {
    try {
      await api.patch(`/admin/taxonomy/subjects/${subjId}/status`, null, {
        params: { active: !currentActive }
      });
      if (selectedDeptId) loadDepartmentDetails(selectedDeptId);
    } catch (err) {
      alert('Failed to update subject status');
    }
  };

  // --- Goal CRUD ---
  const handleAddGoal = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newGoalKey.trim() || !newGoalLabel.trim()) return;
    try {
      await api.post('/admin/taxonomy/goals', {
        departmentId: newGoalIsShared ? null : selectedDeptId,
        goalKey: newGoalKey.trim().toUpperCase(),
        label: newGoalLabel.trim(),
        description: newGoalDesc.trim() || null
      });
      setNewGoalKey('');
      setNewGoalLabel('');
      setNewGoalDesc('');
      if (selectedDeptId) loadDepartmentDetails(selectedDeptId);
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to create goal');
    }
  };

  const handleToggleGoalStatus = async (goalId: number, currentActive: boolean) => {
    try {
      await api.patch(`/admin/taxonomy/goals/${goalId}/status`, null, {
        params: { active: !currentActive }
      });
      if (selectedDeptId) loadDepartmentDetails(selectedDeptId);
    } catch (err) {
      alert('Failed to update goal status');
    }
  };

  // --- Platform CRUD ---
  const handleAddPlatform = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedDeptId) return;
    try {
      await api.post('/admin/taxonomy/platforms', {
        departmentId: selectedDeptId,
        platform: newPlatform,
        priority: Number(newPlatformPriority)
      });
      loadDepartmentDetails(selectedDeptId);
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to add platform mapping');
    }
  };

  const handleDeletePlatform = async (platformId: number) => {
    if (!confirm('Remove platform mapping?')) return;
    try {
      await api.delete(`/admin/taxonomy/platforms/${platformId}`);
      if (selectedDeptId) loadDepartmentDetails(selectedDeptId);
    } catch (err) {
      alert('Failed to delete platform');
    }
  };

  // --- Tool CRUD ---
  const handleAddTool = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedDeptId || !newToolName.trim()) return;
    try {
      await api.post('/admin/taxonomy/tools', {
        departmentId: selectedDeptId,
        name: newToolName.trim()
      });
      setNewToolName('');
      loadDepartmentDetails(selectedDeptId);
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to add tool');
    }
  };

  const handleDeleteTool = async (toolId: number) => {
    if (!confirm('Remove tool mapping?')) return;
    try {
      await api.delete(`/admin/taxonomy/tools/${toolId}`);
      if (selectedDeptId) loadDepartmentDetails(selectedDeptId);
    } catch (err) {
      alert('Failed to delete tool');
    }
  };

  if (user?.role !== 'ADMIN') {
    return (
      <div style={{ maxWidth: '600px', margin: '4rem auto', textAlign: 'center' }} className="glass-panel">
        <div style={{ padding: '3rem 2rem' }}>
          <AlertTriangle size={48} color="#fda4af" style={{ margin: '0 auto 1rem' }} />
          <h2>Access Denied</h2>
          <p style={{ color: 'var(--text-secondary)', marginTop: '0.5rem' }}>
            This portal is restricted to authorized platform administrators.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: '1280px', margin: '0 auto', padding: '2rem 1.5rem' }}>
      
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '2rem', marginBottom: '0.4rem', display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
            <ShieldCheck size={28} color="#ec4899" /> Administrator Portal
          </h1>
          <p style={{ color: 'var(--text-secondary)' }}>
            System-wide content moderation, user account governance, and multi-department taxonomy management.
          </p>
        </div>
        <button onClick={loadDashboardData} className="btn btn-secondary btn-sm">
          <RefreshCw size={14} /> Refresh Data
        </button>
      </div>

      {/* KPI Stats Cards */}
      {stats && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1.25rem', marginBottom: '2rem' }}>
          <div className="glass-panel" style={{ padding: '1.25rem' }}>
            <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Total Users</span>
            <div style={{ fontSize: '1.8rem', fontWeight: 800, color: '#ffffff', marginTop: '0.25rem' }}>{stats.totalUsers}</div>
          </div>
          <div className="glass-panel" style={{ padding: '1.25rem' }}>
            <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Approved Resources</span>
            <div style={{ fontSize: '1.8rem', fontWeight: 800, color: '#34d399', marginTop: '0.25rem' }}>{stats.totalResources}</div>
          </div>
          <div className="glass-panel" style={{ padding: '1.25rem', border: stats.pendingResources > 0 ? '1px solid #f59e0b' : '1px solid var(--border-subtle)' }}>
            <span style={{ fontSize: '0.8rem', color: '#f59e0b' }}>Pending Moderation</span>
            <div style={{ fontSize: '1.8rem', fontWeight: 800, color: '#fbbf24', marginTop: '0.25rem' }}>{stats.pendingResources}</div>
          </div>
          <div className="glass-panel" style={{ padding: '1.25rem' }}>
            <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Total Doubts</span>
            <div style={{ fontSize: '1.8rem', fontWeight: 800, color: '#818cf8', marginTop: '0.25rem' }}>{stats.totalDoubts}</div>
          </div>
          <div className="glass-panel" style={{ padding: '1.25rem' }}>
            <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Total Downloads</span>
            <div style={{ fontSize: '1.8rem', fontWeight: 800, color: '#38bdf8', marginTop: '0.25rem' }}>{stats.totalDownloads}</div>
          </div>
        </div>
      )}

      {/* Tabs Navigation */}
      <div className="glass-panel" style={{ padding: '0.5rem', display: 'flex', gap: '0.5rem', marginBottom: '1.5rem', flexWrap: 'wrap' }}>
        <button 
          onClick={() => { setActiveTab('moderation'); setSelectedDeptId(null); }} 
          className={`btn btn-sm ${activeTab === 'moderation' ? 'btn-primary' : 'btn-secondary'}`}
        >
          Pending Resources ({pendingResources.length})
        </button>
        <button 
          onClick={() => { setActiveTab('reports'); setSelectedDeptId(null); }} 
          className={`btn btn-sm ${activeTab === 'reports' ? 'btn-primary' : 'btn-secondary'}`}
        >
          Abuse Reports ({reports.length})
        </button>
        <button 
          onClick={() => { setActiveTab('users'); setSelectedDeptId(null); }} 
          className={`btn btn-sm ${activeTab === 'users' ? 'btn-primary' : 'btn-secondary'}`}
        >
          User Accounts
        </button>
        <button 
          onClick={() => setActiveTab('taxonomy')} 
          className={`btn btn-sm ${activeTab === 'taxonomy' ? 'btn-primary' : 'btn-secondary'}`}
        >
          Departments & Taxonomy
        </button>
      </div>

      {/* Tab 1: Moderation Queue */}
      {activeTab === 'moderation' && (
        <div className="glass-panel" style={{ padding: '1.5rem' }}>
          <h3 style={{ fontSize: '1.2rem', marginBottom: '1rem' }}>Pending Content Review Queue</h3>
          {pendingResources.length === 0 ? (
            <p style={{ color: 'var(--text-muted)', textAlign: 'center', padding: '2rem 0' }}>All pending uploads have been reviewed!</p>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              {pendingResources.map((res) => (
                <div key={res.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '1rem', background: 'rgba(255,255,255,0.03)', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-subtle)', flexWrap: 'wrap', gap: '1rem' }}>
                  <div>
                    <h4 style={{ fontSize: '1.05rem', color: '#ffffff', marginBottom: '0.25rem' }}>{res.title}</h4>
                    <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                      {res.departmentName || 'General'} • {res.subjectName} • Uploaded by {res.uploaderName} on {new Date(res.createdAt).toLocaleDateString()}
                    </p>
                  </div>

                  <div style={{ display: 'flex', gap: '0.5rem' }}>
                    <a href={`/resources/${res.id}`} target="_blank" rel="noreferrer" className="btn btn-secondary btn-sm">
                      <Eye size={14} /> Preview
                    </a>
                    <button onClick={() => handleApproveResource(res.id)} className="btn btn-primary btn-sm" style={{ background: '#10b981' }}>
                      <CheckCircle size={14} /> Approve
                    </button>
                    <button 
                      onClick={() => { setSelectedResourceId(res.id); setRejectModalOpen(true); }} 
                      className="btn btn-danger btn-sm"
                    >
                      <XCircle size={14} /> Reject
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Tab 2: Reports Queue */}
      {activeTab === 'reports' && (
        <div className="glass-panel" style={{ padding: '1.5rem' }}>
          <h3 style={{ fontSize: '1.2rem', marginBottom: '1rem' }}>Community Moderation Reports</h3>
          {reports.length === 0 ? (
            <p style={{ color: 'var(--text-muted)', textAlign: 'center', padding: '2rem 0' }}>No open abuse reports!</p>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              {reports.map((rep) => (
                <div key={rep.id} style={{ padding: '1rem', background: 'rgba(255,255,255,0.03)', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-subtle)' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.5rem' }}>
                    <div>
                      <span className="badge badge-amber">{rep.targetType}</span>
                      <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginLeft: '0.75rem' }}>
                        Reason: <strong>{rep.reason}</strong>
                      </span>
                    </div>
                    <span className={`badge ${rep.status === 'PENDING' ? 'badge-rose' : 'badge-emerald'}`}>{rep.status}</span>
                  </div>
                  <p style={{ fontSize: '0.9rem', color: 'var(--text-primary)', marginBottom: '0.75rem' }}>
                    {rep.details || 'No additional details provided by reporter.'}
                  </p>
                  {rep.status === 'PENDING' && (
                    <div style={{ display: 'flex', gap: '0.5rem' }}>
                      <button onClick={() => handleResolveReport(rep.id, 'RESOLVED')} className="btn btn-primary btn-sm">
                        Take Action & Resolve
                      </button>
                      <button onClick={() => handleResolveReport(rep.id, 'DISMISSED')} className="btn btn-secondary btn-sm">
                        Dismiss
                      </button>
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Tab 3: Users Accounts */}
      {activeTab === 'users' && (
        <div className="glass-panel" style={{ padding: '1.5rem' }}>
          <h3 style={{ fontSize: '1.2rem', marginBottom: '1.5rem' }}>User Governance & Account Controls</h3>
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.88rem' }}>
              <thead>
                <tr style={{ borderBottom: '1px solid var(--border-subtle)', color: 'var(--text-muted)' }}>
                  <th style={{ padding: '0.75rem' }}>Name</th>
                  <th style={{ padding: '0.75rem' }}>Email</th>
                  <th style={{ padding: '0.75rem' }}>Role</th>
                  <th style={{ padding: '0.75rem' }}>Status</th>
                  <th style={{ padding: '0.75rem', textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {usersList.map((u) => (
                  <tr key={u.id} style={{ borderBottom: '1px solid rgba(255,255,255,0.04)' }}>
                    <td style={{ padding: '0.75rem', fontWeight: 600 }}>{u.fullName}</td>
                    <td style={{ padding: '0.75rem', color: 'var(--text-secondary)' }}>{u.email}</td>
                    <td style={{ padding: '0.75rem' }}><span className="badge badge-indigo">{u.role}</span></td>
                    <td style={{ padding: '0.75rem' }}>
                      <span className={`badge ${u.status === 'ACTIVE' ? 'badge-emerald' : 'badge-rose'}`}>
                        {u.status}
                      </span>
                    </td>
                    <td style={{ padding: '0.75rem', textAlign: 'right' }}>
                      <button 
                        onClick={() => handleToggleUserStatus(u)} 
                        className={`btn btn-sm ${u.status === 'ACTIVE' ? 'btn-danger' : 'btn-secondary'}`}
                        style={{ fontSize: '0.75rem' }}
                      >
                        {u.status === 'ACTIVE' ? <><Lock size={12} /> Suspend</> : <><Unlock size={12} /> Unsuspend</>}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Tab 4: Taxonomy Management */}
      {activeTab === 'taxonomy' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          
          {/* Department List & Add Form */}
          {!selectedDeptId && (
            <div className="glass-panel" style={{ padding: '1.5rem' }}>
              <h3 style={{ fontSize: '1.2rem', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <Folder size={20} color="#ec4899" /> Academic Departments
              </h3>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.88rem', marginBottom: '1.5rem' }}>
                Create, edit, and configure department curriculum, subjects, goals, platforms, and tools.
              </p>
              
              <form onSubmit={handleAddDepartment} style={{ display: 'flex', gap: '0.75rem', marginBottom: '2rem', flexWrap: 'wrap' }}>
                <input 
                  type="text" 
                  required 
                  placeholder="Department Name (e.g. Chemical Engineering)" 
                  value={newDeptName}
                  onChange={(e) => setNewDeptName(e.target.value)}
                  className="input-field"
                  style={{ flex: 1, minWidth: '240px' }}
                />
                <input 
                  type="text" 
                  required 
                  placeholder="Code (e.g. CHE)" 
                  value={newDeptCode}
                  onChange={(e) => setNewDeptCode(e.target.value)}
                  className="input-field"
                  style={{ width: '120px' }}
                />
                <button type="submit" className="btn btn-primary">
                  <Plus size={16} /> Add Department
                </button>
              </form>

              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '1rem' }}>
                {departments.map((d) => (
                  <div key={d.id} className="glass-panel" style={{ padding: '1.25rem', border: '1px solid var(--border-subtle)', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
                    <div>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                        <span className="badge badge-indigo">{d.code}</span>
                        <span className={`badge ${d.active ? 'badge-emerald' : 'badge-rose'}`}>
                          {d.active ? 'Active' : 'Inactive'}
                        </span>
                      </div>
                      <h4 style={{ fontSize: '1.1rem', color: '#ffffff', marginBottom: '0.75rem' }}>{d.name}</h4>
                    </div>

                    <div style={{ display: 'flex', gap: '0.5rem', marginTop: '1rem' }}>
                      <button 
                        onClick={() => loadDepartmentDetails(d.id)} 
                        className="btn btn-primary btn-sm"
                        style={{ flex: 1 }}
                      >
                        Manage Curriculum & Tools
                      </button>
                      <button 
                        onClick={() => handleToggleDepartmentStatus(d.id, !!d.active)}
                        className={`btn btn-sm ${d.active ? 'btn-secondary' : 'btn-primary'}`}
                        title={d.active ? 'Deactivate department' : 'Activate department'}
                      >
                        {d.active ? <X size={14} /> : <Check size={14} />}
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Department Detailed Taxonomy View */}
          {selectedDeptId && deptDetail && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
              
              {/* Back Bar & Header */}
              <div className="glass-panel" style={{ padding: '1.25rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                  <button onClick={() => { setSelectedDeptId(null); setDeptDetail(null); }} className="btn btn-secondary btn-sm">
                    <ArrowLeft size={16} /> Back to Departments
                  </button>
                  <div>
                    <h2 style={{ fontSize: '1.4rem', color: '#ffffff', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                      {deptDetail.name} <span className="badge badge-indigo">{deptDetail.code}</span>
                    </h2>
                    <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                      Status: {deptDetail.active ? 'Active' : 'Deactivated'}
                    </span>
                  </div>
                </div>

                <button 
                  onClick={() => handleToggleDepartmentStatus(deptDetail.id, deptDetail.active)}
                  className={`btn btn-sm ${deptDetail.active ? 'btn-danger' : 'btn-primary'}`}
                >
                  {deptDetail.active ? 'Deactivate Department' : 'Activate Department'}
                </button>
              </div>

              {/* Grid of Sections: Subjects, Goals, Platforms, Tools */}
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(500px, 1fr))', gap: '1.5rem' }}>
                
                {/* 1. Subjects Section */}
                <div className="glass-panel" style={{ padding: '1.5rem' }}>
                  <h3 style={{ fontSize: '1.15rem', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <BookOpen size={18} color="#38bdf8" /> Subjects ({deptDetail.subjects.length})
                  </h3>

                  {/* Add Subject Form */}
                  <form onSubmit={handleAddSubject} style={{ display: 'flex', gap: '0.5rem', marginBottom: '1.25rem', flexWrap: 'wrap' }}>
                    <input 
                      type="text" 
                      required 
                      placeholder="Subject Name" 
                      value={newSubjectName}
                      onChange={(e) => setNewSubjectName(e.target.value)}
                      className="input-field"
                      style={{ flex: 1, minWidth: '150px' }}
                    />
                    <input 
                      type="text" 
                      required 
                      placeholder="Code (e.g. CE301)" 
                      value={newSubjectCode}
                      onChange={(e) => setNewSubjectCode(e.target.value)}
                      className="input-field"
                      style={{ width: '100px' }}
                    />
                    <select 
                      value={newSubjectSemester}
                      onChange={(e) => setNewSubjectSemester(Number(e.target.value))}
                      className="input-field"
                      style={{ width: '85px' }}
                    >
                      {[1, 2, 3, 4, 5, 6, 7, 8].map(s => (
                        <option key={s} value={s}>Sem {s}</option>
                      ))}
                    </select>
                    <button type="submit" className="btn btn-primary btn-sm">
                      <Plus size={14} /> Add
                    </button>
                  </form>

                  {/* Subjects List */}
                  <div style={{ maxHeight: '320px', overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                    {deptDetail.subjects.map(s => (
                      <div key={s.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.6rem 0.8rem', background: 'rgba(255,255,255,0.02)', borderRadius: 'var(--radius-sm)', border: '1px solid rgba(255,255,255,0.05)' }}>
                        <div>
                          <span style={{ fontWeight: 600, color: '#ffffff', fontSize: '0.9rem' }}>{s.name}</span>
                          <span style={{ fontSize: '0.78rem', color: 'var(--text-secondary)', marginLeft: '0.5rem' }}>({s.code} • Sem {s.semester})</span>
                        </div>
                        <button 
                          onClick={() => handleToggleSubjectStatus(s.id, !!s.active)}
                          className={`btn btn-sm ${s.active ? 'btn-secondary' : 'btn-primary'}`}
                          style={{ padding: '0.2rem 0.5rem', fontSize: '0.75rem' }}
                        >
                          {s.active ? 'Deactivate' : 'Activate'}
                        </button>
                      </div>
                    ))}
                    {deptDetail.subjects.length === 0 && (
                      <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem', textAlign: 'center', padding: '1rem 0' }}>No subjects added yet.</p>
                    )}
                  </div>
                </div>

                {/* 2. Goals Section */}
                <div className="glass-panel" style={{ padding: '1.5rem' }}>
                  <h3 style={{ fontSize: '1.15rem', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <Target size={18} color="#a855f7" /> Study Goals ({deptDetail.goals.length})
                  </h3>

                  {/* Add Goal Form */}
                  <form onSubmit={handleAddGoal} style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', marginBottom: '1.25rem' }}>
                    <div style={{ display: 'flex', gap: '0.5rem' }}>
                      <input 
                        type="text" 
                        required 
                        placeholder="Key (e.g. GATE_EXAM)" 
                        value={newGoalKey}
                        onChange={(e) => setNewGoalKey(e.target.value)}
                        className="input-field"
                        style={{ width: '140px' }}
                      />
                      <input 
                        type="text" 
                        required 
                        placeholder="Goal Label (e.g. GATE Preparation)" 
                        value={newGoalLabel}
                        onChange={(e) => setNewGoalLabel(e.target.value)}
                        className="input-field"
                        style={{ flex: 1 }}
                      />
                    </div>
                    <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
                      <input 
                        type="text" 
                        placeholder="Short Description (optional)" 
                        value={newGoalDesc}
                        onChange={(e) => setNewGoalDesc(e.target.value)}
                        className="input-field"
                        style={{ flex: 1 }}
                      />
                      <label style={{ display: 'flex', alignItems: 'center', gap: '0.3rem', fontSize: '0.75rem', color: 'var(--text-secondary)', cursor: 'pointer', whiteSpace: 'nowrap' }}>
                        <input 
                          type="checkbox" 
                          checked={newGoalIsShared}
                          onChange={(e) => setNewGoalIsShared(e.target.checked)}
                        />
                        Shared Goal
                      </label>
                      <button type="submit" className="btn btn-primary btn-sm">
                        <Plus size={14} /> Add
                      </button>
                    </div>
                  </form>

                  {/* Goals List */}
                  <div style={{ maxHeight: '320px', overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                    {deptDetail.goals.map(g => (
                      <div key={g.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.6rem 0.8rem', background: 'rgba(255,255,255,0.02)', borderRadius: 'var(--radius-sm)', border: '1px solid rgba(255,255,255,0.05)' }}>
                        <div>
                          <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                            <span style={{ fontWeight: 600, color: '#ffffff', fontSize: '0.9rem' }}>{g.label}</span>
                            <span className="badge badge-indigo" style={{ fontSize: '0.68rem', padding: '0.1rem 0.35rem' }}>{g.goalKey}</span>
                            {g.departmentId === null && (
                              <span className="badge badge-amber" style={{ fontSize: '0.65rem', padding: '0.1rem 0.35rem' }}>Shared</span>
                            )}
                          </div>
                          {g.description && (
                            <p style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '0.2rem' }}>{g.description}</p>
                          )}
                        </div>
                        <button 
                          onClick={() => handleToggleGoalStatus(g.id, g.active)}
                          className={`btn btn-sm ${g.active ? 'btn-secondary' : 'btn-primary'}`}
                          style={{ padding: '0.2rem 0.5rem', fontSize: '0.75rem' }}
                        >
                          {g.active ? 'Deactivate' : 'Activate'}
                        </button>
                      </div>
                    ))}
                    {deptDetail.goals.length === 0 && (
                      <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem', textAlign: 'center', padding: '1rem 0' }}>No goals configured.</p>
                    )}
                  </div>
                </div>

                {/* 3. External Platforms Section */}
                <div className="glass-panel" style={{ padding: '1.5rem' }}>
                  <h3 style={{ fontSize: '1.15rem', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <ExternalLink size={18} color="#10b981" /> Curated Platforms ({deptDetail.platforms.length})
                  </h3>

                  {/* Add Platform Form */}
                  <form onSubmit={handleAddPlatform} style={{ display: 'flex', gap: '0.5rem', marginBottom: '1.25rem', flexWrap: 'wrap' }}>
                    <select 
                      value={newPlatform}
                      onChange={(e) => setNewPlatform(e.target.value as SourcePlatform)}
                      className="input-field"
                      style={{ flex: 1, minWidth: '150px' }}
                    >
                      {PLATFORM_OPTIONS.map(p => (
                        <option key={p} value={p}>{p}</option>
                      ))}
                    </select>
                    <input 
                      type="number" 
                      placeholder="Priority" 
                      value={newPlatformPriority}
                      onChange={(e) => setNewPlatformPriority(Number(e.target.value))}
                      className="input-field"
                      style={{ width: '80px' }}
                      title="Sorting Priority (higher comes first)"
                    />
                    <button type="submit" className="btn btn-primary btn-sm">
                      <Plus size={14} /> Add
                    </button>
                  </form>

                  {/* Platforms Chips */}
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
                    {deptDetail.platforms.map(p => (
                      <div key={p.id} style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', padding: '0.4rem 0.75rem', background: 'rgba(16, 185, 129, 0.1)', border: '1px solid rgba(16, 185, 129, 0.25)', borderRadius: '999px', fontSize: '0.82rem' }}>
                        <span style={{ fontWeight: 600, color: '#34d399' }}>{p.platform}</span>
                        <span style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>(p:{p.priority})</span>
                        <button 
                          onClick={() => handleDeletePlatform(p.id)} 
                          style={{ background: 'none', border: 'none', color: '#f87171', cursor: 'pointer', padding: 0, display: 'flex', alignItems: 'center' }}
                        >
                          <Trash2 size={12} />
                        </button>
                      </div>
                    ))}
                    {deptDetail.platforms.length === 0 && (
                      <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>No platforms mapped yet.</p>
                    )}
                  </div>
                </div>

                {/* 4. Tools Section */}
                <div className="glass-panel" style={{ padding: '1.5rem' }}>
                  <h3 style={{ fontSize: '1.15rem', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <Wrench size={18} color="#f59e0b" /> Software Tools & Stacks ({deptDetail.tools.length})
                  </h3>

                  {/* Add Tool Form */}
                  <form onSubmit={handleAddTool} style={{ display: 'flex', gap: '0.5rem', marginBottom: '1.25rem' }}>
                    <input 
                      type="text" 
                      required 
                      placeholder="Tool / Framework Name (e.g. AutoCAD, MATLAB, Wireshark)" 
                      value={newToolName}
                      onChange={(e) => setNewToolName(e.target.value)}
                      className="input-field"
                      style={{ flex: 1 }}
                    />
                    <button type="submit" className="btn btn-primary btn-sm">
                      <Plus size={14} /> Add
                    </button>
                  </form>

                  {/* Tools Tags */}
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
                    {deptDetail.tools.map(t => (
                      <div key={t.id} style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', padding: '0.4rem 0.75rem', background: 'rgba(245, 158, 11, 0.1)', border: '1px solid rgba(245, 158, 11, 0.25)', borderRadius: '999px', fontSize: '0.82rem' }}>
                        <span style={{ fontWeight: 600, color: '#fbbf24' }}>{t.name}</span>
                        <button 
                          onClick={() => handleDeleteTool(t.id)} 
                          style={{ background: 'none', border: 'none', color: '#f87171', cursor: 'pointer', padding: 0, display: 'flex', alignItems: 'center' }}
                        >
                          <Trash2 size={12} />
                        </button>
                      </div>
                    ))}
                    {deptDetail.tools.length === 0 && (
                      <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>No tools added yet.</p>
                    )}
                  </div>
                </div>

              </div>

            </div>
          )}

        </div>
      )}

      {/* Rejection Modal */}
      {rejectModalOpen && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.7)', backdropFilter: 'blur(4px)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000, padding: '1rem' }}>
          <div className="glass-panel" style={{ width: '100%', maxWidth: '480px', padding: '1.75rem', background: 'var(--bg-secondary)', border: '1px solid rgba(244,63,94,0.3)' }}>
            <h3 style={{ fontSize: '1.2rem', marginBottom: '0.5rem', color: '#fda4af' }}>Reject Resource Upload</h3>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '1rem' }}>
              Please provide feedback explaining why this resource is rejected so the student uploader can correct it.
            </p>

            <form onSubmit={handleRejectResource}>
              <textarea 
                required
                rows={4}
                placeholder="Reason for rejection (e.g. corrupt pages, unreadable handwriting, missing topic notes)..."
                value={rejectionReason}
                onChange={(e) => setRejectionReason(e.target.value)}
                className="input-field"
                style={{ marginBottom: '1.25rem', resize: 'vertical' }}
              />
              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' }}>
                <button type="button" onClick={() => setRejectModalOpen(false)} className="btn btn-secondary btn-sm">Cancel</button>
                <button type="submit" className="btn btn-danger btn-sm">Confirm Rejection</button>
              </div>
            </form>
          </div>
        </div>
      )}

    </div>
  );
};
