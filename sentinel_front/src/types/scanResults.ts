// Scan Results Types
export interface ScanResult {
    id: string;
    scanId: string;
    status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'FAILED';
    summary: string;
    results: SASTResults | DASTResults | ContainerResults | null;
    createdAt: string;
    completedAt?: string;
}

// SAST (Semgrep) Results
export interface SASTResults {
    results?: SemgrepFinding[];
    errors?: any[];
}

export interface SemgrepFinding {
    check_id: string;
    path: string;
    start: { line: number; col: number };
    end: { line: number; col: number };
    extra: {
        message: string;
        metadata: {
            category?: string;
            confidence?: string;
            impact?: string;
            likelihood?: string;
            subcategory?: string[];
            technology?: string[];
        };
        severity: 'ERROR' | 'WARNING' | 'INFO';
        lines: string;
    };
}

// DAST (OWASP ZAP) Results
export interface DASTResults {
    Site?: Array<{
        Alerts?: ZAPAlert[];
    }>;
}

export interface ZAPAlert {
    Alert: string;
    Risk: 'High' | 'Medium' | 'Low' | 'Informational';
    Confidence: 'High' | 'Medium' | 'Low';
    Description: string;
    Solution: string;
    Instances?: Array<{
        Uri: string;
        Method: string;
        Param?: string;
    }>;
}

// Container (Trivy) Results
export interface ContainerResults {
    Results?: Array<{
        Target: string;
        Class?: string;
        Type?: string;
        Vulnerabilities?: TrivyVulnerability[];
    }>;
}

export interface TrivyVulnerability {
    VulnerabilityID: string;
    PkgName: string;
    InstalledVersion: string;
    FixedVersion?: string;
    Severity: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW' | 'UNKNOWN';
    Title: string;
    Description: string;
    References?: string[];
}

// Unified Vulnerability for Display
export interface UnifiedVulnerability {
    id: string;
    title: string;
    severity: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW' | 'INFO';
    description: string;
    file?: string;
    line?: number;
    solution?: string;
    cwe?: string;
    cve?: string;
    package?: string;
    version?: string;
    fixedVersion?: string;
    url?: string;
    method?: string;
    param?: string;
    code?: string;
}
