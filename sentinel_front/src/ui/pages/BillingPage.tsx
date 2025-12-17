import React, { useState } from "react";
import Sidebar from "@/components/commons/Sidebar";
import { useTheme } from "@/utils/store/themeContext";
import {
    CreditCard,
    CheckCircle,
    Clock,
    Loader2,
    X
} from "lucide-react";
import { usePlans, useSubscription, usePaymentHistory, useUpgradePlan } from "@hooks/useBilling";
// MercadoPago SDK not needed - we use redirect-based Checkout Pro
// import { initMercadoPago, Wallet } from '@mercadopago/sdk-react';
// initMercadoPago('...', { locale: 'es-AR' });

export default function BillingPage() {
    const { theme } = useTheme();
    const { data: plans, loading: plansLoading } = usePlans();
    const { data: subscription, loading: subLoading, refetch: refetchSubscription } = useSubscription();
    const { data: payments, loading: paymentsLoading } = usePaymentHistory();
    const { upgradeToPlan, loading: upgradeLoading } = useUpgradePlan();

    const [preferenceId, setPreferenceId] = useState<string | null>(null);
    const [initPoint, setInitPoint] = useState<string | null>(null);

    // Get user from session or local storage for fallback
    const user = JSON.parse(sessionStorage.getItem("user") || localStorage.getItem("user") || "{}");
    const displayPlan = subscription?.planName || user?.plan || "Free";
    const formatPlan = (p: string) => p === "PRO" ? "Professional" : p;

    const loading = plansLoading || subLoading || paymentsLoading;

    const handleUpgrade = async (planId: string) => {
        const result = await upgradeToPlan(planId);
        if (result && result.initPoint) {
            // Redirect directly to MercadoPago checkout page
            // This bypasses the need for Wallet component and public_key
            window.location.href = result.initPoint;
        }
    };

    const closeModal = () => {
        setPreferenceId(null);
        setInitPoint(null);
        refetchSubscription();
    };

    // Determine if this is a Mock checkout
    const isMock = preferenceId?.startsWith("MOCK_");

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
                    <h1 style={{ color: theme.colors.text.primary }}>Billing & Plans</h1>
                </header>

                <section className="content" style={{ padding: 24, position: 'relative' }}>
                    {/* Loading State */}
                    {loading && (
                        <div style={{ display: "flex", justifyContent: "center", padding: 40 }}>
                            <Loader2 size={32} color={theme.colors.primary} style={{ animation: "spin 1s linear infinite" }} />
                        </div>
                    )}

                    {!loading && (
                        <>
                            {/* Current Plan */}
                            <div
                                style={{
                                    background: theme.colors.surface,
                                    border: `1px solid ${theme.colors.border}`,
                                    borderRadius: 12,
                                    padding: 24,
                                    marginBottom: 24,
                                }}
                            >
                                <div style={{ display: "flex", alignItems: "center", gap: 12, marginBottom: 16 }}>
                                    <CreditCard size={24} color={theme.colors.primary} />
                                    <h2 style={{ color: theme.colors.text.primary, margin: 0 }}>Current Plan</h2>
                                </div>
                                <div style={{ display: "flex", alignItems: "center", gap: 16 }}>
                                    <span
                                        style={{
                                            background: theme.colors.primary,
                                            color: theme.colors.onPrimary,
                                            padding: "8px 16px",
                                            borderRadius: 8,
                                            fontWeight: 600,
                                        }}
                                    >
                                        {formatPlan(displayPlan)}
                                    </span>
                                    <span style={{ color: theme.colors.text.secondary }}>
                                        {subscription?.currentPeriodEnd
                                            ? `Renews on ${new Date(subscription.currentPeriodEnd).toLocaleDateString()}`
                                            : "Active via User Plan"}
                                    </span>
                                </div>
                            </div>

                            {/* Sandbox Mode Banner Removed */}

                            {/* Plans Grid */}
                            <h3 style={{ color: theme.colors.text.primary, marginBottom: 16 }}>Planes Disponibles</h3>
                            <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(280px, 1fr))", gap: 24 }}>
                                {(plans || []).sort((a, b) => {
                                    // Sort: FREE first, then PRO, then ENTERPRISE
                                    const order: Record<string, number> = { 'FREE': 0, 'PRO': 1, 'PROFESSIONAL': 1, 'ENTERPRISE': 2 };
                                    return (order[a.id.toUpperCase()] ?? 99) - (order[b.id.toUpperCase()] ?? 99);
                                }).map((plan) => {
                                    // Plan hierarchy: FREE < PRO/PROFESSIONAL < ENTERPRISE
                                    const planOrder: Record<string, number> = {
                                        'FREE': 0, 'PRO': 1, 'PROFESSIONAL': 1, 'ENTERPRISE': 2,
                                        // Legacy plans (lowercase)
                                        'free': 0, 'pro': 1, 'professional': 1, 'enterprise': 2,
                                    };

                                    const currentPlanId = subscription?.planId?.toUpperCase() || user?.plan?.toUpperCase() || 'FREE';
                                    const thisPlanId = plan.id.toUpperCase();

                                    const currentPlanOrder = planOrder[currentPlanId] ?? 0;
                                    const thisPlanOrder = planOrder[thisPlanId] ?? 0;

                                    const isCurrent = currentPlanId === thisPlanId;
                                    const isDowngrade = thisPlanOrder < currentPlanOrder;
                                    const isUpgrade = thisPlanOrder > currentPlanOrder;

                                    return (
                                        <div
                                            key={plan.id}
                                            style={{
                                                background: theme.colors.surface,
                                                border: isCurrent
                                                    ? `2px solid ${theme.colors.primary}`
                                                    : `1px solid ${theme.colors.border}`,
                                                borderRadius: 12,
                                                padding: 24,
                                                position: "relative",
                                                opacity: isDowngrade ? 0.6 : 1,
                                            }}
                                        >
                                            {isCurrent && (
                                                <span
                                                    style={{
                                                        position: "absolute",
                                                        top: -12,
                                                        right: 16,
                                                        background: theme.colors.primary,
                                                        color: theme.colors.onPrimary,
                                                        padding: "4px 12px",
                                                        borderRadius: 12,
                                                        fontSize: 12,
                                                        fontWeight: 600,
                                                    }}
                                                >
                                                    Current Plan
                                                </span>
                                            )}

                                            <h3 style={{ color: theme.colors.text.primary, marginBottom: 8 }}>{plan.name}</h3>
                                            <div style={{ marginBottom: 16 }}>
                                                <span style={{ fontSize: 32, fontWeight: 700, color: theme.colors.text.primary }}>
                                                    {plan.price === 0 ? 'Gratis' : `$${plan.price.toLocaleString('es-CO')}`}
                                                </span>
                                                <span style={{ color: theme.colors.text.secondary }}>
                                                    {plan.price > 0 ? ' COP/mes' : ''}
                                                </span>
                                            </div>

                                            <ul style={{ listStyle: "none", padding: 0, marginBottom: 24 }}>
                                                {(plan.features || []).map((feature, idx) => (
                                                    <li
                                                        key={idx}
                                                        style={{
                                                            display: "flex",
                                                            alignItems: "center",
                                                            gap: 8,
                                                            color: theme.colors.text.secondary,
                                                            marginBottom: 8,
                                                        }}
                                                    >
                                                        <CheckCircle size={16} color={theme.colors.success} />
                                                        {feature}
                                                    </li>
                                                ))}
                                            </ul>

                                            <button
                                                onClick={() => isUpgrade && handleUpgrade(plan.id)}
                                                style={{
                                                    width: "100%",
                                                    padding: "12px 24px",
                                                    borderRadius: 8,
                                                    border: isCurrent || isDowngrade ? "none" : `1px solid ${theme.colors.primary}`,
                                                    background: isUpgrade ? theme.colors.primary : theme.colors.surface,
                                                    color: isUpgrade ? theme.colors.onPrimary : theme.colors.text.tertiary,
                                                    fontWeight: 600,
                                                    cursor: isUpgrade && !upgradeLoading ? "pointer" : "not-allowed",
                                                    opacity: upgradeLoading ? 0.7 : 1,
                                                }}
                                                disabled={!isUpgrade || upgradeLoading}
                                            >
                                                {upgradeLoading ? "Processing..." :
                                                    isCurrent ? "✓ Current Plan" :
                                                        isDowngrade ? "—" :
                                                            "Upgrade"}
                                            </button>
                                        </div>
                                    );
                                })}
                            </div>

                            {/* Payment History */}
                            <div
                                style={{
                                    background: theme.colors.surface,
                                    border: `1px solid ${theme.colors.border}`,
                                    borderRadius: 12,
                                    padding: 24,
                                    marginTop: 24,
                                }}
                            >
                                <div style={{ display: "flex", alignItems: "center", gap: 12, marginBottom: 16 }}>
                                    <Clock size={20} color={theme.colors.primary} />
                                    <h3 style={{ color: theme.colors.text.primary, margin: 0 }}>Payment History</h3>
                                </div>
                                <table style={{ width: "100%", borderCollapse: "collapse" }}>
                                    <thead>
                                        <tr>
                                            {["Date", "Description", "Amount", "Status"].map((h) => (
                                                <th
                                                    key={h}
                                                    style={{
                                                        textAlign: "left",
                                                        padding: "12px 8px",
                                                        borderBottom: `1px solid ${theme.colors.border}`,
                                                        color: theme.colors.text.secondary,
                                                        fontWeight: 500,
                                                    }}
                                                >
                                                    {h}
                                                </th>
                                            ))}
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {(payments || []).map((payment) => (
                                            <tr key={payment.id}>
                                                <td style={{ padding: "12px 8px", color: theme.colors.text.primary }}>
                                                    {new Date(payment.createdAt).toLocaleDateString()}
                                                </td>
                                                <td style={{ padding: "12px 8px", color: theme.colors.text.secondary }}>
                                                    {payment.description}
                                                </td>
                                                <td style={{ padding: "12px 8px", color: theme.colors.text.primary }}>
                                                    ${payment.amount.toLocaleString('es-CO')} COP
                                                </td>
                                                <td style={{ padding: "12px 8px" }}>
                                                    <span
                                                        style={{
                                                            background:
                                                                payment.status === "PAID"
                                                                    ? theme.colors.success
                                                                    : payment.status === "PENDING"
                                                                        ? theme.colors.warning
                                                                        : theme.colors.danger,
                                                            color: theme.colors.onPrimary,
                                                            padding: "4px 8px",
                                                            borderRadius: 4,
                                                            fontSize: 12,
                                                        }}
                                                    >
                                                        {payment.status}
                                                    </span>
                                                </td>
                                            </tr>
                                        ))}
                                        {payments.length === 0 && (
                                            <tr>
                                                <td colSpan={4} style={{ textAlign: "center", padding: 20, color: theme.colors.text.tertiary }}>
                                                    No payments yet
                                                </td>
                                            </tr>
                                        )}
                                    </tbody>
                                </table>
                            </div>
                        </>
                    )}

                    {/* Dual Strategy Modal */}
                    {preferenceId && (
                        <div style={{
                            position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
                            backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex',
                            justifyContent: 'center', alignItems: 'center', zIndex: 1000
                        }}>
                            <div style={{
                                backgroundColor: theme.colors.surface, padding: '24px',
                                borderRadius: '12px', width: '100%', maxWidth: '500px',
                                position: 'relative', boxShadow: '0 4px 20px rgba(0,0,0,0.2)'
                            }}>
                                <button
                                    onClick={closeModal}
                                    style={{
                                        position: 'absolute', top: 12, right: 12,
                                        background: 'transparent', border: 'none',
                                        cursor: 'pointer', color: theme.colors.text.secondary
                                    }}
                                >
                                    <X size={24} />
                                </button>

                                <h2 style={{ lineHeight: '1.5', marginBottom: '16px', color: theme.colors.text.primary }}>
                                    {isMock ? "Confirm Subscription (Test Mode)" : "Complete Subscription"}
                                </h2>

                                <div id="wallet_container">
                                    {isMock ? (
                                        <div style={{ textAlign: 'center', padding: '20px 0' }}>
                                            <p style={{ marginBottom: 20, color: theme.colors.text.secondary }}>
                                                You are in Development Mode. <br />
                                                Click below to simulate a successful payment.
                                            </p>
                                            <button
                                                onClick={() => {
                                                    if (initPoint) window.location.href = initPoint;
                                                }}
                                                style={{
                                                    background: theme.colors.success,
                                                    color: '#fff',
                                                    border: 'none',
                                                    padding: '12px 24px',
                                                    borderRadius: '8px',
                                                    fontWeight: 'bold',
                                                    fontSize: '16px',
                                                    cursor: 'pointer',
                                                    width: '100%'
                                                }}
                                            >
                                                Simulate Payment
                                            </button>
                                        </div>
                                    ) : (
                                        // Redirect button instead of Wallet component
                                        <button
                                            onClick={() => {
                                                if (initPoint) window.location.href = initPoint;
                                            }}
                                            style={{
                                                background: theme.colors.primary,
                                                color: '#fff',
                                                border: 'none',
                                                padding: '12px 24px',
                                                borderRadius: '8px',
                                                fontWeight: 'bold',
                                                fontSize: '16px',
                                                cursor: 'pointer',
                                                width: '100%'
                                            }}
                                        >
                                            Continue to MercadoPago Checkout
                                        </button>
                                    )}
                                </div>
                            </div>
                        </div>
                    )}
                </section>
            </main>
        </div>
    );
}
