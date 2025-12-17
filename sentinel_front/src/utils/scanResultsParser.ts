import {
    ScanResult,
    SASTResults,
    DASTResults,
    ContainerResults,
    UnifiedVulnerability,
    SemgrepFinding,
    ZAPAlert,
    TrivyVulnerability
} from '../types/scanResults';

/**
 * Parse scan results and convert to unified vulnerability format
 */
export function parseScanResults(scan: any): UnifiedVulnerability[] {
    if (!scan || !scan.results) {
        return [];
    }

    const scanType = scan.scanType?.toUpperCase();
    const results = typeof scan.results === 'string'
        ? JSON.parse(scan.results)
        : scan.results;

    switch (scanType) {
        case 'SAST':
            return parseSASTResults(results);
        case 'DAST':
            return parseDASTResults(results);
        case 'CONTAINER':
            return parseContainerResults(results);
        default:
            console.warn('Unknown scan type:', scanType);
            return [];
    }
}

/**
 * Parse SAST (Semgrep) results
 */
function parseSASTResults(results: SASTResults): UnifiedVulnerability[] {
    if (!results.results || !Array.isArray(results.results)) {
        return [];
    }

    return results.results.map((finding: SemgrepFinding, index: number) => ({
        id: `sast-${index}`,
        title: finding.check_id || 'Unknown Issue',
        severity: mapSemgrepSeverity(finding.extra.severity),
        description: finding.extra.message || 'No description available',
        file: finding.path,
        line: finding.start.line,
        solution: finding.extra.metadata?.subcategory?.join(', ') || 'Review and fix the code',
        cwe: finding.extra.metadata?.category,
        code: finding.extra.lines,
    }));
}

/**
 * Parse DAST (OWASP ZAP) results
 */
function parseDASTResults(results: DASTResults): UnifiedVulnerability[] {
    if (!results.Site || !Array.isArray(results.Site)) {
        return [];
    }

    const vulnerabilities: UnifiedVulnerability[] = [];

    results.Site.forEach((site) => {
        if (!site.Alerts) return;

        site.Alerts.forEach((alert: ZAPAlert, index: number) => {
            const instance = alert.Instances?.[0];

            vulnerabilities.push({
                id: `dast-${index}`,
                title: alert.Alert,
                severity: mapZAPRisk(alert.Risk),
                description: alert.Description,
                solution: alert.Solution,
                url: instance?.Uri,
                method: instance?.Method,
                param: instance?.Param,
            });
        });
    });

    return vulnerabilities;
}

/**
 * Parse Container (Trivy) results
 */
function parseContainerResults(results: ContainerResults): UnifiedVulnerability[] {
    if (!results.Results || !Array.isArray(results.Results)) {
        return [];
    }

    const vulnerabilities: UnifiedVulnerability[] = [];

    results.Results.forEach((result) => {
        if (!result.Vulnerabilities) return;

        result.Vulnerabilities.forEach((vuln: TrivyVulnerability, index: number) => {
            vulnerabilities.push({
                id: `container-${index}`,
                title: vuln.Title || vuln.VulnerabilityID,
                severity: vuln.Severity as any,
                description: vuln.Description,
                cve: vuln.VulnerabilityID,
                package: vuln.PkgName,
                version: vuln.InstalledVersion,
                fixedVersion: vuln.FixedVersion,
                solution: vuln.FixedVersion
                    ? `Upgrade ${vuln.PkgName} to version ${vuln.FixedVersion}`
                    : 'No fix available yet',
            });
        });
    });

    return vulnerabilities;
}

/**
 * Map Semgrep severity to unified severity
 */
function mapSemgrepSeverity(severity: string): UnifiedVulnerability['severity'] {
    switch (severity?.toUpperCase()) {
        case 'ERROR':
            return 'HIGH';
        case 'WARNING':
            return 'MEDIUM';
        case 'INFO':
            return 'INFO';
        default:
            return 'LOW';
    }
}

/**
 * Map ZAP risk to unified severity
 */
function mapZAPRisk(risk: string): UnifiedVulnerability['severity'] {
    switch (risk?.toUpperCase()) {
        case 'HIGH':
            return 'HIGH';
        case 'MEDIUM':
            return 'MEDIUM';
        case 'LOW':
            return 'LOW';
        case 'INFORMATIONAL':
            return 'INFO';
        default:
            return 'LOW';
    }
}

/**
 * Get severity color for UI
 */
export function getSeverityColor(severity: string): string {
    switch (severity?.toUpperCase()) {
        case 'CRITICAL':
            return '#DC2626'; // red-600
        case 'HIGH':
            return '#EA580C'; // orange-600
        case 'MEDIUM':
            return '#F59E0B'; // amber-500
        case 'LOW':
            return '#3B82F6'; // blue-500
        case 'INFO':
            return '#6B7280'; // gray-500
        default:
            return '#9CA3AF'; // gray-400
    }
}

/**
 * Get severity badge background color
 */
export function getSeverityBgColor(severity: string): string {
    switch (severity?.toUpperCase()) {
        case 'CRITICAL':
            return '#FEE2E2'; // red-100
        case 'HIGH':
            return '#FFEDD5'; // orange-100
        case 'MEDIUM':
            return '#FEF3C7'; // amber-100
        case 'LOW':
            return '#DBEAFE'; // blue-100
        case 'INFO':
            return '#F3F4F6'; // gray-100
        default:
            return '#F9FAFB'; // gray-50
    }
}

/**
 * Group vulnerabilities by severity
 */
export function groupBySeverity(vulnerabilities: UnifiedVulnerability[]): Record<string, UnifiedVulnerability[]> {
    return vulnerabilities.reduce((acc, vuln) => {
        const severity = vuln.severity || 'UNKNOWN';
        if (!acc[severity]) {
            acc[severity] = [];
        }
        acc[severity].push(vuln);
        return acc;
    }, {} as Record<string, UnifiedVulnerability[]>);
}

/**
 * Calculate severity statistics
 */
export function getSeverityStats(vulnerabilities: UnifiedVulnerability[]) {
    const stats = {
        CRITICAL: 0,
        HIGH: 0,
        MEDIUM: 0,
        LOW: 0,
        INFO: 0,
        total: vulnerabilities.length,
    };

    vulnerabilities.forEach((vuln) => {
        const severity = vuln.severity?.toUpperCase();
        if (severity && severity in stats) {
            stats[severity as keyof typeof stats]++;
        }
    });

    return stats;
}
