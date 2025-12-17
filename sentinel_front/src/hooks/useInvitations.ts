import { useState, useEffect, useCallback } from "react";

const API = import.meta.env.VITE_API_URL || "http://localhost:8000";

export interface Invitation {
    id: string;
    email: string;
    role: string;
    status: "PENDING" | "ACCEPTED" | "REJECTED" | "EXPIRED" | "CANCELLED";
    createdAt: string;
    expiresAt: string;
}

export function useInvitations(tenantId: string | null) {
    const [invitations, setInvitations] = useState<Invitation[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const fetchInvitations = useCallback(async () => {
        if (!tenantId) return;
        try {
            setLoading(true);
            const token = localStorage.getItem("accessToken");
            const res = await fetch(`${API}/api/tenants/${tenantId}/invitations`, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });
            if (!res.ok) throw new Error("Failed to load invitations");
            const json = await res.json();
            setInvitations(json);
        } catch (e: any) {
            setError(e.message);
        } finally {
            setLoading(false);
        }
    }, [tenantId]);

    useEffect(() => {
        fetchInvitations();
    }, [fetchInvitations]);

    const inviteMember = async (email: string, role: string, projectIds?: string[]) => {
        if (!tenantId) throw new Error("No tenant selected");
        try {
            setLoading(true);
            const token = localStorage.getItem("accessToken");
            const body: any = { email, role };
            if (projectIds && projectIds.length > 0) {
                body.projectIds = projectIds;
            }
            const res = await fetch(`${API}/api/tenants/${tenantId}/invitations`, {
                method: "POST",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(body),
            });

            if (!res.ok) {
                const errJson = await res.json().catch(() => ({}));
                // Extract the actual error message from backend
                const errorMessage = errJson.message || errJson.error || "Failed to send invitation";
                throw new Error(errorMessage);
            }

            const newInvitation = await res.json();
            setInvitations((prev) => [...prev, newInvitation]);
            return newInvitation;
        } catch (e: any) {
            setError(e.message);
            throw e;
        } finally {
            setLoading(false);
        }
    };

    const cancelInvitation = async (invitationId: string) => {
        if (!tenantId) return;
        try {
            // Optimistic update
            setInvitations((prev) => prev.filter((inv) => inv.id !== invitationId));

            const token = localStorage.getItem("accessToken");
            const res = await fetch(`${API}/api/tenants/invitations/${invitationId}`, {
                method: "DELETE",
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });

            if (!res.ok) {
                // Revert on failure
                fetchInvitations();
                throw new Error("Failed to cancel invitation");
            }
        } catch (e: any) {
            setError(e.message);
        }
    };

    return { invitations, loading, error, inviteMember, cancelInvitation, refetch: fetchInvitations };
}
