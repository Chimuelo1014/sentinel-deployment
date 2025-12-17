import React, { ReactNode } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { useTheme } from "@/utils/store/themeContext";
import {
  ShieldCheck,
  LayoutDashboard,
  Radar,
  Folder,
  Bug,
  Bell,
  Settings,
  LogOut,
  CreditCard,
} from "lucide-react";

type SidebarProps = {
  activeItem?: string;
  children?: ReactNode;
};

export default function Sidebar({ activeItem = "dashboard", children }: SidebarProps) {
  const { theme } = useTheme();
  const navigate = useNavigate();

  // 👉 Usuario desde sessionStorage
  const user = JSON.parse(sessionStorage.getItem("user") || "{}");

  const menuItems = [
    { id: "dashboard", label: "Dashboard", icon: LayoutDashboard, path: "/dashboard" },
    { id: "scans", label: "Scans", icon: Radar, path: "/scans" },
    { id: "workspaces", label: "Workspaces", icon: Folder, path: "/workspaces" },
    { id: "findings", label: "Findings", icon: Bug, path: "/findings" },
    { id: "billing", label: "Billing", icon: CreditCard, path: "/billing" },
    { id: "settings", label: "Settings", icon: Settings, path: "/settings" },
  ];

  const logout = () => {
    sessionStorage.removeItem("user");
    sessionStorage.removeItem("token");
    navigate("/auth");
  };

  return (
    <div style={{ display: "flex", minHeight: "100vh" }}>
      <aside
        className="sidebar"
        style={{
          background: theme.colors.surface,
          borderRight: `1px solid ${theme.colors.border}`,
        }}
      >
        {/* Logo */}
        <div
          className="logo"
          style={{
            color: theme.colors.text.primary,
            display: "flex",
            alignItems: "center",
            gap: 10,
            fontWeight: 600,
          }}
        >
          <ShieldCheck size={20} />
          Sentinel
        </div>

        {/* Navigation */}
        <nav>
          {menuItems.map(({ label, icon: Icon, id, path }) => (
            <NavLink
              key={id}
              to={path}
              className={({ isActive }) => (isActive ? "active" : "")}
              style={({ isActive }) => ({
                background: isActive ? theme.colors.primary : "transparent",
                color: isActive
                  ? theme.colors.onPrimary
                  : theme.colors.text.secondary,
                display: "flex",
                alignItems: "center",
                gap: 12,
                fontWeight: 500,
              })}
            >
              <Icon size={18} />
              {label}
            </NavLink>
          ))}
        </nav>

        {/* User */}
        <div className="user">
          <div
            className="avatar"
            style={{
              background: theme.colors.primary,
              color: theme.colors.onPrimary,
            }}
          >
            {user?.name ? user.name.charAt(0).toUpperCase() : "U"}
          </div>

          <div style={{ flex: 1 }}>
            <strong style={{ color: theme.colors.text.primary }}>
              {user?.name || "Unknown User "}
            </strong>
            <span style={{ color: theme.colors.text.secondary }}>
              {user?.company || "No company"}
            </span>
          </div>

          {/* Logout */}
          <button
            onClick={logout}
            title="Cerrar sesión"
            style={{
              background: "transparent",
              border: "none",
              cursor: "pointer",
              color: theme.colors.danger,
            }}
          >
            <LogOut size={18} />
          </button>
        </div>
      </aside>

      {/* Main content area */}
      <main style={{ flex: 1, overflow: "auto" }}>
        {children}
      </main>
    </div>
  );
}
