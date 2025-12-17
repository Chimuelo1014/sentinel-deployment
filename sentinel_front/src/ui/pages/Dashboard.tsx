import React from "react";
import Sidebar from "@/components/commons/Sidebar";
import { useTheme } from "@/utils/store/themeContext";
import {
  ShieldCheck,
  LayoutDashboard,
  Radar,
  Folder,
  Bug,
  Bell,
  Settings,
  Activity,
  AlertTriangle,
  Layers,
  TrendingUp,
  Clock,
  PlayCircle,
  Flame,
  Search,
} from "lucide-react";

import {
  useDashboardSummary,
  useActiveScans,
  useVulnerabilityTrends,
  useRecentScans,
  useTopRiskProjects,
} from "@hooks/useDashboard";
import { i } from "framer-motion/client";

/* ========================================================= */
/* ====================== DASHBOARD ======================== */
/* ========================================================= */

export default function Dashboard() {
  const { theme } = useTheme();

  const { data: summary } = useDashboardSummary();
  const { data: activeScans } = useActiveScans();
  const { data: recentScans } = useRecentScans();
  const { data: riskProjects } = useTopRiskProjects();

  return (
    <div className="app" style={{ background: theme.colors.background }}>
      {/* ================= SIDEBAR ================= */}
      <Sidebar />

      {/* ================= MAIN ================= */}
      <main className="main">
        {/* HEADER */}
        <header
          className="header"
          style={{
            background: theme.colors.surface,
            borderBottom: `1px solid ${theme.colors.border}`,
          }}
        >
          <h1 style={{ color: theme.colors.text.primary }}>Overview</h1>

          <div style={{ position: "relative" }}>
            <Search
              size={16}
              style={{
                position: "absolute",
                top: "50%",
                left: 12,
                transform: "translateY(-50%)",
                color: theme.colors.text.tertiary,
              }}
            />
            <input
              placeholder="Search IPs, CVEs, or Projects..."
              style={{
                paddingLeft: 36,
                background: theme.colors.background,
                borderColor: theme.colors.border,
                color: theme.colors.text.primary,
              }}
            />
          </div>
        </header>

        {/* ================= CONTENT ================= */}
        <section className="content">
          {/* ================= KPIs ================= */}
          <div className="kpis">
            <Kpi
              title="Exposed Ports"
              value={summary?.exposedPorts ?? 0}
              icon={Activity}
              theme={theme}
            />

            <Kpi
              title="Critical Findings"
              value={summary?.criticalFindingsOpen ?? 0}
              icon={AlertTriangle}
              theme={theme}
            />

            <Kpi
              title="Quality Gate Pass"
              value={`${summary?.qualityGatePassRate ?? 0}%`}
              icon={ShieldCheck}
              theme={theme}
            />

            <Kpi
              title="Projects Monitored"
              value={summary?.projectsMonitored ?? 0}
              icon={Layers}
              theme={theme}
            />
          </div>

          {/* ================= GRID ================= */}
          <div className="dashboard-grid">
            {/* ============ LEFT ============ */}
            <div className="left">
              {/* Trends */}
              <section
                className="card"
                style={{
                  background: theme.colors.surface,
                  borderColor: theme.colors.border,
                }}
              >
                <div className="card-header">
                  <div>
                    <h3
                      style={{
                        display: "flex",
                        gap: 8,
                        color: theme.colors.text.primary,
                      }}
                    >
                      <TrendingUp size={18} />
                      Vulnerability Trends
                    </h3>
                    <p style={{ color: theme.colors.text.secondary }}>
                      Last 30 Days Overview
                    </p>
                  </div>
                </div>

                <div
                  className="chart"
                  style={{
                    height: 180,
                    background: theme.colors.background,
                    borderRadius: 12,
                    display: "grid",
                    placeItems: "center",
                    color: theme.colors.text.tertiary,
                  }}
                >
                  Chart Placeholder
                </div>
              </section>

              {/* Recent Scans */}
              <section
                className="card"
                style={{
                  background: theme.colors.surface,
                  borderColor: theme.colors.border,
                }}
              >
                <div className="card-header">
                  <h3
                    style={{
                      display: "flex",
                      gap: 8,
                      color: theme.colors.text.primary,
                    }}
                  >
                    <Clock size={18} />
                    Recent Scans
                  </h3>
                </div>

                <table>
                  <thead>
                    <tr>
                      {["Project", "Type", "Status", "Quality Gate", ""].map(
                        (h) => (
                          <th
                            key={h}
                            style={{ color: theme.colors.text.secondary }}
                          >
                            {h}
                          </th>
                        )
                      )}
                    </tr>
                  </thead>

                  <tbody>
                    {(recentScans ?? []).map((scan) => (
                      <tr key={scan.scanId}>
                        <td style={{ color: theme.colors.text.primary }}>
                          {scan.projectName}
                        </td>
                        <td>
                          <span className="tag">{scan.scanType}</span>
                        </td>
                        <td style={{ color: theme.colors.text.secondary }}>
                          {scan.status}
                        </td>
                        <td>
                          <span
                            className="badge"
                            style={{
                              background: scan.qualityGatePassed
                                ? theme.colors.success
                                : theme.colors.danger,
                              color: theme.colors.onPrimary,
                            }}
                          >
                            {scan.qualityGatePassed ? "PASS" : "FAIL"}
                          </span>
                        </td>
                        <td>
                          <button className="btn-sm">View</button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </section>
            </div>

            {/* ============ RIGHT ============ */}
            <div className="right">
              {/* Active Scans */}
              <section
                className="card"
                style={{
                  background: theme.colors.surface,
                  borderColor: theme.colors.border,
                }}
              >
                <h3
                  style={{
                    display: "flex",
                    gap: 8,
                    color: theme.colors.text.primary,
                  }}
                >
                  <PlayCircle size={18} />
                  Active Scans
                </h3>

                <ul className="scans">
                  {(activeScans ?? []).map((scan) => (
                    <li key={scan.scanId}>
                      <div
                        style={{
                          display: "flex",
                          flexDirection: "column",
                          gap: 4,
                        }}
                      >
                        <strong style={{ color: theme.colors.text.primary }}>
                          {scan.projectName}
                        </strong>

                        <span className="tag" style={{ alignSelf: "flex-start" }}>
                          {scan.scanType}
                        </span>

                        <p style={{ color: theme.colors.text.secondary }}>
                          {scan.statusMessage ?? "Scanning..."}
                        </p>
                      </div>

                      <span style={{ color: theme.colors.text.tertiary }}>
                        {scan.elapsedTime ?? "--:--"}
                      </span>
                    </li>
                  ))}
                </ul>

                <button
                  className="btn-outline"
                  style={{
                    borderColor: theme.colors.primary,
                    color: theme.colors.primary,
                  }}
                >
                  + Start New Scan
                </button>
              </section>

              {/* Top Risk Projects */}
              <section
                className="card"
                style={{
                  background: theme.colors.surface,
                  borderColor: theme.colors.border,
                }}
              >
                <h3
                  style={{
                    display: "flex",
                    gap: 8,
                    color: theme.colors.text.primary,
                  }}
                >
                  <Flame size={18} />
                  Top Risk Projects
                </h3>

                <ul className="risk">
                  {(riskProjects ?? []).map((project) => {
                    const isSafe =
                      project.criticalFindings === 0 &&
                      project.highFindings === 0;

                    return (
                      <li key={project.projectName}>
                        <div
                          style={{
                            display: "flex",
                            alignItems: "center",
                            gap: 8,
                          }}
                        >
                          {isSafe ? (
                            <ShieldCheck
                              size={16}
                              color={theme.colors.success}
                            />
                          ) : project.criticalFindings > 0 ? (
                            <Flame size={16} color={theme.colors.danger} />
                          ) : (
                            <AlertTriangle
                              size={16}
                              color={theme.colors.warning}
                            />
                          )}

                          <strong style={{ color: theme.colors.text.primary }}>
                            {project.projectName}
                          </strong>
                        </div>

                        <div style={{ marginTop: 6 }}>
                          {isSafe ? (
                            <div style={{ color: theme.colors.success }}>
                              Safe
                            </div>
                          ) : (
                            <>
                              <div style={{ color: theme.colors.danger }}>
                                {project.criticalFindings} Critical
                              </div>
                              <div style={{ color: theme.colors.warning }}>
                                {project.highFindings} High
                              </div>
                            </>
                          )}
                        </div>
                      </li>
                    );
                  })}
                </ul>
              </section>
            </div>
          </div>
        </section>
      </main>
    </div>
  );
}

/* ========================================================= */
/* ======================== KPI ============================ */
/* ========================================================= */

function Kpi({
  title,
  value,
  icon: Icon,
  danger = false,
  theme,
}: {
  title: string;
  value: string | number;
  icon: any;
  danger?: boolean;
  theme: any;
}) {
  return (
    <div
      className="kpi"
      style={{
        background: theme.colors.surface,
        borderColor: danger
          ? theme.colors.danger
          : theme.colors.border,
        display: "flex",
        gap: 16,
        alignItems: "center",
      }}
    >
      <Icon
        size={28}
        color={danger ? theme.colors.danger : theme.colors.primary}
      />
      <div>
        <p style={{ color: theme.colors.text.secondary }}>{title}</p>
        <h3
          style={{
            color: danger
              ? theme.colors.danger
              : theme.colors.text.primary,
          }}
        >
          {value}
        </h3>
      </div>
    </div>
  );
}