import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Sidebar from '@/components/commons/Sidebar';
import { useProjects } from '@/hooks/useProjects';
import { useScans } from '@/hooks/useScans';
import { useTheme } from '@/utils/store/themeContext';

export const ProjectDetailPage = () => {
    const { projectId } = useParams<{ projectId: string }>();
    const navigate = useNavigate();
    const { theme } = useTheme();
    const currentTenantId = localStorage.getItem('selectedTenantId');

    const { projects, loading: projectsLoading, updateProject, deleteProject } = useProjects(currentTenantId || '');
    const { scans, loading: scansLoading, fetchScans, startScan } = useScans();

    const [activeTab, setActiveTab] = useState<'overview' | 'scans' | 'domains' | 'repos'>('overview');
    const [project, setProject] = useState<any>(null);
    const [startingScan, setStartingScan] = useState(false);
    const [loadingProject, setLoadingProject] = useState(true);
    const [showEditModal, setShowEditModal] = useState(false);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [editName, setEditName] = useState('');
    const [editDescription, setEditDescription] = useState('');
    const [saving, setSaving] = useState(false);

    // Fetch project directly if we don't have tenantId in localStorage
    useEffect(() => {
        const fetchProjectDirectly = async () => {
            if (!projectId) return;

            try {
                setLoadingProject(true);
                const token = localStorage.getItem("accessToken");
                const API = import.meta.env.VITE_API_URL || "http://localhost:8000";

                const res = await fetch(`${API}/api/projects/${projectId}`, {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                });

                if (res.ok) {
                    const projectData = await res.json();
                    setProject(projectData);

                    // Set tenantId in localStorage for future use
                    if (projectData.tenantId) {
                        localStorage.setItem('selectedTenantId', projectData.tenantId);
                        // Fetch scans for this tenant
                        fetchScans(projectData.tenantId);
                    }
                } else {
                    console.error('Failed to fetch project');
                }
            } catch (error) {
                console.error('Error fetching project:', error);
            } finally {
                setLoadingProject(false);
            }
        };

        // If we don't have currentTenantId, fetch project directly
        if (!currentTenantId && projectId) {
            fetchProjectDirectly();
        }
    }, [projectId, currentTenantId]);

    useEffect(() => {
        console.log('ProjectDetailPage - Effect triggered', {
            projectId,
            currentTenantId,
            projectsLoading,
            projectsCount: projects.length,
            projects: projects.map(p => ({ id: p.id, name: p.name }))
        });

        if (projectId && currentTenantId && !projectsLoading) {
            // Find project from loaded projects
            const foundProject = projects.find((p: any) => p.id === projectId);
            console.log('ProjectDetailPage - Found project:', foundProject);

            if (foundProject) {
                setProject(foundProject);
                setLoadingProject(false);
                // Fetch scans for this tenant
                fetchScans(currentTenantId);
            } else {
                console.error('ProjectDetailPage - Project not found!', {
                    lookingFor: projectId,
                    availableProjects: projects.map(p => p.id)
                });
                setLoadingProject(false);
            }
        }
    }, [projectId, currentTenantId, projects, projectsLoading]);

    const [showScanModal, setShowScanModal] = useState(false);
    const [scanForm, setScanForm] = useState({
        repoUrl: '',
        targetUrl: '',
        scanType: 'FULL'
    });

    const handleStartScan = async (e: React.FormEvent) => {
        e.preventDefault();

        // Robust ID retrieval
        const tenantIdToUse = currentTenantId || project?.tenantId;
        console.log('Start Scan clicked', { projectId, tenantIdToUse, scanForm });

        if (!projectId) {
            alert('Error: Project ID is missing');
            return;
        }
        if (!tenantIdToUse) {
            alert('Error: Tenant ID is NOT found in storage OR project data. Wait for project to load.');
            return;
        }

        setStartingScan(true);
        try {
            await startScan(
                {
                    projectId,
                    scanType: scanForm.scanType,
                    targetRepo: scanForm.repoUrl,
                    targetUrl: scanForm.targetUrl || undefined
                },
                tenantIdToUse
            );
            alert('Scan started successfully!');
            setShowScanModal(false);
            setScanForm({ repoUrl: '', targetUrl: '', scanType: 'FULL' });
            fetchScans(tenantIdToUse); // Refresh scan list
        } catch (error) {
            console.error(error);
            alert('Failed to start scan (Check console for details)');
        } finally {
            setStartingScan(false);
        }
    };

    const handleEditProject = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!projectId) return;
        try {
            setSaving(true);
            await updateProject(projectId, { name: editName, description: editDescription });
            setProject({ ...project, name: editName, description: editDescription });
            setShowEditModal(false);
            alert('✅ Project updated successfully!');
        } catch (error) {
            console.error('Failed to update project:', error);
            alert('❌ Failed to update project');
        } finally {
            setSaving(false);
        }
    };

    const handleDeleteProject = async () => {
        if (!projectId) return;
        try {
            setSaving(true);
            await deleteProject(projectId);
            setShowDeleteModal(false);
            alert('✅ Project deleted successfully!');
            navigate('/workspaces');
        } catch (error) {
            console.error('Failed to delete project:', error);
            alert('❌ Failed to delete project');
        } finally {
            setSaving(false);
        }
    };

    // Initialize edit form when modal opens
    React.useEffect(() => {
        if (showEditModal && project) {
            setEditName(project.name);
            setEditDescription(project.description || '');
        }
    }, [showEditModal, project]);


    if (loadingProject || !project) {
        return (
            <Sidebar>
                <div style={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    minHeight: '100vh',
                    background: theme.colors.background
                }}>
                    <div style={{ textAlign: 'center' }}>
                        <div style={{
                            width: 48,
                            height: 48,
                            border: `2px solid ${theme.colors.primary}`,
                            borderTop: '2px solid transparent',
                            borderRadius: '50%',
                            margin: '0 auto',
                            animation: 'spin 1s linear infinite'
                        }}></div>
                        <p style={{ marginTop: 16, color: theme.colors.text.secondary }}>Loading project...</p>
                    </div>
                </div>
            </Sidebar>
        );
    }

    const projectScans = scans.filter((scan: any) => scan.projectId === projectId);

    return (
        <Sidebar>
            <div style={{ minHeight: '100vh', background: theme.colors.background, padding: 24 }}>
                <div style={{ maxWidth: 1200, margin: '0 auto' }}>
                    {/* Header */}
                    <div style={{
                        background: theme.colors.surface,
                        borderRadius: 12,
                        padding: 24,
                        marginBottom: 24,
                        border: `1px solid ${theme.colors.border}`
                    }}>
                        <button
                            onClick={() => navigate(-1)}
                            style={{
                                background: 'transparent',
                                border: 'none',
                                color: theme.colors.primary,
                                cursor: 'pointer',
                                marginBottom: 16,
                                display: 'flex',
                                alignItems: 'center',
                                gap: 8,
                                fontSize: 14,
                                padding: 0
                            }}
                        >
                            ← Back to Workspace
                        </button>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                            <div>
                                <h1 style={{ fontSize: 28, fontWeight: 700, color: theme.colors.text.primary, margin: 0 }}>
                                    {project.name}
                                </h1>
                                <p style={{ color: theme.colors.text.secondary, marginTop: 8, margin: 0 }}>
                                    {project.description || 'No description'}
                                </p>
                            </div>
                            <div style={{ display: 'flex', gap: 12 }}>
                                <button
                                    onClick={() => setShowEditModal(true)}
                                    style={{
                                        display: 'flex',
                                        alignItems: 'center',
                                        gap: 6,
                                        padding: '8px 16px',
                                        background: 'transparent',
                                        color: theme.colors.text.secondary,
                                        border: `1px solid ${theme.colors.border}`,
                                        borderRadius: 8,
                                        cursor: 'pointer',
                                        fontSize: 14
                                    }}
                                >
                                    ✏️ Edit
                                </button>
                                <button
                                    onClick={() => setShowDeleteModal(true)}
                                    style={{
                                        display: 'flex',
                                        alignItems: 'center',
                                        gap: 6,
                                        padding: '8px 16px',
                                        background: theme.colors.danger,
                                        color: '#fff',
                                        border: 'none',
                                        borderRadius: 8,
                                        cursor: 'pointer',
                                        fontSize: 14
                                    }}
                                >
                                    🗑️ Delete
                                </button>
                            </div>
                        </div>
                    </div>

                    {/* Tabs */}
                    <div style={{
                        background: theme.colors.surface,
                        borderRadius: 12,
                        marginBottom: 24,
                        border: `1px solid ${theme.colors.border}`
                    }}>
                        <div style={{ borderBottom: `1px solid ${theme.colors.border}` }}>
                            <nav style={{ display: 'flex', gap: 32, padding: '0 24px' }}>
                                {(['overview', 'scans', 'domains', 'repos'] as const).map((tab) => (
                                    <button
                                        key={tab}
                                        onClick={() => setActiveTab(tab)}
                                        style={{
                                            padding: '16px 8px',
                                            borderBottom: activeTab === tab ? `2px solid ${theme.colors.primary}` : '2px solid transparent',
                                            background: 'transparent',
                                            border: 'none',
                                            color: activeTab === tab ? theme.colors.primary : theme.colors.text.secondary,
                                            fontWeight: 500,
                                            fontSize: 14,
                                            cursor: 'pointer',
                                            transition: 'all 0.2s'
                                        }}
                                    >
                                        {tab.charAt(0).toUpperCase() + tab.slice(1)}
                                    </button>
                                ))}
                            </nav>
                        </div>

                        {/* Tab Content */}
                        <div style={{ padding: 24 }}>
                            {activeTab === 'overview' && (
                                <div>
                                    <h2 style={{ fontSize: 20, fontWeight: 600, marginBottom: 16, color: theme.colors.text.primary }}>
                                        Project Overview
                                    </h2>
                                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: 16 }}>
                                        <div style={{ background: theme.colors.background, padding: 16, borderRadius: 8 }}>
                                            <p style={{ fontSize: 14, color: theme.colors.text.secondary, margin: 0 }}>Project ID</p>
                                            <p style={{ fontFamily: 'monospace', fontSize: 14, marginTop: 4, color: theme.colors.text.primary }}>
                                                {project.id}
                                            </p>
                                        </div>
                                        <div style={{ background: theme.colors.background, padding: 16, borderRadius: 8 }}>
                                            <p style={{ fontSize: 14, color: theme.colors.text.secondary, margin: 0 }}>Status</p>
                                            <p style={{ fontWeight: 600, marginTop: 4, color: theme.colors.text.primary }}>
                                                {project.status}
                                            </p>
                                        </div>
                                        <div style={{ background: theme.colors.background, padding: 16, borderRadius: 8 }}>
                                            <p style={{ fontSize: 14, color: theme.colors.text.secondary, margin: 0 }}>Created</p>
                                            <p style={{ fontSize: 14, marginTop: 4, color: theme.colors.text.primary }}>
                                                {new Date(project.createdAt).toLocaleDateString()}
                                            </p>
                                        </div>
                                        <div style={{ background: theme.colors.background, padding: 16, borderRadius: 8 }}>
                                            <p style={{ fontSize: 14, color: theme.colors.text.secondary, margin: 0 }}>Total Scans</p>
                                            <p style={{ fontSize: 24, fontWeight: 700, marginTop: 4, color: theme.colors.text.primary }}>
                                                {projectScans.length}
                                            </p>
                                        </div>
                                    </div>
                                </div>
                            )}

                            {activeTab === 'scans' && (
                                <div>
                                    <div className="flex justify-between items-center mb-6">
                                        <h2 className="text-xl font-semibold">Security Scans</h2>
                                        <button
                                            onClick={() => setShowScanModal(true)}
                                            disabled={startingScan}
                                            className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
                                            style={{
                                                background: theme.colors.primary,
                                                color: 'white',
                                                padding: '8px 16px',
                                                borderRadius: 8,
                                                border: 'none',
                                                fontWeight: 500,
                                                cursor: startingScan ? 'not-allowed' : 'pointer',
                                                opacity: startingScan ? 0.6 : 1,
                                                fontSize: 14
                                            }}
                                        >
                                            {startingScan ? 'Starting...' : 'New Scan'}
                                        </button>
                                    </div>

                                    {/* Scan Modal */}
                                    {showScanModal && (
                                        <div style={{
                                            position: 'fixed',
                                            top: 0, left: 0, right: 0, bottom: 0,
                                            background: 'rgba(0,0,0,0.5)',
                                            display: 'flex',
                                            alignItems: 'center',
                                            justifyContent: 'center',
                                            zIndex: 1000
                                        }}>
                                            <div style={{
                                                background: theme.colors.surface,
                                                padding: 24,
                                                borderRadius: 12,
                                                width: '100%',
                                                maxWidth: 500,
                                                border: `1px solid ${theme.colors.border}`
                                            }}>
                                                <h3 style={{ marginTop: 0, marginBottom: 16, color: theme.colors.text.primary }}>Start Security Scan</h3>
                                                <form onSubmit={handleStartScan}>
                                                    <div style={{ marginBottom: 16 }}>
                                                        <label style={{ display: 'block', marginBottom: 8, color: theme.colors.text.secondary }}>
                                                            Target Repository URL {['SAST', 'SCA', 'FULL'].includes(scanForm.scanType) && '(Required)'}
                                                        </label>
                                                        <input
                                                            type="text"
                                                            required={['SAST', 'SCA', 'FULL'].includes(scanForm.scanType)}
                                                            placeholder="https://github.com/user/repo.git"
                                                            value={scanForm.repoUrl}
                                                            onChange={(e) => setScanForm({ ...scanForm, repoUrl: e.target.value })}
                                                            style={{
                                                                width: '100%',
                                                                padding: '8px 12px',
                                                                borderRadius: 6,
                                                                border: `1px solid ${theme.colors.border}`,
                                                                background: theme.colors.background,
                                                                color: theme.colors.text.primary
                                                            }}
                                                        />
                                                    </div>
                                                    <div style={{ marginBottom: 16 }}>
                                                        <label style={{ display: 'block', marginBottom: 8, color: theme.colors.text.secondary }}>
                                                            Target URL {['DAST', 'CONTAINER'].includes(scanForm.scanType) && '(Required for DAST/Container)'}
                                                        </label>
                                                        <input
                                                            type="text"
                                                            placeholder="https://example.com"
                                                            value={scanForm.targetUrl}
                                                            onChange={(e) => setScanForm({ ...scanForm, targetUrl: e.target.value })}
                                                            style={{
                                                                width: '100%',
                                                                padding: '8px 12px',
                                                                borderRadius: 6,
                                                                border: `1px solid ${theme.colors.border}`,
                                                                background: theme.colors.background,
                                                                color: theme.colors.text.primary
                                                            }}
                                                        />
                                                    </div>
                                                    <div style={{ marginBottom: 24 }}>
                                                        <label style={{ display: 'block', marginBottom: 8, color: theme.colors.text.secondary }}>Scan Type</label>
                                                        <select
                                                            value={scanForm.scanType}
                                                            onChange={(e) => setScanForm({ ...scanForm, scanType: e.target.value })}
                                                            style={{
                                                                width: '100%',
                                                                padding: '8px 12px',
                                                                borderRadius: 6,
                                                                border: `1px solid ${theme.colors.border}`,
                                                                background: theme.colors.background,
                                                                color: theme.colors.text.primary
                                                            }}
                                                        >
                                                            <option value="SAST">SAST (Static Analysis)</option>
                                                            <option value="DAST">DAST (Dynamic Analysis)</option>
                                                            <option value="CONTAINER">Container Scan (Trivy)</option>
                                                        </select>
                                                    </div>
                                                    <div style={{ display: 'flex', gap: 12, justifyContent: 'flex-end' }}>
                                                        <button
                                                            type="button"
                                                            onClick={() => setShowScanModal(false)}
                                                            style={{
                                                                padding: '8px 16px',
                                                                border: `1px solid ${theme.colors.border}`,
                                                                background: 'transparent',
                                                                color: theme.colors.text.primary,
                                                                borderRadius: 6,
                                                                cursor: 'pointer'
                                                            }}
                                                        >
                                                            Cancel
                                                        </button>
                                                        <button
                                                            type="submit"
                                                            disabled={startingScan}
                                                            style={{
                                                                padding: '8px 16px',
                                                                background: theme.colors.primary,
                                                                color: 'white',
                                                                border: 'none',
                                                                borderRadius: 6,
                                                                cursor: 'pointer',
                                                                opacity: startingScan ? 0.7 : 1
                                                            }}
                                                        >
                                                            {startingScan ? 'Starting...' : 'Start Scan'}
                                                        </button>
                                                    </div>
                                                </form>
                                            </div>
                                        </div>
                                    )}

                                    {scansLoading ? (
                                        <p style={{ color: theme.colors.text.secondary }}>Loading scans...</p>
                                    ) : projectScans.length === 0 ? (
                                        <p style={{ color: theme.colors.text.secondary }}>No scans yet. Start your first scan!</p>
                                    ) : (
                                        <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                                            {projectScans.map((scan: any) => (
                                                <div
                                                    key={scan.id}
                                                    style={{
                                                        background: theme.colors.background,
                                                        padding: 16,
                                                        borderRadius: 8,
                                                        border: `1px solid ${theme.colors.border}`
                                                    }}
                                                >
                                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start' }}>
                                                        <div>
                                                            <p style={{ fontWeight: 600, margin: 0, color: theme.colors.text.primary }}>
                                                                Scan #{scan.id.substring(0, 8)}
                                                            </p>
                                                            <p style={{ fontSize: 14, color: theme.colors.text.secondary, marginTop: 4 }}>
                                                                {new Date(scan.createdAt).toLocaleString()}
                                                            </p>
                                                        </div>
                                                        <span style={{
                                                            padding: '4px 12px',
                                                            borderRadius: 12,
                                                            fontSize: 12,
                                                            fontWeight: 500,
                                                            background: scan.status === 'COMPLETED' ? '#10b981' : scan.status === 'RUNNING' ? '#3b82f6' : '#6b7280',
                                                            color: 'white'
                                                        }}>
                                                            {scan.status}
                                                        </span>
                                                    </div>
                                                </div>
                                            ))}
                                        </div>
                                    )}
                                </div>
                            )}

                            {activeTab === 'domains' && (
                                <div>
                                    <h2 style={{ fontSize: 20, fontWeight: 600, marginBottom: 16, color: theme.colors.text.primary }}>Domains</h2>
                                    <p style={{ color: theme.colors.text.secondary }}>Domain management coming soon...</p>
                                </div>
                            )}

                            {activeTab === 'repos' && (
                                <div>
                                    <h2 style={{ fontSize: 20, fontWeight: 600, marginBottom: 16, color: theme.colors.text.primary }}>Repositories</h2>
                                    <p style={{ color: theme.colors.text.secondary }}>Repository management coming soon...</p>
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            </div>

            {/* Edit Project Modal */}
            {showEditModal && (
                <div style={{
                    position: 'fixed',
                    top: 0,
                    left: 0,
                    right: 0,
                    bottom: 0,
                    background: 'rgba(0,0,0,0.5)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    zIndex: 2000
                }}>
                    <form onSubmit={handleEditProject} style={{
                        background: theme.colors.surface,
                        padding: 24,
                        borderRadius: 12,
                        width: 400,
                        border: `1px solid ${theme.colors.border}`
                    }}>
                        <h2 style={{ color: theme.colors.text.primary, marginTop: 0 }}>Edit Project</h2>
                        <div style={{ marginBottom: 16 }}>
                            <label style={{ display: 'block', color: theme.colors.text.secondary, marginBottom: 8, fontSize: 14 }}>
                                Project Name
                            </label>
                            <input
                                autoFocus
                                value={editName}
                                onChange={(e) => setEditName(e.target.value)}
                                required
                                style={{
                                    width: '100%',
                                    padding: '10px',
                                    borderRadius: 8,
                                    border: `1px solid ${theme.colors.border}`,
                                    background: theme.colors.background,
                                    color: theme.colors.text.primary
                                }}
                            />
                        </div>
                        <div style={{ marginBottom: 24 }}>
                            <label style={{ display: 'block', color: theme.colors.text.secondary, marginBottom: 8, fontSize: 14 }}>
                                Description
                            </label>
                            <input
                                value={editDescription}
                                onChange={(e) => setEditDescription(e.target.value)}
                                style={{
                                    width: '100%',
                                    padding: '10px',
                                    borderRadius: 8,
                                    border: `1px solid ${theme.colors.border}`,
                                    background: theme.colors.background,
                                    color: theme.colors.text.primary
                                }}
                            />
                        </div>
                        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 12 }}>
                            <button
                                type="button"
                                onClick={() => setShowEditModal(false)}
                                style={{
                                    padding: '8px 16px',
                                    background: 'transparent',
                                    color: theme.colors.text.secondary,
                                    border: 'none',
                                    cursor: 'pointer'
                                }}
                            >
                                Cancel
                            </button>
                            <button
                                type="submit"
                                disabled={saving}
                                style={{
                                    padding: '8px 16px',
                                    background: theme.colors.primary,
                                    color: theme.colors.onPrimary,
                                    border: 'none',
                                    borderRadius: 8,
                                    cursor: 'pointer',
                                    opacity: saving ? 0.7 : 1
                                }}
                            >
                                {saving ? 'Saving...' : 'Save Changes'}
                            </button>
                        </div>
                    </form>
                </div>
            )}

            {/* Delete Project Modal */}
            {showDeleteModal && (
                <div style={{
                    position: 'fixed',
                    top: 0,
                    left: 0,
                    right: 0,
                    bottom: 0,
                    background: 'rgba(0,0,0,0.5)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    zIndex: 2000
                }}>
                    <div style={{
                        background: theme.colors.surface,
                        padding: 24,
                        borderRadius: 12,
                        width: 400,
                        border: `1px solid ${theme.colors.border}`
                    }}>
                        <h2 style={{ color: theme.colors.text.primary, marginTop: 0 }}>Delete Project</h2>
                        <p style={{ color: theme.colors.text.secondary, marginBottom: 24 }}>
                            Are you sure you want to delete <strong>{project.name}</strong>? This action cannot be undone.
                        </p>
                        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 12 }}>
                            <button
                                onClick={() => setShowDeleteModal(false)}
                                style={{
                                    padding: '8px 16px',
                                    background: 'transparent',
                                    color: theme.colors.text.secondary,
                                    border: `1px solid ${theme.colors.border}`,
                                    borderRadius: 8,
                                    cursor: 'pointer'
                                }}
                            >
                                Cancel
                            </button>
                            <button
                                onClick={handleDeleteProject}
                                disabled={saving}
                                style={{
                                    padding: '8px 16px',
                                    background: theme.colors.danger,
                                    color: '#fff',
                                    border: 'none',
                                    borderRadius: 8,
                                    cursor: 'pointer',
                                    opacity: saving ? 0.7 : 1
                                }}
                            >
                                {saving ? 'Deleting...' : 'Delete Project'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </Sidebar>
    );
};
