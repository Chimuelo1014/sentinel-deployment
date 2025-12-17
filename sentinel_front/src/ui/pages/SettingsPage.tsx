import React, { useState } from "react";
import Sidebar from "@/components/commons/Sidebar";
import { useTheme } from "@/utils/store/themeContext";
import {
  Palette,
  Sun,
  Moon,
  Check,
  Shield,
  Bell,
  ChevronRight,
} from "lucide-react";

export default function SettingsPage() {
  const { theme } = useTheme();
  const [mode, setMode] = useState("dark");

  if (!theme || !theme.colors) return null;

  const c = theme.colors;

  return (
    <div className="settings-layout">
      <Sidebar activeItem="settings" />

      <main className="settings-main">
        {/* HEADER */}
        <header
          className="settings-header"
          style={{ borderBottomColor: c.border }}
        >
          <h1 style={{ color: c.text.primary }}>
            <Shield size={20} />
            System Settings
          </h1>
        </header>

        {/* CONTENT */}
        <section className="settings-content">
          {/* APPEARANCE */}
          <h2 className="settings-section-title" style={{ color: c.text.primary }}>
            <Palette size={18} />
            Appearance
          </h2>

          <div
            className="settings-card"
            style={{ background: c.surface, borderColor: c.border }}
          >
            <div className="setting-row">
              <div>
                <span className="setting-title">Theme Mode</span>
                <span className="setting-desc">
                  Customize the interface appearance
                </span>
              </div>

              <div className="theme-toggle">
                <button
                  className={mode === "dark" ? "active" : ""}
                  onClick={() => setMode("dark")}
                  style={{
                    background: mode === "dark" ? c.primary : "transparent",
                    color:
                      mode === "dark" ? c.onPrimary : c.text.secondary,
                    borderColor: c.border,
                  }}
                >
                  <Moon size={16} />
                  Dark
                  {mode === "dark" && <Check size={14} />}
                </button>

                <button
                  className={mode === "light" ? "active" : ""}
                  onClick={() => setMode("light")}
                  style={{
                    background: mode === "light" ? c.primary : "transparent",
                    color:
                      mode === "light" ? c.onPrimary : c.text.secondary,
                    borderColor: c.border,
                  }}
                >
                  <Sun size={16} />
                  Light
                  {mode === "light" && <Check size={14} />}
                </button>
              </div>
            </div>
          </div>

          {/* NOTIFICATIONS */}
          <h2 className="settings-section-title" style={{ color: c.text.primary }}>
            <Bell size={18} />
            Notifications
          </h2>

          <div
            className="settings-card"
            style={{ background: c.surface, borderColor: c.border }}
          >
            <div className="setting-link">
              <div>
                <span className="setting-title">Email Alerts</span>
                <span className="setting-desc">
                  Manage critical finding emails
                </span>
              </div>
              <ChevronRight size={18} />
            </div>

            <div className="setting-link">
              <div>
                <span className="setting-title">Integration Webhooks</span>
                <span className="setting-desc">
                  Slack, Jira and external services
                </span>
              </div>
              <ChevronRight size={18} />
            </div>
          </div>
        </section>
      </main>
    </div>
  );
}
/* ---------------- SIDEBAR COMPONENT ---------------- */