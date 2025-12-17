import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import Sidebar from "@/components/commons/Sidebar";
import { useTheme } from "@/utils/store/themeContext";
import {
    Folder,
    Plus,
    Settings,
    ChevronRight,
    Users,
    FileCode,
    Loader2,
} from "lucide-react";
import { useWorkspaces, useCreateWorkspace } from "@hooks/useWorkspaces";
import { useSubscription } from "@hooks/useBilling";
import { ProjectsList } from "@/ui/components/ProjectsList";
import UpgradePlanModal from "@/ui/components/UpgradePlanModal";
import LimitReachedModal from "@/ui/components/LimitReachedModal";

// Plan limits configuration (matches backend UserLimitsService)
const PLAN_LIMITS: Record<string, { maxTenants: number; maxProjects: number }> = {
    FREE: { maxTenants: 0, maxProjects: 0 },
    PRO: { maxTenants: 3, maxProjects: 6 },
    PROFESSIONAL: { maxTenants: 3, maxProjects: 6 },
    ENTERPRISE: { maxTenants: 6, maxProjects: 12 },
};

export default function WorkspacesPage() {
    const { theme } = useTheme();
    const navigate = useNavigate();
    const { data: workspaces, loading, error, refetch } = useWorkspaces();
    const { data: subscription } = useSubscription(); // Check subscription
    const { createWorkspace, loading: creating, upgradeRequired } = useCreateWorkspace();
    const [selectedWorkspace, setSelectedWorkspace] = useState<string | null>(null);
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [showUpgradeModal, setShowUpgradeModal] = useState(false);
    const [showLimitModal, setShowLimitModal] = useState(false);
    const [newName, setNewName] = useState("");
    const [newDesc, setNewDesc] = useState("");

    // Retrieve user from session or local storage for fallback plan check
    const user = JSON.parse(sessionStorage.getItem("user") || localStorage.getItem("user") || "{}");
    const currentPlan = (subscription?.planId || user?.plan || "FREE").toUpperCase();
    const planLimits = PLAN_LIMITS[currentPlan] || PLAN_LIMITS.FREE;
    const currentWorkspaceCount = workspaces?.length || 0;
    const isAtLimit = currentWorkspaceCount >= planLimits.maxTenants && planLimits.maxTenants !== -1;

    // Helper to check if creation is allowed: Check Subscription OR User Session
    const canCreateWorkspace = ((subscription?.planId && subscription.planId !== "FREE" && subscription?.planId !== "free")
        || (user?.plan === "PRO" || user?.plan === "ENTERPRISE" || user?.plan === "PROFESSIONAL"))
        && !isAtLimit;

    const handleCreate = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            await createWorkspace({ name: newName, description: newDesc });
            setIsCreateModalOpen(false);
            setNewName("");
            setNewDesc("");
            refetch(); // Refresh list
        } catch (err) {
            console.error("Failed to create workspace", err);
            // Check if it's an upgrade required error (402)
            if (upgradeRequired) {
                setShowUpgradeModal(true);
                setIsCreateModalOpen(false);
            }
        }
    };

    return (
        <div className="app" style={{ background: theme.colors.background }}>
            <Sidebar />

            <main className="main">
                <header
                    className="header"
                    style={{
                        background: theme.colors.surface,
                        borderBottom: `1px solid ${theme.colors.border}`,
                    }}
                >
                    <h1 style={{ color: theme.colors.text.primary }}>Workspaces</h1>
                    <button
                        onClick={() => {
                            if (canCreateWorkspace) {
                                setIsCreateModalOpen(true);
                            } else if (isAtLimit && currentPlan !== 'FREE') {
                                // User has a paid plan but reached limit
                                setShowLimitModal(true);
                            } else {
                                // User is on FREE plan
                                setShowUpgradeModal(true);
                            }
                        }}
                        style={{
                            display: "flex",
                            alignItems: "center",
                            gap: 8,
                            padding: "10px 20px",
                            background: canCreateWorkspace ? theme.colors.primary : theme.colors.surface,
                            color: canCreateWorkspace ? theme.colors.onPrimary : theme.colors.text.tertiary,
                            border: canCreateWorkspace ? "none" : `1px solid ${theme.colors.border}`,
                            borderRadius: 8,
                            fontWeight: 600,
                            cursor: "pointer",
                            opacity: canCreateWorkspace ? 1 : 0.7,
                        }}
                    >
                        {canCreateWorkspace ? (
                            <>
                                <Plus size={18} />
                                New Workspace
                            </>
                        ) : isAtLimit ? (
                            <>
                                <Plus size={18} />
                                Limit Reached ({currentWorkspaceCount}/{planLimits.maxTenants})
                            </>
                        ) : (
                            <>
                                <Plus size={18} />
                                Upgrade to Create
                            </>
                        )}
                    </button>
                </header>

                <section className="content" style={{ padding: 24 }}>
                    {/* Loading State */}
                    {loading && (
                        <div style={{ display: "flex", justifyContent: "center", padding: 40 }}>
                            <Loader2 size={32} color={theme.colors.primary} style={{ animation: "spin 1s linear infinite" }} />
                        </div>
                    )}

                    {/* Error State */}
                    {error && (
                        <div style={{ color: theme.colors.danger, padding: 20, textAlign: "center" }}>
                            Error loading workspaces: {error}
                            <button onClick={refetch} style={{ marginLeft: 10, color: theme.colors.primary }}>
                                Retry
                            </button>
                        </div>
                    )}

                    {/* Workspaces Grid */}
                    {!loading && !error && (
                        <div
                            style={{
                                display: "grid",
                                gridTemplateColumns: "repeat(auto-fill, minmax(320px, 1fr))",
                                gap: 20,
                            }}
                        >
                            {workspaces.map((workspace) => (
                                <div
                                    key={workspace.id}
                                    onClick={() => navigate(`/workspaces/${workspace.id}`)}
                                    style={{
                                        background: theme.colors.surface,
                                        border: `1px solid ${theme.colors.border}`,
                                        borderRadius: 12,
                                        padding: 20,
                                        cursor: "pointer",
                                        transition: "all 0.2s ease",
                                    }}
                                >
                                    {/* Header */}
                                    <div
                                        style={{
                                            display: "flex",
                                            justifyContent: "space-between",
                                            alignItems: "flex-start",
                                            marginBottom: 12,
                                        }}
                                    >
                                        <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
                                            <div
                                                style={{
                                                    width: 40,
                                                    height: 40,
                                                    borderRadius: 8,
                                                    background: theme.colors.primary,
                                                    display: "flex",
                                                    alignItems: "center",
                                                    justifyContent: "center",
                                                }}
                                            >
                                                <Folder size={20} color={theme.colors.onPrimary} />
                                            </div>
                                            <div>
                                                <h3 style={{ color: theme.colors.text.primary, margin: 0 }}>
                                                    {workspace.name}
                                                </h3>
                                                <p
                                                    style={{
                                                        color: theme.colors.text.secondary,
                                                        fontSize: 13,
                                                        margin: 0,
                                                    }}
                                                >
                                                    {workspace.description}
                                                </p>
                                            </div>
                                        </div>
                                        <button
                                            onClick={(e) => {
                                                e.stopPropagation();
                                            }}
                                            style={{
                                                background: "transparent",
                                                border: "none",
                                                cursor: "pointer",
                                                color: theme.colors.text.tertiary,
                                            }}
                                        >
                                            <Settings size={18} />
                                        </button>
                                    </div>

                                    {/* Stats */}
                                    <div
                                        style={{
                                            display: "flex",
                                            gap: 16,
                                            marginBottom: 16,
                                            paddingTop: 12,
                                            borderTop: `1px solid ${theme.colors.border}`,
                                        }}
                                    >
                                        <div style={{ display: "flex", alignItems: "center", gap: 6 }}>
                                            <FileCode size={16} color={theme.colors.text.tertiary} />
                                            <span style={{ color: theme.colors.text.secondary, fontSize: 13 }}>
                                                {workspace.projectCount} Projects
                                            </span>
                                        </div>
                                        <div style={{ display: "flex", alignItems: "center", gap: 6 }}>
                                            <Users size={16} color={theme.colors.text.tertiary} />
                                            <span style={{ color: theme.colors.text.secondary, fontSize: 13 }}>
                                                {workspace.memberCount} Members
                                            </span>
                                        </div>
                                    </div>

                                    {/* Footer */}
                                    <div
                                        style={{
                                            display: "flex",
                                            justifyContent: "space-between",
                                            alignItems: "center",
                                        }}
                                    >
                                        <span style={{ color: theme.colors.text.tertiary, fontSize: 12 }}>
                                            Last activity: {workspace.lastActivity}
                                        </span>
                                        <ChevronRight size={18} color={theme.colors.text.tertiary} />
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}

                    {/* Empty State */}
                    {!loading && !error && workspaces.length === 0 && (
                        <div
                            style={{
                                textAlign: "center",
                                padding: 60,
                                background: theme.colors.surface,
                                borderRadius: 12,
                                border: `1px dashed ${theme.colors.border}`,
                            }}
                        >
                            <Folder size={48} color={theme.colors.text.tertiary} />
                            <h3 style={{ color: theme.colors.text.primary, marginTop: 16 }}>
                                No Workspaces Yet
                            </h3>
                            <p style={{ color: theme.colors.text.secondary, marginBottom: 24 }}>
                                Create your first workspace to organize your projects
                            </p>
                            <button
                                onClick={() => {
                                    if (canCreateWorkspace) {
                                        setIsCreateModalOpen(true);
                                    } else {
                                        setShowUpgradeModal(true);
                                    }
                                }}
                                style={{
                                    padding: "12px 24px",
                                    background: theme.colors.primary,
                                    color: theme.colors.onPrimary,
                                    border: "none",
                                    borderRadius: 8,
                                    fontWeight: 600,
                                    cursor: "pointer",
                                    opacity: canCreateWorkspace ? 1 : 0.8,
                                }}
                            >
                                {canCreateWorkspace ? "Create Workspace" : "Upgrade to Create"}
                            </button>
                        </div>
                    )}

                    {/* Projects Section */}
                    {selectedWorkspace && (
                        <div style={{ marginTop: 40, borderTop: `1px solid ${theme.colors.border}`, paddingTop: 24 }}>
                            <ProjectsList workspaceId={selectedWorkspace} />
                        </div>
                    )}
                </section>

                {/* Create Modal */}
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
                            <h2 style={{ color: theme.colors.text.primary, marginTop: 0 }}>
                                New Workspace
                            </h2>
                            <div style={{ marginBottom: 16 }}>
                                <label
                                    style={{
                                        display: "block",
                                        color: theme.colors.text.secondary,
                                        marginBottom: 8,
                                        fontSize: 14,
                                    }}
                                >
                                    Workspace Name
                                </label>
                                <input
                                    autoFocus
                                    value={newName}
                                    onChange={(e) => setNewName(e.target.value)}
                                    placeholder="e.g. My Company"
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
                                <label
                                    style={{
                                        display: "block",
                                        color: theme.colors.text.secondary,
                                        marginBottom: 8,
                                        fontSize: 14,
                                    }}
                                >
                                    Description (Optional)
                                </label>
                                <input
                                    value={newDesc}
                                    onChange={(e) => setNewDesc(e.target.value)}
                                    placeholder="Brief description..."
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
                                    {creating ? "Creating..." : "Create Workspace"}
                                </button>
                            </div>
                        </form>
                    </div>
                )}
            </main>

            {/* Upgrade Modal */}
            <UpgradePlanModal
                isOpen={showUpgradeModal}
                onClose={() => setShowUpgradeModal(false)}
            />

            {/* Limit Reached Modal */}
            <LimitReachedModal
                isOpen={showLimitModal}
                onClose={() => setShowLimitModal(false)}
                resourceType="workspaces"
                currentCount={currentWorkspaceCount}
                maxLimit={planLimits.maxTenants}
                currentPlan={currentPlan}
            />
        </div>
    );
}
