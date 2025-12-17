import { useEffect, useState } from "react";

const API = import.meta.env.VITE_API_URL || "http://localhost:8000"; // Kong Gateway

type ApiState<T> = {
    data: T;
    loading: boolean;
    error: string | null;
    refetch: () => void;
};

/* ======================================================
   BILLING TYPES
====================================================== */

export interface Plan {
    id: string;
    name: string;
    price: number;
    period: string;
    features: string[];
    maxProjects: number;
    maxScansPerMonth: number;
}

export interface Subscription {
    id: string;
    planId: string;
    planName: string;
    status: "ACTIVE" | "CANCELLED" | "EXPIRED";
    currentPeriodStart: string;
    currentPeriodEnd: string;
    price: number;
}

export interface Payment {
    id: string;
    amount: number;
    status: "PAID" | "PENDING" | "FAILED";
    description: string;
    createdAt: string;
}

/* ======================================================
   PLANS HOOK
====================================================== */

export function usePlans(): ApiState<Plan[]> {
    const [data, setData] = useState<Plan[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const fetchPlans = async () => {
        try {
            setLoading(true);
            const res = await fetch(`${API}/api/billing/plans`);
            if (!res.ok) throw new Error("Failed to load plans");
            const json = await res.json();
            const rawData = Array.isArray(json) ? json : (json.content || []);

            // Map backend DTO to Frontend Interface
            const mappedPlans = rawData.map((p: any) => ({
                id: p.id,
                name: p.name,
                price: p.priceCop ?? p.monthlyPriceCop ?? p.price ?? p.monthlyPriceUsd ?? 0, // Prefer COP price
                period: p.period ?? "month",
                features: p.features ?? [ // Use backend features if available, otherwise build from limits
                    p.maxTenants === 0 ? "Sin workspaces" : p.maxTenants === -1 ? "Workspaces ilimitados" : `${p.maxTenants} Workspaces`,
                    p.maxProjects === -1 ? "Proyectos ilimitados" : `${p.maxProjects} Proyectos`,
                    p.maxUsers === -1 ? "Usuarios ilimitados" : `${p.maxUsers} Usuarios por workspace`,
                    // p.maxDomains > 0 ? `${p.maxDomains} Dominios` : null, // Hidden as requested
                    // p.maxRepos > 0 ? `${p.maxRepos} Repositorios` : null, // Hidden as requested
                    p.includesBlockchain ? "Seguridad Blockchain" : null,
                    p.recommended ? "⭐ Recomendado" : null
                ].filter(Boolean),
                maxProjects: p.maxProjects,
                maxScansPerMonth: -1 // specific field not in generic plan DTO, defaulting
            }));

            setData(mappedPlans);
        } catch (e: any) {
            setError(e.message);
            // Fallback to mock data if API fails
            setData([
                {
                    id: "FREE",
                    name: "Free",
                    price: 0,
                    period: "month",
                    features: ["0 Workspaces", "0 Proyectos", "Soporte comunidad"],
                    maxProjects: 0,
                    maxScansPerMonth: 0,
                },
                {
                    id: "PRO",
                    name: "Professional",
                    price: 120000,
                    period: "month",
                    features: ["Proyectos ilimitados", "Escaneos ilimitados", "Soporte prioritario", "Análisis avanzado"],
                    maxProjects: -1,
                    maxScansPerMonth: -1,
                },
                {
                    id: "ENTERPRISE",
                    name: "Enterprise",
                    price: 400000,
                    period: "month",
                    features: ["Todo en Pro", "Soporte dedicado", "Integraciones personalizadas", "Garantía SLA"],
                    maxProjects: -1,
                    maxScansPerMonth: -1,
                },
            ]);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchPlans();
    }, []);

    return { data, loading, error, refetch: fetchPlans };
}

/* ======================================================
   SUBSCRIPTION HOOK
====================================================== */

export function useSubscription(): ApiState<Subscription | null> {
    const [data, setData] = useState<Subscription | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const fetchSubscription = async () => {
        try {
            setLoading(true);
            const token = localStorage.getItem("accessToken");
            const res = await fetch(`${API}/api/billing/subscriptions/me`, {
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
            });
            if (!res.ok) throw new Error("Failed to load subscription");
            const json = await res.json();
            setData(json);
        } catch (e: any) {
            setError(e.message);
            // Don't use fallback - show actual error state
            setData(null);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchSubscription();
    }, []);

    return { data, loading, error, refetch: fetchSubscription };
}

/* ======================================================
   PAYMENT HISTORY HOOK
====================================================== */

export function usePaymentHistory(): ApiState<Payment[]> {
    const [data, setData] = useState<Payment[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const fetchPayments = async () => {
        try {
            setLoading(true);
            const token = localStorage.getItem("accessToken");
            // Fixed URL: /api/payments-history/me (matches PaymentsHistoryController)
            const res = await fetch(`${API}/api/payments-history/me`, {
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
            });
            if (!res.ok) throw new Error("Failed to load payment history");
            const json = await res.json();
            // Ensure array safety
            setData(Array.isArray(json) ? json : (json.content || []));
        } catch (e: any) {
            setError(e.message);
            // No fallback - show actual empty state instead of mock data
            setData([]);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchPayments();
    }, []);

    return { data, loading, error, refetch: fetchPayments };
}

/* ======================================================
   UPGRADE PLAN HOOK
====================================================== */

export function useUpgradePlan() {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const upgradeToPlan = async (planId: string): Promise<{ initPoint?: string; preferenceId?: string } | null> => {
        try {
            setLoading(true);
            setError(null);
            const token = localStorage.getItem("accessToken");

            // Call checkout endpoint (SubscriptionsController)
            const res = await fetch(`${API}/api/subscriptions/checkout`, {
                method: "POST",
                headers: {
                    Authorization: `Bearer ${token}`,
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({ planId }),
            });

            if (!res.ok) {
                const errorData = await res.json().catch(() => ({}));
                throw new Error(errorData.message || `Failed to create checkout: ${res.status}`);
            }

            const checkoutData = await res.json();
            return checkoutData;

        } catch (e: any) {
            setError(e.message);
            console.error("❌ Checkout error:", e);
            alert(`Error creating checkout: ${e.message}`);
            return null;
        } finally {
            setLoading(false);
        }
    };

    return { upgradeToPlan, loading, error };
}
