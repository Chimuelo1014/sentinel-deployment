import React, { useState, RefAttributes, ForwardRefExoticComponent } from "react";
import * as LucideIcons from "lucide-react";
import { ChevronLeft, ChevronRight, MoreVertical, LucideProps } from "lucide-react";

// --- 1. Definición de Tipos/Interfaces ---
interface FindingsTableProps {
    theme: any; 
}

// Interfaces de datos (se mantienen)
interface Finding {
    id: string;
    cve: string | null;
    description: string;
    severity: "Critical" | "High" | "Medium" | "Low";
    project: string;
    location: string;
    status: "Open" | "In Review" | "Fixed";
    discovered: string;
    isFixed: boolean;
}
interface SeverityMapItem {
    icon: ForwardRefExoticComponent<Omit<LucideProps, "ref"> & RefAttributes<SVGSVGElement>>;
    colorClass: string;
}
interface SelectedFindings {
    [id: string]: boolean;
}

// --- 2. Datos y Mapeos Tipados (Se mantienen) ---
const severityMap: Record<Finding['severity'], SeverityMapItem> = {
  Critical: { icon: LucideIcons.ShieldAlert, colorClass: "critical" },
  High: { icon: LucideIcons.AlertTriangle, colorClass: "high" },
  Medium: { icon: LucideIcons.CircleAlert, colorClass: "medium" },
  Low: { icon: LucideIcons.Info, colorClass: "low" },
};
const statusMap: Record<Finding['status'], string> = {
  Open: "open",
  "In Review": "in-review",
  Fixed: "fixed",
};

const findingsData: Finding[] = [
  { id: "VULN-2394", cve: "CVE-2023-4401", description: "SQL Injection in Login Module", severity: "Critical", project: "Payment Gateway API", location: "/src/auth/login.ts:42", status: "Open", discovered: "2 hrs ago", isFixed: false },
  { id: "VULN-2392", cve: null, description: "Cross-Site Scripting (Reflected)", severity: "High", project: "Customer Portal", location: "/views/profile/edit.js:108", status: "In Review", discovered: "Yesterday", isFixed: false },
  { id: "CONF-104", cve: null, description: "Missing Security Headers", severity: "Medium", project: "Frontend Assets", location: "nginx.conf", status: "Open", discovered: "Oct 24, 2023", isFixed: false },
  { id: "INFO-002", cve: null, description: "Verbose Error Messages", severity: "Low", project: "Legacy API", location: "/api/v1/debug", status: "Fixed", discovered: "Oct 22, 2023", isFixed: true },
  { id: "VULN-2350", cve: null, description: "Hardcoded AWS Credentials", severity: "Critical", project: "Worker Service", location: "config.py", status: "Fixed", discovered: "Oct 20, 2023", isFixed: true },
];


export default function FindingsTable({ theme }: FindingsTableProps) {
  
  // VERIFICACIÓN DE SEGURIDAD
  if (!theme || !theme.colors) {
      console.warn("Theme or theme.colors is undefined in FindingsTable. Rendering fallback.");
      return null;
  }

  // ALIAS CORRECTO: Accede directamente a theme.colors
  const c = theme.colors; 
  
  const [selected, setSelected] = useState<SelectedFindings>({});

  const toggleSelect = (id: string) => {
    setSelected((prev) => ({ ...prev, [id]: !prev[id] }));
  };

  const isAllSelected = findingsData.every((f) => selected[f.id]);

  const handleSelectAll = () => {
    if (isAllSelected) {
      setSelected({});
    } else {
      const allSelected = findingsData.reduce((acc: SelectedFindings, f) => {
        acc[f.id] = true;
        return acc;
      }, {});
      setSelected(allSelected);
    }
  };
  
  // --- Estilos de la tabla ---
  const tableContainerStyle = {
    background: c.surface,
    borderColor: c.border,
    color: c.text.primary,
  };

  const tableHeaderStyle = {
    color: c.text.secondary,
    borderBottom: `1px solid ${c.border}`,
    background: c.surfaceLight, 
  };

  const paginationStyle = {
    borderTop: `1px solid ${c.border}`,
    color: c.text.secondary,
    background: c.surface,
  };

  return (
    <div className="table-card card" style={tableContainerStyle}>
      <div className="table-scroll-wrapper">
        <table className="findings-table">
          <thead>
            <tr style={tableHeaderStyle}>
              <th className="checkbox-col">
                <input
                  type="checkbox"
                  checked={isAllSelected && findingsData.length > 0}
                  onChange={handleSelectAll}
                />
              </th>
              <th style={{ color: c.text.secondary }}>Severity</th>
              <th style={{ color: c.text.secondary }}>Finding / Description</th>
              <th style={{ color: c.text.secondary }}>Project / Location</th>
              <th className="status-col" style={{ color: c.text.secondary }}>Status</th>
              <th style={{ color: c.text.secondary }}>Discovered</th>
              <th className="actions-col" style={{ color: c.text.secondary }}></th>
            </tr>
          </thead>
          <tbody>
            {findingsData.map((finding) => {
              const { icon: SeverityIcon, colorClass: severityColor } =
                severityMap[finding.severity];
              const statusColor = statusMap[finding.status];
              const isChecked = selected[finding.id] || false;

              const rowStyle = {
                color: finding.isFixed ? c.text.tertiary : c.text.primary,
                borderBottom: `1px solid ${c.border}`,
              };


              return (
                <tr
                  key={finding.id}
                  className={`table-row ${finding.isFixed ? "is-fixed" : ""}`}
                  style={rowStyle}
                >
                  <td className="checkbox-col">
                    <input
                      type="checkbox"
                      checked={isChecked}
                      onChange={() => toggleSelect(finding.id)}
                      style={{ borderColor: c.border }}
                    />
                  </td>
                  <td>
                    <div className={`severity-tag ${severityColor}`}>
                      <SeverityIcon size={18} />
                      {finding.severity}
                    </div>
                  </td>
                  <td>
                    <div className="finding-details">
                      <span className={`description ${finding.isFixed ? 'line-through' : ''}`}>
                        {finding.description}
                      </span>
                      <span className="code-text" style={{ color: c.text.secondary }}>
                        {finding.id} {finding.cve ? `• ${finding.cve}` : ""}
                      </span>
                    </div>
                  </td>
                  <td>
                    <div className="location-details">
                      <span className="project-name">{finding.project}</span>
                      <span className="code-text truncate-text" style={{ color: c.text.secondary }}>
                        {finding.location}
                      </span>
                    </div>
                  </td>
                  <td className="status-col">
                    <span className={`status-badge ${statusColor}`}>
                      {finding.status}
                    </span>
                  </td>
                  <td className="discovered-col" style={{ color: c.text.secondary }}>
                      {finding.discovered}
                  </td>
                  <td className="actions-col">
                    <button className="icon-action" style={{ color: c.text.tertiary }}>
                      <MoreVertical size={20} />
                    </button>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      {/* Pagination */}
      <footer className="pagination" style={paginationStyle}>
        <p>
          Showing <span style={{ color: c.text.primary }}>1-5</span> of <span style={{ color: c.text.primary }}>1,205</span> results
        </p>
        <div>
          <button 
            className="pagination-btn" 
            disabled 
            style={{ 
                color: c.text.tertiary,
                borderColor: c.border
            }}
          >
            <ChevronLeft size={18} /> Previous
          </button>
          <button 
            className="pagination-btn"
            style={{ 
                color: c.text.secondary,
                borderColor: c.border
            }}
          >
            Next <ChevronRight size={18} />
          </button>
        </div>
      </footer>
    </div>
  );
}