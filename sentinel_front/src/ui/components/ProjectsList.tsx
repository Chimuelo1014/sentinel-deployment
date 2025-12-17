import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTheme } from "@/utils/store/themeContext";
import { useProjects } from "@/hooks/useProjects"; // Using absolute alias
import { Plus, Box, Loader2 } from "lucide-react";
import LimitReachedModal from "@/ui/components/LimitReachedModal";

// Plan limits configuration (matches backend UserLimitsService)
const PLAN_LIMITS: Record<string, { maxTenants: number; maxProjects: number }> = {
    FREE: { maxTenants: 0, maxProjects: 0 },
    PRO: { maxTenants: 3, maxProjects: 6 },
    PROFESSIONAL: { maxTenants: 3, maxProjects: 6 },
    ENTERPRISE: { maxTenants: 6, maxProjects: 12 },
};

export function ProjectsList({ workspaceId }: { workspaceId: string }) {
    const { theme } = useTheme();
    const navigate = useNavigate();
    const { projects, loading, error, createProject, refetch } = useProjects(workspaceId);
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [showLimitModal, setShowLimitModal] = useState(false);
    const [name, setName] = useState("");
    const [description, setDescription] = useState("");
    const [creating, setCreating] = useState(false);

    // Get user plan from session/localStorage
    const user = JSON.parse(sessionStorage.getItem("user") || localStorage.getItem("user") || "{}");
    const currentPlan = (user?.plan || "FREE").toUpperCase();
    const planLimits = PLAN_LIMITS[currentPlan] || PLAN_LIMITS.FREE;
    const currentProjectCount = projects?.length || 0;
    // Note: This is per-workspace count. Backend checks global limit.
    // We show a warning if workspace count is high, but backend is authoritative.
    const isAtLocalLimit = currentProjectCount >= planLimits.maxProjects && planLimits.maxProjects !== -1 && planLimits.maxProjects > 0;

    const handleCreate = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            setCreating(true);
            await createProject({ name, description });
            setIsCreateModalOpen(false);
            setName("");
            setDescription("");
            // refetch handles update automatically via state, but explicit refetch ensures sync
        } catch (err: any) {
            console.error("Failed to create project", err);
            // Check if backend returned limit error (402)
            if (err.message?.includes("limit") || err.message?.includes("402")) {
                setIsCreateModalOpen(false);
                setShowLimitModal(true);
            }
        } finally {
            setCreating(false);
        }
    };

    const handleNewProjectClick = () => {
        if (currentPlan === 'FREE') {
            // FREE users can't create anything - they should see upgrade modal on WorkspacesPage
            alert("Con el plan FREE no puedes crear proyectos. Por favor actualiza tu plan.");
            return;
        }
        // For paid plans, let backend validate the global limit
        setIsCreateModalOpen(true);
    };

    if (loading && projects.length === 0) return <div style={{ padding: 20 }}>Loading projects...</div>;
    if (error) return <div style={{ color: theme.colors.danger }}>Error: {error}</div>;

    return (
        <div>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 20 }}>
                <h3 style={{ color: theme.colors.text.primary, margin: 0 }}>Projects</h3>
                <button
                    onClick={handleNewProjectClick}
                    style={{
                        display: "flex",
                        alignItems: "center",
                        gap: 8,
                        padding: "8px 16px",
                        background: currentPlan === 'FREE' ? theme.colors.surface : theme.colors.primary,
                        color: currentPlan === 'FREE' ? theme.colors.text.tertiary : theme.colors.onPrimary,
                        border: currentPlan === 'FREE' ? `1px solid ${theme.colors.border}` : "none",
                        borderRadius: 6,
                        cursor: "pointer",
                        fontSize: 14,
                        opacity: currentPlan === 'FREE' ? 0.7 : 1
                    }}
                >
                    <Plus size={16} />
                    {currentPlan === 'FREE' ? 'Upgrade to Create' : 'New Project'}
                </button>
            </div>

            {projects.length === 0 ? (
                <div style={{
                    textAlign: 'center',
                    padding: 40,
                    border: `1px dashed ${theme.colors.border}`,
                    borderRadius: 8,
                    background: theme.colors.background
                }}>
                    <Box size={32} color={theme.colors.text.tertiary} />
                    <p style={{ color: theme.colors.text.secondary }}>No projects found in this workspace</p>
                </div>
            ) : (
                <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(280px, 1fr))", gap: 16 }}>
                    {projects.map((project) => (
                        <div
                            key={project.id}
                            onClick={() => navigate(`/projects/${project.id}`)}
                            style={{
                                background: theme.colors.background,
                                border: `1px solid ${theme.colors.border}`,
                                borderRadius: 8,
                                padding: 16,
                                cursor: "pointer",
                                transition: "all 0.2s ease",
                            }}
                            onMouseEnter={(e) => {
                                e.currentTarget.style.transform = "translateY(-2px)";
                                e.currentTarget.style.boxShadow = "0 4px 12px rgba(0,0,0,0.1)";
                            }}
                            onMouseLeave={(e) => {
                                e.currentTarget.style.transform = "translateY(0)";
                                e.currentTarget.style.boxShadow = "none";
                            }}
                        >
                            <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 8 }}>
                                <Box size={20} color={theme.colors.primary} />
                                <h4 style={{ margin: 0, color: theme.colors.text.primary }}>{project.name}</h4>
                            </div>
                            <p style={{ color: theme.colors.text.secondary, fontSize: 13, margin: "0 0 12px 0" }}>
                                {project.description || "No description"}
                            </p>
                            <div style={{ display: "flex", gap: 12, fontSize: 12, color: theme.colors.text.tertiary }}>
                                <span>{project.repoCount || 0} Repos</span>
                                <span>{project.domainCount || 0} Domains</span>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {isCreateModalOpen && (
                <div
                    style={{
                        position: "fixed",
                        top: 0,
                        left: 0,
                        right: 0,
                        bottom: 0,
                        background: "rgba(0,0,0,0.5)",
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                        zIndex: 1000,
                    }}
                >
                    <form
                        onSubmit={handleCreate}
                        style={{
                            background: theme.colors.surface,
                            padding: 24,
                            borderRadius: 12,
                            width: 400,
                            border: `1px solid ${theme.colors.border}`,
                        }}
                    >
                        <h2 style={{ color: theme.colors.text.primary, marginTop: 0 }}>New Project</h2>
                        <div style={{ marginBottom: 16 }}>
                            <label style={{ display: "block", color: theme.colors.text.secondary, marginBottom: 8, fontSize: 14 }}>
                                Project Name
                            </label>
                            <input
                                autoFocus
                                value={name}
                                onChange={(e) => setName(e.target.value)}
                                placeholder="My Project"
                                required
                                style={{
                                    width: "100%",
                                    padding: "10px",
                                    borderRadius: 8,
                                    border: `1px solid ${theme.colors.border}`,
                                    background: theme.colors.background,
                                    color: theme.colors.text.primary,
                                }}
                            />
                        </div>
                        <div style={{ marginBottom: 24 }}>
                            <label style={{ display: "block", color: theme.colors.text.secondary, marginBottom: 8, fontSize: 14 }}>
                                Description
                            </label>
                            <input
                                value={description}
                                onChange={(e) => setDescription(e.target.value)}
                                placeholder="Optional description"
                                style={{
                                    width: "100%",
                                    padding: "10px",
                                    borderRadius: 8,
                                    border: `1px solid ${theme.colors.border}`,
                                    background: theme.colors.background,
                                    color: theme.colors.text.primary,
                                }}
                            />
                        </div>
                        <div style={{ display: "flex", justifyContent: "flex-end", gap: 12 }}>
                            <button
                                type="button"
                                onClick={() => setIsCreateModalOpen(false)}
                                style={{
                                    padding: "8px 16px",
                                    background: "transparent",
                                    color: theme.colors.text.secondary,
                                    border: "none",
                                    cursor: "pointer",
                                }}
                            >
                                Cancel
                            </button>
                            <button
                                type="submit"
                                disabled={creating}
                                style={{
                                    padding: "8px 16px",
                                    background: theme.colors.primary,
                                    color: theme.colors.onPrimary,
                                    border: "none",
                                    borderRadius: 8,
                                    cursor: "pointer",
                                    opacity: creating ? 0.7 : 1,
                                }}
                            >
                                {creating ? "Creating..." : "Create Project"}
                            </button>
                        </div>
                    </form>
                </div>
            )}

            {/* Limit Reached Modal */}
            <LimitReachedModal
                isOpen={showLimitModal}
                onClose={() => setShowLimitModal(false)}
                resourceType="projects"
                currentCount={currentProjectCount}
                maxLimit={planLimits.maxProjects}
                currentPlan={currentPlan}
            />
        </div>
    );
}
