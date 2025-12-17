import React from "react";
import Sidebar from "@/components/commons/Sidebar";
import { useTheme } from "@/utils/store/themeContext";
import {
  Folder,
  ShieldAlert,
  AlertTriangle,
  ShieldCheck,
  Search,
  Plus,
  Edit,
  BarChart3,
} from "lucide-react";

export default function ProjectsPage() {
  const { theme } = useTheme();

  const projects = [
    {
      id: "FC",
      name: "FinTech-Core-Payment",
      repo: "github/fintech-core",
      status: "ACTIVE",
      health: "FAILED",
      vulns: { critical: 14, high: 23, medium: 45 },
      lastScan: "2 hours ago",
      trigger: "Manual Scan",
    },
    {
      id: "CP",
      name: "Customer-Portal-Web",
      repo: "gitlab/cust-portal",
      status: "ACTIVE",
      health: "WARNING",
      vulns: { critical: 2, high: 15, medium: 8 },
      lastScan: "1 day ago",
      trigger: "Scheduled (Daily)",
    },
    {
      id: "AC",
      name: "Admin-Console-V2",
      repo: "github/admin-v2",
      status: "ACTIVE",
      health: "PASSED",
      vulns: { critical: 0, high: 5, medium: 12 },
      lastScan: "5 hours ago",
      trigger: "CI/CD Trigger",
    },
    {
      id: "IW",
      name: "Internal-Wiki-Tool",
      repo: "internal/wiki",
      status: "ARCHIVED",
      health: "INACTIVE",
      vulns: { critical: 0, high: 0, medium: 2 },
      lastScan: "30 days ago",
      trigger: "Manual Scan",
    },
    {
      id: "MA",
      name: "Mobile-App-API",
      repo: "github/mobile-api",
      status: "ACTIVE",
      health: "PASSED",
      vulns: { critical: 0, high: 1, medium: 10 },
      lastScan: "12 hours ago",
      trigger: "Scheduled (Daily)",
    },
  ];

  return (
    <div className="projects-layout">
      <Sidebar />

      <main className="projects-main">
        {/* HEADER */}
        <header className="projects-header">
          <h1 style={{ color: theme.colors.text.primary }}>Projects</h1>

          <div className="header-actions">
            <div
              className="search"
              style={{
                background: theme.colors.surface,
                borderColor: theme.colors.border,
              }}
            >
              <Search size={18} />
              <input
                placeholder="Search projects..."
                style={{ color: theme.colors.text.primary }}
              />
            </div>

            <button
              className="primary-btn"
              style={{
                background: theme.colors.primary,
                color: theme.colors.onPrimary,
              }}
            >
              <Plus size={18} />
              New Project
            </button>
          </div>
        </header>

        {/* STATS */}
        <section className="projects-overview">
          <StatCard icon={<Folder />} label="Total Projects" value="45" theme={theme} />
          <StatCard icon={<ShieldAlert />} label="Critical Risk" value="3" danger theme={theme} />
          <StatCard icon={<AlertTriangle />} label="High Risk" value="12" warning theme={theme} />
          <StatCard icon={<ShieldCheck />} label="Healthy" value="30" success theme={theme} />
        </section>

        {/* FILTERS */}
        <section
          className="filters"
          style={{
            background: theme.colors.surface,
            borderColor: theme.colors.border,
          }}
        >
          <div className="search">
            <Search size={16} />
            <input placeholder="Search by name..." />
          </div>

          <select>
            <option>All Status</option>
            <option>Active</option>
            <option>Archived</option>
          </select>

          <select>
            <option>Risk Level</option>
            <option>Critical</option>
            <option>High</option>
            <option>Medium</option>
            <option>Low</option>
          </select>
        </section>

        {/* TABLE */}
        <section
          className="table-wrapper"
          style={{
            background: theme.colors.surface,
            borderColor: theme.colors.border,
          }}
        >
          <table>
            <thead>
              <tr>
                <th>Project Name</th>
                <th>Status</th>
                <th>Security Health</th>
                <th>Vulnerabilities</th>
                <th>Last Scan</th>
                <th align="right">Action</th>
              </tr>
            </thead>

            <tbody>
              {projects.map((p) => (
                <tr key={p.name}>
                  <td>
                    <div className="project-info">
                      <div className="project-avatar">{p.id}</div>
                      <div>
                        <span className="name">{p.name}</span>
                        <span className="muted">Repository: {p.repo}</span>
                      </div>
                    </div>
                  </td>

                  <td>
                    <span className="tag">{p.status}</span>
                  </td>

                  <td>
                    <span className="tag">{p.health}</span>
                  </td>

                  <td>
                    <span className="risk-high">{p.vulns.critical}</span>{" "}
                    <span className="risk-medium">{p.vulns.high}</span>{" "}
                    <span className="risk-low">{p.vulns.medium}</span>
                  </td>

                  <td>
                    <span>{p.lastScan}</span>
                    <span className="muted">{p.trigger}</span>
                  </td>

                  <td align="right">
                    <button className="icon-btn">
                      <Edit size={16} />
                    </button>
                    <button className="icon-btn">
                      <BarChart3 size={16} />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {/* PAGINATION */}
          <footer className="pagination">
            <span>Showing 5 of 45 projects</span>
            <div>
              <button disabled>Previous</button>
              <button>Next</button>
            </div>
          </footer>
        </section>
      </main>
    </div>
  );
}

/* ---------------- COMPONENTS ---------------- */

function StatCard({ icon, label, value, danger, warning, success, theme }) {
  let color = theme.colors.text.primary;
  if (danger) color = theme.colors.danger;
  if (warning) color = theme.colors.warning;
  if (success) color = theme.colors.success;

  return (
    <div
      className="overview-card"
      style={{
        background: theme.colors.surface,
        borderColor: theme.colors.border,
      }}
    >
      <div>
        <p className="overview-title">{label}</p>
        <span className="overview-value" style={{ color }}>
          {value}
        </span>
      </div>
      {icon}
    </div>
  );
}
