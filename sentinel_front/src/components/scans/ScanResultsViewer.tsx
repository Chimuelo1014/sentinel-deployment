import React, { useState } from 'react';
import { UnifiedVulnerability } from '../../types/scanResults';
import {
    parseScanResults,
    getSeverityColor,
    getSeverityBgColor,
    groupBySeverity,
    getSeverityStats,
} from '../../utils/scanResultsParser';
import { AlertTriangle, CheckCircle, Info, XCircle, ChevronDown, ChevronRight, Code, FileText, Package, Globe } from 'lucide-react';

interface ScanResultsViewerProps {
    scan: any;
}

export const ScanResultsViewer: React.FC<ScanResultsViewerProps> = ({ scan }) => {
    const [expandedVuln, setExpandedVuln] = useState<string | null>(null);
    const [selectedSeverity, setSelectedSeverity] = useState<string>('ALL');

    if (!scan) {
        return (
            <div style={{ padding: '40px', textAlign: 'center', color: '#6B7280' }}>
                <Info size={48} style={{ margin: '0 auto 16px' }} />
                <p>No scan data available</p>
            </div>
        );
    }

    const vulnerabilities = parseScanResults(scan);
    const stats = getSeverityStats(vulnerabilities);
    const groupedVulns = groupBySeverity(vulnerabilities);

    const filteredVulns = selectedSeverity === 'ALL'
        ? vulnerabilities
        : groupedVulns[selectedSeverity] || [];

    const getSeverityIcon = (severity: string) => {
        switch (severity?.toUpperCase()) {
            case 'CRITICAL':
            case 'HIGH':
                return <XCircle size={20} />;
            case 'MEDIUM':
                return <AlertTriangle size={20} />;
            case 'LOW':
            case 'INFO':
                return <Info size={20} />;
            default:
                return <CheckCircle size={20} />;
        }
    };

    return (
        <div style={{ padding: '24px' }}>
            {/* Header */}
            <div style={{ marginBottom: '24px' }}>
                <h2 style={{ fontSize: '24px', fontWeight: 'bold', marginBottom: '8px' }}>
                    Scan Results
                </h2>
                <p style={{ color: '#6B7280' }}>
                    {scan.scanType} scan completed on {new Date(scan.completedAt || scan.createdAt).toLocaleString()}
                </p>
            </div>

            {/* Summary Stats */}
            <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))',
                gap: '16px',
                marginBottom: '24px'
            }}>
                <StatCard
                    label="Total"
                    value={stats.total}
                    color="#6B7280"
                    active={selectedSeverity === 'ALL'}
                    onClick={() => setSelectedSeverity('ALL')}
                />
                <StatCard
                    label="Critical"
                    value={stats.CRITICAL}
                    color={getSeverityColor('CRITICAL')}
                    active={selectedSeverity === 'CRITICAL'}
                    onClick={() => setSelectedSeverity('CRITICAL')}
                />
                <StatCard
                    label="High"
                    value={stats.HIGH}
                    color={getSeverityColor('HIGH')}
                    active={selectedSeverity === 'HIGH'}
                    onClick={() => setSelectedSeverity('HIGH')}
                />
                <StatCard
                    label="Medium"
                    value={stats.MEDIUM}
                    color={getSeverityColor('MEDIUM')}
                    active={selectedSeverity === 'MEDIUM'}
                    onClick={() => setSelectedSeverity('MEDIUM')}
                />
                <StatCard
                    label="Low"
                    value={stats.LOW}
                    color={getSeverityColor('LOW')}
                    active={selectedSeverity === 'LOW'}
                    onClick={() => setSelectedSeverity('LOW')}
                />
            </div>

            {/* Vulnerabilities List */}
            {filteredVulns.length === 0 ? (
                <div style={{
                    padding: '40px',
                    textAlign: 'center',
                    backgroundColor: '#F9FAFB',
                    borderRadius: '8px',
                    border: '1px solid #E5E7EB'
                }}>
                    <CheckCircle size={48} style={{ color: '#10B981', margin: '0 auto 16px' }} />
                    <p style={{ fontSize: '18px', fontWeight: '600', marginBottom: '8px' }}>
                        No vulnerabilities found
                    </p>
                    <p style={{ color: '#6B7280' }}>
                        {selectedSeverity === 'ALL'
                            ? 'This scan completed successfully with no issues detected.'
                            : `No ${selectedSeverity.toLowerCase()} severity issues found.`}
                    </p>
                </div>
            ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                    {filteredVulns.map((vuln) => (
                        <VulnerabilityCard
                            key={vuln.id}
                            vulnerability={vuln}
                            expanded={expandedVuln === vuln.id}
                            onToggle={() => setExpandedVuln(expandedVuln === vuln.id ? null : vuln.id)}
                        />
                    ))}
                </div>
            )}
        </div>
    );
};

// Stat Card Component
interface StatCardProps {
    label: string;
    value: number;
    color: string;
    active: boolean;
    onClick: () => void;
}

const StatCard: React.FC<StatCardProps> = ({ label, value, color, active, onClick }) => (
    <div
        onClick={onClick}
        style={{
            padding: '16px',
            borderRadius: '8px',
            border: `2px solid ${active ? color : '#E5E7EB'}`,
            backgroundColor: active ? `${color}10` : '#FFFFFF',
            cursor: 'pointer',
            transition: 'all 0.2s',
        }}
    >
        <div style={{ fontSize: '28px', fontWeight: 'bold', color, marginBottom: '4px' }}>
            {value}
        </div>
        <div style={{ fontSize: '14px', color: '#6B7280' }}>
            {label}
        </div>
    </div>
);

