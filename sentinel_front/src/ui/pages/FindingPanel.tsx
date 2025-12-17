import React, { useState } from "react";
import Sidebar from "@/components/commons/Sidebar";
import StatCard from "@/components/StatCard"; 
import FindingsTable from "@/components/FindingsTable";
import { useTheme } from "@/utils/store/themeContext";
import {
  Search,
  Bell,
  Download,
  Plus,
  Filter,
  ChevronDown,
} from "lucide-react";

// Datos de ejemplo para las estadísticas (se mantienen)
const statsData = [
  {
    title: "Critical Findings",
    value: "12",
    icon: "ShieldAlert",
    iconColor: "#EF4444", 
    change: "+2",
    changeContext: "since yesterday",
    changeType: "up", 
  },
  {
    title: "High Severity",
    value: "45",
    icon: "AlertTriangle", 
    iconColor: "#F59E0B", 
    change: "No change",
    changeType: "no-change",
  },
  {
    title: "Total Open",
    value: "1,205",
    icon: "Target", 
    iconColor: "#FF1B6D", 
    change: "-5%",
    changeContext: "vs last week",
    changeType: "down",
  },
  {
    title: "MTTR",
    value: "4.5d",
    icon: "Clock", 
    iconColor: "#10B981", 
    change: "-12% faster",
    changeType: "down", 
    hasChart: true, 
  },
];

export default function FindingsPage() {
  const { theme } = useTheme();
  const [search, setSearch] = useState("");

  // VERIFICACIÓN DE SEGURIDAD: Si theme no está disponible, mostrar error visible.
  if (!theme || !theme.colors) {
      console.error("CRITICAL: Theme context is undefined. Is the app wrapped in ThemeProvider?");
      return (
         <div style={{ height: '100vh', display: 'flex', justifyContent: 'center', alignItems: 'center', backgroundColor: '#000', color: '#FFF' }}>
            <h1>Theme Unavailable</h1>
            <p>Please check your ThemeProvider setup.</p>
         </div>
      );
  }
  
  // ALIAS CORRECTO: Acceso directo a theme.colors (estructura plana de ThemeConfig)
  const c = theme.colors; 

  const layoutStyle = {
    background: c.background, 
  };
  const headerStyle = {
    background: c.surface, 
    borderBottom: `1px solid ${c.border}`,
  };

  return (
    <div className="findings-layout app" style={layoutStyle}>
      <Sidebar activeItem="findings" /> 

      <main className="findings-main main">
        {/* HEADER */}
        <header className="findings-header header" style={headerStyle}>
          <h1 style={{ color: c.text.primary, fontSize: '1.5rem', fontWeight: 700 }}>
            Security Findings
          </h1>

          <div className="header-actions">
            {/* Search */}
            <div style={{ position: "relative" }}>
                <Search
                    size={16}
                    style={{
                        position: "absolute",
                        top: "50%",
                        left: 12,
                        transform: "translateY(-50%)",
                        color: c.text.tertiary,
                    }}
                />
                <input
                    type="text"
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                    placeholder="Search findings (ID, CVE, Desc)..."
                    style={{
                        paddingLeft: 36,
                        background: c.background,
                        borderColor: c.border,
                        color: c.text.primary,
                        borderRadius: '0.5rem',
                        padding: '0.5rem 0.625rem 0.5rem 2.5rem',
                        borderWidth: '1px',
                        fontSize: '0.875rem'
                    }}
                />
            </div>
            {/* Notification Bell */}
            <button className="icon-btn" style={{ color: c.text.secondary }}>
              <Bell size={22} />
              <span className="notification-dot" style={{ backgroundColor: c.danger }}/>
            </button>
          </div>
        </header>

        {/* Scrollable Page Content */}
        <section className="page-content-scrollable content">
          {/* Statistics Row */}
          <div className="stats-grid kpis">
            {statsData.map((stat, index) => (
              <StatCard key={index} {...stat} theme={theme} />
            ))}
          </div>

          {/* Filters & Actions Toolbar */}
          <div className="toolbar" style={{ marginTop: '0.5rem' }}>
            <div className="filters-group">
              {/* Severity Filter */}
              <button 
                className="filter-btn"
                style={{ background: c.surface, borderColor: c.border, color: c.text.secondary }}
              >
                <Filter size={18} style={{ color: c.text.secondary }} />
                Severity
                <ChevronDown size={18} />
              </button>
              {/* Status Filter */}
              <button 
                className="filter-btn"
                style={{ background: c.surface, borderColor: c.border, color: c.text.secondary }}
              >
                Status: <span className="highlight" style={{ color: c.primary, fontWeight: 600 }}>Open</span>
                <ChevronDown size={18} />
              </button>
              <button 
                className="reset-btn" 
                style={{ color: c.text.tertiary, background: 'none', border: 'none', cursor: 'pointer' }}
              >
                Reset
              </button>
            </div>
            
            <div className="actions-group">
              <button 
                className="action-btn secondary btn-outline"
                style={{ 
                    borderColor: c.primary, 
                    color: c.primary, 
                    background: c.surface 
                }}
              >
                <Download size={18} />
                Export
              </button>
              <button 
                className="action-btn primary btn-primary"
                style={{ background: c.primary, color: c.onPrimary, border: `1px solid ${c.primary}` }}
              >
                <Plus size={18} />
                Manual Finding
              </button>
            </div>
          </div>

          {/* Data Table */}
          <div 
            className="card"
            style={{ 
              background: c.surface, 
              borderColor: c.border,
              marginTop: '1.5rem',
              padding: 0 
            }}
          >
            <FindingsTable theme={theme} /> 
          </div>
        </section>
      </main>
    </div>
  );
}