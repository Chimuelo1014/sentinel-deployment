import React, { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import Sidebar from "@/components/commons/Sidebar";
import { useTheme } from "@/utils/store/themeContext";
import { ProjectsList } from "@/ui/components/ProjectsList";
import { useInvitations } from "@/hooks/useInvitations";
import { useProjects } from "@/hooks/useProjects";
import {
    Users,
    FolderOpen,
    ArrowLeft,
    Mail,
    Loader2,
    CheckCircle,
    XCircle,
    Trash2
} from "lucide-react";

export default function WorkspaceDetailsPage() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { theme } = useTheme();
    const [activeTab, setActiveTab] = useState<"projects" | "members">("projects");
    const workspaceId = id || null;

    // Invitations Hook
    const {
        invitations,
        loading: loadingInvites,
        error: inviteError,
        inviteMember,
        cancelInvitation
    } = useInvitations(workspaceId);

    // Projects Hook (for project selection in invitations)
    const { projects } = useProjects(workspaceId || "");

    // Invite Modal State
    const [isInviteModalOpen, setIsInviteModalOpen] = useState(false);
    const [inviteEmail, setInviteEmail] = useState("");
    const [inviteRole, setInviteRole] = useState("TENANT_USER");
    const [inviting, setInviting] = useState(false);
    const [selectedProjects, setSelectedProjects] = useState<string[]>([]);
    const [allProjects, setAllProjects] = useState(true); // Default to all projects

    const handleInvite = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            setInviting(true);
            // If "All Projects" is checked, don't send projectIds
            const projectIds = allProjects ? undefined : selectedProjects;
            await inviteMember(inviteEmail, inviteRole, projectIds);
            setIsInviteModalOpen(false);
            setInviteEmail("");
            setSelectedProjects([]);
            setAllProjects(true);
            alert("✅ Invitation sent successfully!");
        } catch (err: any) {
            console.error("Failed to invite", err);

            // Check for specific error messages
            const errorMessage = err.message || "Unknown error";

            if (errorMessage.includes("User limit reached") || errorMessage.includes("Upgrade your plan")) {
                alert("❌ Member Limit Reached\n\nYour current plan has reached its member limit. Please upgrade to BASIC, PRO, or ENTERPRISE plan to invite more members.\n\nGo to Billing → Plans to upgrade.");
            } else if (errorMessage.includes("already invited") || errorMessage.includes("already exists")) {
                alert("⚠️ This user has already been invited or is already a member of this workspace.");
            } else {
                alert(`❌ Failed to send invitation: ${errorMessage}`);
            }
        } finally {
            setInviting(false);
        }
    };

    return (
        <div className="app" style={{ background: theme.colors.background }}>
            <Sidebar />

            <main className="main">
                {/* Header */}
                <header
                    className="header"
                    style={{
                        background: theme.colors.surface,
                        borderBottom: `1px solid ${theme.colors.border}`,
                        padding: "0 24px",
                        height: 64,
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "space-between"
                    }}
                >
                    <div style={{ display: "flex", alignItems: "center", gap: 16 }}>
                        <button
                            onClick={() => navigate("/workspaces")}
                            style={{
                                background: "transparent",
                                border: "none",
                                cursor: "pointer",
                                color: theme.colors.text.secondary,
                                display: "flex",
                                alignItems: "center"
                            }}
                        >
                            <ArrowLeft size={20} />
                        </button>
                        <h1 style={{ color: theme.colors.text.primary, fontSize: 20, margin: 0 }}>
                            Workspace Dashboard
                        </h1>
                    </div>
                </header>

                {/* Tabs */}
                <div style={{
                    padding: "24px 24px 0",
                    borderBottom: `1px solid ${theme.colors.border}`,
                    display: "flex",
                    gap: 32
                }}>
                    <button
                        onClick={() => setActiveTab("projects")}
                        style={{
                            background: "transparent",
                            border: "none",
                            padding: "12px 4px",
                            cursor: "pointer",
                            borderBottom: activeTab === "projects" ? `2px solid ${theme.colors.primary}` : "2px solid transparent",
                            color: activeTab === "projects" ? theme.colors.primary : theme.colors.text.secondary,
                            fontWeight: 600,
                            display: "flex",
                            alignItems: "center",
                            gap: 8
                        }}
                    >
                        <FolderOpen size={18} />
                        Projects
                    </button>
                    <button
                        onClick={() => setActiveTab("members")}
                        style={{
                            background: "transparent",
                            border: "none",
                            padding: "12px 4px",
                            cursor: "pointer",
                            borderBottom: activeTab === "members" ? `2px solid ${theme.colors.primary}` : "2px solid transparent",
                            color: activeTab === "members" ? theme.colors.primary : theme.colors.text.secondary,
                            fontWeight: 600,
                            display: "flex",
                            alignItems: "center",
                            gap: 8
                        }}
                    >
                        <Users size={18} />
                        Members & Invitations
                    </button>
                </div>

                <section className="content" style={{ padding: 24 }}>
                    {/* PROJECTS TAB */}
                    {activeTab === "projects" && workspaceId && (
                        <ProjectsList workspaceId={workspaceId} />
                    )}

                    {/* MEMBERS TAB */}
                    {activeTab === "members" && (
                        <div>
                            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 20 }}>
                                <h3 style={{ color: theme.colors.text.primary, margin: 0 }}>Pending Invitations</h3>
                                <button
                                    onClick={() => setIsInviteModalOpen(true)}
                                    style={{
                                        display: "flex",
                                        alignItems: "center",
                                        gap: 8,
                                        padding: "8px 16px",
                                        background: theme.colors.primary,
                                        color: theme.colors.onPrimary,
                                        border: "none",
                                        borderRadius: 6,
                                        cursor: "pointer",
                                        fontWeight: 600
                                    }}
                                >
                                    <Mail size={16} />
                                    Invite Member
                                </button>
                            </div>

                            {loadingInvites && (
                                <div style={{ display: "flex", justifyContent: "center", padding: 20 }}>
                                    <Loader2 size={24} color={theme.colors.primary} style={{ animation: "spin 1s linear infinite" }} />
                                </div>
                            )}

                            {inviteError && (
                                <div style={{ color: theme.colors.danger, padding: 10 }}>Error: {inviteError}</div>
                            )}

                            {/* Invitations List */}
                            <div style={{ display: "grid", gap: 12 }}>
                                {!loadingInvites && invitations.length === 0 && (
                                    <div style={{ color: theme.colors.text.secondary, fontStyle: "italic" }}>No pending invitations.</div>
                                )}

                                {invitations.map((inv) => (
                                    <div
                                        key={inv.id}
                                        style={{
                                            background: theme.colors.surface,
                                            border: `1px solid ${theme.colors.border}`,
                                            borderRadius: 8,
                                            padding: "16px 20px",
                                            display: "flex",
                                            justifyContent: "space-between",
                                            alignItems: "center"
                                        }}
                                    >
                                        <div>
                                            <div style={{ color: theme.colors.text.primary, fontWeight: 500 }}>{inv.email}</div>
                                            <div style={{ color: theme.colors.text.tertiary, fontSize: 13 }}>
                                                Role: {inv.role} • Status: <span style={{ color: theme.colors.warning }}>{inv.status}</span>
                                            </div>
                                        </div>
                                        <button
                                            onClick={() => cancelInvitation(inv.id)}
                                            style={{
                                                background: "transparent",
                                                border: "none",
                                                color: theme.colors.danger,
                                                cursor: "pointer",
                                                display: "flex",
                                                alignItems: "center",
                                                gap: 6
                                            }}
                                            title="Cancel Invitation"
                                        >
                                            <Trash2 size={16} />
                                        </button>
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}
                </section>

                {/* Invite Modal */}
                {isInviteModalOpen && (
                    <div
                        style={{
                            position: "fixed",
                            top: 0,
                            right: 0,
                            bottom: 0,
                            left: 0,
                            background: "rgba(0,0,0,0.5)",
                            display: "flex",
                            alignItems: "center",
                            justifyContent: "center",
                            zIndex: 1000
                        }}
                    >
                        <form
                            onSubmit={handleInvite}
                            style={{
                                background: theme.colors.surface,
                                padding: 24,
                                borderRadius: 12,
                                width: 400,
                                border: `1px solid ${theme.colors.border}`
                            }}
                        >
                            <h2 style={{ color: theme.colors.text.primary, marginTop: 0 }}>Invite Member</h2>

                            <div style={{ marginBottom: 16 }}>
                                <label style={{ display: "block", color: theme.colors.text.secondary, marginBottom: 8, fontSize: 14 }}>
                                    Email Address
                                </label>
                                <input
                                    type="email"
                                    autoFocus
                                    required
                                    value={inviteEmail}
                                    onChange={(e) => setInviteEmail(e.target.value)}
                                    placeholder="colleague@company.com"
                                    style={{
                                        width: "100%",
                                        padding: "10px",
                                        borderRadius: 8,
                                        border: `1px solid ${theme.colors.border}`,
                                        background: theme.colors.background,
                                        color: theme.colors.text.primary
                                    }}
                                />
                            </div>

                            <div style={{ marginBottom: 24 }}>
                                <label style={{ display: "block", color: theme.colors.text.secondary, marginBottom: 8, fontSize: 14 }}>
                                    Project Access
                                </label>
                                <div style={{ marginBottom: 12 }}>
                                    <label style={{ display: "flex", alignItems: "center", gap: 8, cursor: "pointer" }}>
                                        <input
                                            type="checkbox"
                                            checked={allProjects}
                                            onChange={(e) => {
                                                setAllProjects(e.target.checked);
                                                if (e.target.checked) {
                                                    setSelectedProjects([]);
                                                }
                                            }}
                                            style={{ cursor: "pointer" }}
                                        />
                                        <span style={{ color: theme.colors.text.primary, fontSize: 14 }}>
                                            All Projects (current and future)
                                        </span>
                                    </label>
                                </div>

                                {!allProjects && (
                                    <div>
                                        <label style={{ display: "block", color: theme.colors.text.tertiary, marginBottom: 8, fontSize: 13 }}>
                                            Select specific projects:
                                        </label>
                                        <div style={{
                                            maxHeight: 150,
                                            overflowY: "auto",
                                            border: `1px solid ${theme.colors.border}`,
                                            borderRadius: 8,
                                            padding: 8,
                                            background: theme.colors.background
                                        }}>
                                            {projects.length === 0 ? (
                                                <p style={{ color: theme.colors.text.tertiary, fontSize: 13, textAlign: "center", padding: 8 }}>
                                                    No projects available
                                                </p>
                                            ) : (
                                                projects.map((project: any) => (
                                                    <label
                                                        key={project.id}
                                                        style={{
                                                            display: "flex",
                                                            alignItems: "center",
                                                            gap: 8,
                                                            padding: "6px 8px",
                                                            cursor: "pointer",
                                                            borderRadius: 4,
                                                            transition: "background 0.2s"
                                                        }}
                                                        onMouseEnter={(e) => {
                                                            e.currentTarget.style.background = theme.colors.surface;
                                                        }}
                                                        onMouseLeave={(e) => {
                                                            e.currentTarget.style.background = "transparent";
                                                        }}
                                                    >
                                                        <input
                                                            type="checkbox"
                                                            checked={selectedProjects.includes(project.id)}
                                                            onChange={(e) => {
                                                                if (e.target.checked) {
                                                                    setSelectedProjects([...selectedProjects, project.id]);
                                                                } else {
                                                                    setSelectedProjects(selectedProjects.filter(id => id !== project.id));
                                                                }
                                                            }}
                                                            style={{ cursor: "pointer" }}
                                                        />
                                                        <span style={{ color: theme.colors.text.primary, fontSize: 14 }}>
                                                            {project.name}
                                                        </span>
                                                    </label>
                                                ))
                                            )}
                                        </div>
                                        {!allProjects && selectedProjects.length === 0 && (
                                            <p style={{ color: theme.colors.warning, fontSize: 12, marginTop: 8 }}>
                                                ⚠️ Please select at least one project or check "All Projects"
                                            </p>
                                        )}
                                    </div>
                                )}
                            </div>

                            <div style={{ display: "flex", justifyContent: "flex-end", gap: 12 }}>
                                <button
                                    type="button"
                                    onClick={() => setIsInviteModalOpen(false)}
                                    style={{
                                        padding: "8px 16px",
                                        background: "transparent",
                                        color: theme.colors.text.secondary,
                                        border: "none",
                                        cursor: "pointer"
                                    }}
                                >
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    disabled={inviting || (!allProjects && selectedProjects.length === 0)}
                                    style={{
                                        padding: "8px 16px",
                                        background: theme.colors.primary,
                                        color: theme.colors.onPrimary,
                                        border: "none",
                                        borderRadius: 8,
                                        cursor: "pointer",
                                        opacity: (inviting || (!allProjects && selectedProjects.length === 0)) ? 0.7 : 1
                                    }}
                                >
                                    {inviting ? "Sending..." : "Send Invitation"}
                                </button>
                            </div>
                        </form>
                    </div>
                )}
            </main>
        </div>
    );
}