// Vulnerability Card Component
interface VulnerabilityCardProps {
    vulnerability: UnifiedVulnerability;
    expanded: boolean;
    onToggle: () => void;
}

const VulnerabilityCard: React.FC<VulnerabilityCardProps> = ({ vulnerability, expanded, onToggle }) => {
    const severityColor = getSeverityColor(vulnerability.severity);
    const severityBgColor = getSeverityBgColor(vulnerability.severity);

    return (
        <div style={{
            border: '1px solid #E5E7EB',
            borderRadius: '8px',
            overflow: 'hidden',
            backgroundColor: '#FFFFFF',
        }}>
            {/* Header */}
            <div
                onClick={onToggle}
                style={{
                    padding: '16px',
                    cursor: 'pointer',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '12px',
                    backgroundColor: expanded ? '#F9FAFB' : '#FFFFFF',
                    transition: 'background-color 0.2s',
                }}
            >
                {expanded ? <ChevronDown size={20} /> : <ChevronRight size={20} />}

                <div style={{
                    padding: '4px 12px',
                    borderRadius: '12px',
                    backgroundColor: severityBgColor,
                    color: severityColor,
                    fontSize: '12px',
                    fontWeight: '600',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '4px',
                }}>
                    {React.createElement(
                        vulnerability.severity === 'CRITICAL' || vulnerability.severity === 'HIGH' ? XCircle :
                            vulnerability.severity === 'MEDIUM' ? AlertTriangle : Info,
                        { size: 14 }
                    )}
                    {vulnerability.severity}
                </div>

                <div style={{ flex: 1 }}>
                    <div style={{ fontWeight: '600', marginBottom: '4px' }}>
                        {vulnerability.title}
                    </div>
                    {vulnerability.file && (
                        <div style={{ fontSize: '14px', color: '#6B7280', display: 'flex', alignItems: 'center', gap: '4px' }}>
                            <FileText size={14} />
                            {vulnerability.file}
                            {vulnerability.line && `:${vulnerability.line}`}
                        </div>
                    )}
                    {vulnerability.package && (
                        <div style={{ fontSize: '14px', color: '#6B7280', display: 'flex', alignItems: 'center', gap: '4px' }}>
                            <Package size={14} />
                            {vulnerability.package} {vulnerability.version && `(${vulnerability.version})`}
                        </div>
                    )}
                    {vulnerability.url && (
                        <div style={{ fontSize: '14px', color: '#6B7280', display: 'flex', alignItems: 'center', gap: '4px' }}>
                            <Globe size={14} />
                            {vulnerability.url}
                        </div>
                    )}
                </div>
            </div>

            {/* Expanded Details */}
            {expanded && (
                <div style={{ padding: '16px', borderTop: '1px solid #E5E7EB', backgroundColor: '#F9FAFB' }}>
                    {/* Description */}
                    <div style={{ marginBottom: '16px' }}>
                        <h4 style={{ fontSize: '14px', fontWeight: '600', marginBottom: '8px' }}>
                            Description
                        </h4>
                        <p style={{ fontSize: '14px', color: '#374151', lineHeight: '1.6' }}>
                            {vulnerability.description}
                        </p>
                    </div>

                    {/* Code Snippet */}
                    {vulnerability.code && (
                        <div style={{ marginBottom: '16px' }}>
                            <h4 style={{ fontSize: '14px', fontWeight: '600', marginBottom: '8px', display: 'flex', alignItems: 'center', gap: '4px' }}>
                                <Code size={16} />
                                Code
                            </h4>
                            <pre style={{
                                backgroundColor: '#1F2937',
                                color: '#F3F4F6',
                                padding: '12px',
                                borderRadius: '4px',
                                fontSize: '13px',
                                overflow: 'auto',
                                fontFamily: 'monospace',
                            }}>
                                {vulnerability.code}
                            </pre>
                        </div>
                    )}

                    {/* Solution */}
                    {vulnerability.solution && (
                        <div style={{ marginBottom: '16px' }}>
                            <h4 style={{ fontSize: '14px', fontWeight: '600', marginBottom: '8px' }}>
                                Remediation
                            </h4>
                            <p style={{
                                fontSize: '14px',
                                color: '#059669',
                                backgroundColor: '#D1FAE5',
                                padding: '12px',
                                borderRadius: '4px',
                                lineHeight: '1.6',
                            }}>
                                {vulnerability.solution}
                            </p>
                        </div>
                    )}

                    {/* Metadata */}
                    <div style={{ display: 'flex', gap: '16px', flexWrap: 'wrap', fontSize: '13px', color: '#6B7280' }}>
                        {vulnerability.cve && (
                            <div>
                                <strong>CVE:</strong> {vulnerability.cve}
                            </div>
                        )}
                        {vulnerability.cwe && (
                            <div>
                                <strong>CWE:</strong> {vulnerability.cwe}
                            </div>
                        )}
                        {vulnerability.fixedVersion && (
                            <div>
                                <strong>Fixed in:</strong> {vulnerability.fixedVersion}
                            </div>
                        )}
                        {vulnerability.method && (
                            <div>
                                <strong>Method:</strong> {vulnerability.method}
                            </div>
                        )}
                        {vulnerability.param && (
                            <div>
                                <strong>Parameter:</strong> {vulnerability.param}
                            </div>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
};

export default ScanResultsViewer;
