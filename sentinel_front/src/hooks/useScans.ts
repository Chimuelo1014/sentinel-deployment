import { useState } from 'react';
import axios from 'axios';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8000';

export interface ScanRequest {
    projectId: string;
    scanType?: string;
    targetRepo?: string;
    targetUrl?: string;
}

export interface Scan {
    id: string;
    projectId: string;
    tenantId: string;
    userId: string;
    status: string;
    scanType: string;
    createdAt: string;
    updatedAt: string;
    completedAt?: string;
}

export const useScans = () => {
    const [scans, setScans] = useState<Scan[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const fetchScans = async (tenantId: string) => {
        setLoading(true);
        setError(null);
        try {
            const token = localStorage.getItem('accessToken');
            const response = await axios.get(`${API_URL}/api/scans`, {
                headers: {
                    Authorization: `Bearer ${token}`,
                    'X-Tenant-Id': tenantId,
                },
                params: { page: 0, size: 50 },
            });
            setScans(response.data.content || response.data);
        } catch (err: any) {
            console.error('Failed to fetch scans:', err);
            setError(err.response?.data?.message || 'Failed to fetch scans');
        } finally {
            setLoading(false);
        }
    };

    const startScan = async (request: ScanRequest, tenantId: string) => {
        setLoading(true);
        setError(null);
        try {
            const token = localStorage.getItem('accessToken');
            const userId = localStorage.getItem('userId');

            if (!userId) {
                alert('DEBUG ERROR: User ID not found in localStorage. Please Logout and Login again.');
                console.error('User ID missing');
            }

            // Map frontend request to backend DTO
            const backendRequest = {
                type: request.scanType,
                targetUrl: request.targetUrl,
                targetRepo: request.targetRepo,
                projectId: request.projectId,
            };

            console.log('Starting scan with payload:', backendRequest);
            console.log('Headers:', { 'X-Tenant-Id': tenantId, 'X-User-Id': userId });

            const response = await axios.post(`${API_URL}/api/scans`, backendRequest, {
                headers: {
                    Authorization: `Bearer ${token}`,
                    'X-Tenant-Id': tenantId,
                    'X-User-Id': userId || '',
                },
            });
            return response.data;
        } catch (err: any) {
            console.error('Failed to start scan:', err);
            setError(err.response?.data?.message || 'Failed to start scan');
            throw err;
        } finally {
            setLoading(false);
        }
    };

    const getScan = async (scanId: string) => {
        setLoading(true);
        setError(null);
        try {
            const token = localStorage.getItem('accessToken');
            const response = await axios.get(`${API_URL}/api/scans/${scanId}`, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });
            return response.data;
        } catch (err: any) {
            console.error('Failed to fetch scan:', err);
            setError(err.response?.data?.message || 'Failed to fetch scan');
            throw err;
        } finally {
            setLoading(false);
        }
    };

    return {
        scans,
        loading,
        error,
        fetchScans,
        startScan,
        getScan,
    };
};
