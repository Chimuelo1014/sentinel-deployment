import React, { useState } from "react";
import Sidebar from "@/components/commons/Sidebar";
import { useTheme } from "@/utils/store/themeContext";
import {
  Bell,
  CirclePlus,
  Info,
  Search,
  Calendar,
  ListFilter,
  CircleX,
  RotateCcw,
  CheckCircle,
  CircleAlert,
  Gauge,
  ChevronLeft,
  ChevronRight,
} from "lucide-react";

export default function ScansPage() {
  const { theme } = useTheme();
  const [search, setSearch] = useState("");
const scans = [
  {
    id: "#SCN-9942",
    project: "Web-Client-Front",
    type: "SAST",
    time: "Today, 10:42 AM",
    duration: "Running for 12m",
    status: "RUNNING",
    progress: 65,
  },
  {
    id: "#SCN-9941",
    project: "Payment-Gateway-SVC",
    type: "DAST",
    time: "Oct 24, 09:15 AM",
    duration: "Duration: 45m 10s",
    status: "COMPLETED",
    findings: { critical: 5, high: 12, medium: 24 },
  },
  {
    id: "#SCN-9938",
    project: "Auth-Service-V2",
    type: "SCA",
    time: "Oct 23, 04:30 PM",
    duration: "Duration: 2m 45s",
    status: "COMPLETED",
    findings: { critical: 0, high: 2, medium: 8 },
  },
  {
    id: "#SCN-9935",
    project: "Legacy-DB-Connector",
    type: "CONTAINER",
    time: "Oct 23, 02:00 PM",
    duration: "Duration: 0s",
    status: "FAILED",
    error: "Connection Timeout",
  },
];


  return (
    <div className="scans-layout">
      <Sidebar activeItem="scans" />

      <main
        className="scans-main"
        style={{ background: theme.colors.surface }}
      >
        {/* HEADER */}
        <header
          className="scans-header"
          style={{
            background: theme.colors.background,
            borderColor: theme.colors.border,
          }}
        >
          <h1 style={{ color: theme.colors.text.primary }}>
            Scan Management
          </h1>

          <div className="header-actions">
            <button
              className="primary-btn"
              style={{
                background: theme.colors.primary,
                color: theme.colors.onPrimary,
              }}
            >
              <CirclePlus size={18} />
              New Scan
            </button>

            <button className="icon-btn">
              <Bell size={18} />
            </button>

            <button className="icon-btn">
              <Info size={18} />
            </button>
          </div>
        </header>
              
              <div className="scan-overview">
  {/* Scan Volume Chart */}
  <div className="scan-volume-card">
    <div className="scan-volume-header">
      <div>
        <h3>Scan Volume</h3>
        <span>Scans initiated over last 7 days</span>
      </div>

      <div className="legend">
        <span>
          <i className="dot sast" /> SAST
        </span>
        <span>
          <i className="dot dast" /> DAST
        </span>
      </div>
    </div>

    <div className="chart">
      {[
        { day: "Mon", sast: 6, dast: 4 },
        { day: "Tue", sast: 8, dast: 3 },
        { day: "Wed", sast: 4, dast: 2 },
        { day: "Thu", sast: 9, dast: 5 },
        { day: "Fri", sast: 7, dast: 4 },
        { day: "Sat", sast: 3, dast: 1 },
        { day: "Sun", sast: 2, dast: 1 },
      ].map((d) => (
        <div key={d.day} className="bar-group">
          <div className="bar">
            <span className="bar-sast" style={{ height: `${d.sast * 10}px` }} />
            <span className="bar-dast" style={{ height: `${d.dast * 10}px` }} />
          </div>
          <span className="bar-label">{d.day}</span>
        </div>
      ))}
    </div>
  </div>

  {/* Side Stats */}
  <div className="scan-stats">
    <div className="stat-card">
      <span className="stat-title">Scans Today</span>
      <strong className="stat-value">28</strong>
      <span className="stat-sub">Active</span>
      <span className="stat-badge success">+4</span>
    </div>

    <div className="stat-card">
      <span className="stat-title">Avg. Duration</span>
      <strong className="stat-value">14m 02s</strong>
      <span className="stat-badge warning">-12s</span>
    </div>

    <div className="stat-card">
      <span className="stat-title">Failed</span>
      <strong className="stat-value danger">3</strong>
      <span className="stat-sub">Requires Review</span>
      <span className="stat-badge danger">Alert</span>
    </div>
  </div>
</div>

        {/* FILTERS */}
        <section
          className="filters"
          style={{
            background: theme.colors.background,
            borderColor: theme.colors.border,
          }}
        >
          <div className="search">
            <Search size={18} />
            <input
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search scan or project"
              style={{
                background: theme.colors.background,
                color: theme.colors.text.primary,
              }}
            />
          </div>

          <button className="filter-btn">
            <Calendar size={16} /> Last 30 days
          </button>

          <button className="filter-btn">
            <ListFilter size={16} /> More
          </button>
        </section>

  {/* TABLE */}
<section
  className="table-wrapper"
  style={{
    background: theme.colors.background,
    borderColor: theme.colors.border,
  }}
>
  <table>
    <thead>
      <tr>
        <th>Scan Details</th>
        <th>Type</th>
        <th>Timeline</th>
        <th>Status</th>
        <th>Findings Summary</th>
        <th align="right">Actions</th>
      </tr>
    </thead>

    <tbody>
      {scans.map((scan) => (
        <tr key={scan.id}>
          {/* SCAN DETAILS */}
          <td>
            <strong>{scan.project}</strong>
            <span className="muted">ID: {scan.id}</span>
          </td>

          {/* TYPE */}
          <td>
            <span className={`tag tag-${scan.type.toLowerCase()}`}>
              {scan.type}
            </span>
          </td>

          {/* TIMELINE */}
          <td>
            <span>{scan.time}</span>
            <span className="muted">{scan.duration}</span>
          </td>

          {/* STATUS */}
          <td>
            {scan.status === "RUNNING" && (
              <div className="status running">
                <span className="status-dot" />
                RUNNING
                <div className="progress">
                  <span style={{ width: `${scan.progress}%` }} />
                </div>
              </div>
            )}

            {scan.status === "COMPLETED" && (
              <div className="status success">
                <CheckCircle size={14} />
                COMPLETED
              </div>
            )}

            {scan.status === "FAILED" && (
              <div className="status danger">
                <CircleAlert size={14} />
                FAILED
              </div>
            )}
          </td>

          {/* FINDINGS */}
          <td>
            {scan.status === "RUNNING" && (
              <span className="muted">Analysis in progress...</span>
            )}

            {scan.status === "COMPLETED" && (
              <div className="findings">
                <span className="dot critical" /> {scan.findings.critical}
                <span className="dot high" /> {scan.findings.high}
                <span className="dot medium" /> {scan.findings.medium}
              </div>
            )}

            {scan.status === "FAILED" && (
              <span className="error-text">{scan.error}</span>
            )}
          </td>

          {/* ACTIONS */}
          <td align="right">
            {scan.status === "RUNNING" && (
              <button className="icon-action danger">
                <CircleX size={18} />
              </button>
            )}

            {scan.status === "FAILED" && (
              <button className="retry">
                <RotateCcw size={14} /> Retry
              </button>
            )}

            {scan.status === "COMPLETED" && (
              <button className="view">
                View Report
              </button>
            )}
          </td>
        </tr>
      ))}
    </tbody>
  </table>

  {/* PAGINATION */}
  <footer className="pagination">
    <span>Showing 1–4 of 248 scans</span>
    <div>
      <button><ChevronLeft size={18} /></button>
      <button><ChevronRight size={18} /></button>
    </div>
  </footer>
</section>

              
      </main>
    </div>
  );
}
