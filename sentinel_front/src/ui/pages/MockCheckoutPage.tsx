import React, { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import Sidebar from '@/components/commons/Sidebar';
import { useTheme } from '@/utils/store/themeContext';
import { CreditCard, CheckCircle, Loader2 } from 'lucide-react';

export default function MockCheckoutPage() {
    const { theme } = useTheme();
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const [processing, setProcessing] = useState(false);
    const [completed, setCompleted] = useState(false);

    const planId = searchParams.get('plan') || 'PROFESSIONAL';
    const userId = searchParams.get('user') || '';
    const isMock = searchParams.get('mock') === 'true';

    const planDetails: Record<string, { name: string; price: number; currency: string }> = {
        'FREE': { name: 'Free', price: 0, currency: 'USD' },
        'PROFESSIONAL': { name: 'Professional', price: 29.99, currency: 'USD' },
        'ENTERPRISE': { name: 'Enterprise', price: 99.99, currency: 'USD' },
    };

    const plan = planDetails[planId.toUpperCase()] || planDetails['PROFESSIONAL'];

    const handleConfirmPayment = async () => {
        setProcessing(true);

        try {
            const token = localStorage.getItem('accessToken');
            const paymentId = 'test_' + Date.now();

            // Call the BFF webhook test endpoint to simulate MP payment approval
            // This proxies to billing-service which updates user plan via RabbitMQ event
            const res = await fetch(
                `${import.meta.env.VITE_API_URL || 'http://localhost:8000'}/api/subscriptions/webhook/test?paymentId=${paymentId}&planId=${planId.toUpperCase()}&userId=${userId}`,
                {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json',
                    },
                }
            );

            if (!res.ok) {
                const error = await res.json().catch(() => ({}));
                throw new Error(error.message || 'Failed to confirm payment');
            }

            setCompleted(true);

            // Redirect to billing page after 2 seconds
            setTimeout(() => {
                navigate('/billing?status=success&plan=' + planId);
            }, 2000);

        } catch (error) {
            console.error('Payment error:', error);
            alert('Error processing payment. Please try again.');
        } finally {
            setProcessing(false);
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
                    <h1 style={{ color: theme.colors.text.primary }}>
                        {isMock ? '🧪 Mock Payment' : '💳 MercadoPago Checkout'}
                    </h1>
                </header>

                <section className="content" style={{ padding: 24, display: 'flex', justifyContent: 'center' }}>
                    <div
                        style={{
                            background: theme.colors.surface,
                            border: `1px solid ${theme.colors.border}`,
                            borderRadius: 16,
                            padding: 32,
                            maxWidth: 500,
                            width: '100%',
                        }}
                    >
                        {!completed ? (
                            <>
                                {/* Plan Info */}
                                <div style={{ textAlign: 'center', marginBottom: 32 }}>
                                    <div
                                        style={{
                                            width: 80,
                                            height: 80,
                                            borderRadius: '50%',
                                            background: `linear-gradient(135deg, ${theme.colors.primary}, ${theme.colors.primaryDark})`,
                                            display: 'flex',
                                            alignItems: 'center',
                                            justifyContent: 'center',
                                            margin: '0 auto 16px',
                                        }}
                                    >
                                        <CreditCard size={40} color="#fff" />
                                    </div>
                                    <h2 style={{ color: theme.colors.text.primary, margin: 0 }}>
                                        Upgrade to {plan.name}
                                    </h2>
                                    <p style={{ color: theme.colors.text.secondary, marginTop: 8 }}>
                                        You're about to subscribe to the {plan.name} plan
                                    </p>
                                </div>

                                {/* Price */}
                                <div
                                    style={{
                                        background: theme.colors.background,
                                        padding: 24,
                                        borderRadius: 12,
                                        textAlign: 'center',
                                        marginBottom: 24,
                                    }}
                                >
                                    <span style={{ fontSize: 48, fontWeight: 700, color: theme.colors.text.primary }}>
                                        ${plan.price}
                                    </span>
                                    <span style={{ color: theme.colors.text.secondary, fontSize: 18 }}> / month</span>
                                </div>

                                {/* Mock Notice */}
                                {isMock && (
                                    <div
                                        style={{
                                            background: theme.colors.warning + '20',
                                            border: `1px solid ${theme.colors.warning}`,
                                            borderRadius: 8,
                                            padding: 12,
                                            marginBottom: 24,
                                            textAlign: 'center',
                                        }}
                                    >
                                        <p style={{ color: theme.colors.warning, margin: 0, fontWeight: 600 }}>
                                            🧪 This is a MOCK checkout for testing
                                        </p>
                                        <p style={{ color: theme.colors.text.secondary, margin: '4px 0 0 0', fontSize: 14 }}>
                                            No real payment will be processed
                                        </p>
                                    </div>
                                )}

                                {/* Buttons */}
                                <div style={{ display: 'flex', gap: 12 }}>
                                    <button
                                        onClick={() => navigate('/billing')}
                                        style={{
                                            flex: 1,
                                            padding: '14px 24px',
                                            background: 'transparent',
                                            color: theme.colors.text.secondary,
                                            border: `1px solid ${theme.colors.border}`,
                                            borderRadius: 8,
                                            cursor: 'pointer',
                                            fontWeight: 600,
                                        }}
                                    >
                                        Cancel
                                    </button>
                                    <button
                                        onClick={handleConfirmPayment}
                                        disabled={processing}
                                        style={{
                                            flex: 2,
                                            padding: '14px 24px',
                                            background: `linear-gradient(135deg, ${theme.colors.primary}, ${theme.colors.primaryDark})`,
                                            color: '#fff',
                                            border: 'none',
                                            borderRadius: 8,
                                            cursor: processing ? 'not-allowed' : 'pointer',
                                            fontWeight: 600,
                                            display: 'flex',
                                            alignItems: 'center',
                                            justifyContent: 'center',
                                            gap: 8,
                                            opacity: processing ? 0.7 : 1,
                                        }}
                                    >
                                        {processing ? (
                                            <>
                                                <Loader2 size={20} style={{ animation: 'spin 1s linear infinite' }} />
                                                Processing...
                                            </>
                                        ) : (
                                            <>Confirm Payment</>
                                        )}
                                    </button>
                                </div>
                            </>
                        ) : (
                            /* Success State */
                            <div style={{ textAlign: 'center', padding: 32 }}>
                                <div
                                    style={{
                                        width: 80,
                                        height: 80,
                                        borderRadius: '50%',
                                        background: theme.colors.success,
                                        display: 'flex',
                                        alignItems: 'center',
                                        justifyContent: 'center',
                                        margin: '0 auto 24px',
                                    }}
                                >
                                    <CheckCircle size={48} color="#fff" />
                                </div>
                                <h2 style={{ color: theme.colors.text.primary, margin: 0 }}>
                                    Payment Successful! 🎉
                                </h2>
                                <p style={{ color: theme.colors.text.secondary, marginTop: 12 }}>
                                    You are now subscribed to the {plan.name} plan.
                                </p>
                                <p style={{ color: theme.colors.text.tertiary, marginTop: 8, fontSize: 14 }}>
                                    Redirecting to billing page...
                                </p>
                            </div>
                        )}
                    </div>
                </section>
            </main>
        </div>
    );
}
