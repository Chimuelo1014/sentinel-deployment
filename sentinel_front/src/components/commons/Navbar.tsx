import { useEffect } from "react";
import { useLocation, Link } from "react-router-dom";
import { useTheme } from "../../utils/store/themeContext";
import { useTranslation } from "react-i18next";
import { Globe } from "lucide-react";
import { Button } from "./Button";
import { IconButton } from "./toggleButton";
import Logo from "@/assets/icons/sentinel-logo.svg?react";

export const Navbar = () => {
  const { theme, mode, setTheme } = useTheme();

  const { i18n, t } = useTranslation();
  const location = useLocation();

  /* 🔥 Forzar tema según la ruta */
  useEffect(() => {
    if (location.pathname.startsWith("/auth")) {
      if (mode !== "light") setTheme("light");
    } else {
      if (mode !== "dark") setTheme("dark");
    }
  }, [location.pathname, mode, setTheme]);

  const toggleLanguage = () => {
    i18n.changeLanguage(i18n.language === "es" ? "en" : "es");
  };
  return (
    <nav
      style={{
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        padding: "20px 40px",
        backdropFilter: "blur(10px)",
        borderBottom: `1px solid ${theme.colors.border}`,
        position: "sticky",
        top: 0,
        zIndex: 100,
      }}
    >
      <div
        style={{ fontSize: "24px", fontWeight: "bold", letterSpacing: "2px" }}
      >
        <Logo style={{ width: 24, height: 24,}} />
        EN
        <span style={{ color: theme.colors.primary }}>TIN</span>EL
      </div>

      <div style={{ display: "flex", gap: "30px", alignItems: "center" }}>
        <Link
          to="/"
          style={{
            color: theme.colors.text.secondary,
            textDecoration: "none",
            cursor: "pointer",
          }}
        >
          {t("nav.home")}
        </Link>
        <Link
          to="/"
          style={{
            color: theme.colors.text.secondary,
            textDecoration: "none",
            cursor: "pointer",
          }}
        >
          {t("nav.services")}
        </Link>
        <a
          href="#"
          style={{
            color: theme.colors.text.secondary,
            textDecoration: "none",
            cursor: "pointer",
          }}
        >
          {t("nav.contact")}
        </a>
      </div>

      <div style={{ display: "flex", gap: "15px", alignItems: "center" }}>
   

        <IconButton
          icon={
            <>
              <Globe size={18} />
              <span style={{ marginLeft: "4px" }}>
                {i18n.language === "es" ? "🇪🇸" : "🇺🇸"}
              </span>
            </>
          }
          onClick={toggleLanguage}
          color={theme.colors.primary}
          size={18}
          ariaLabel={`Current language: ${
            i18n.language === "es" ? "Spanish" : "English"
          }`}
        />
    
        <Button
          text="Sing In /"
          icon="Up"
          iconPosition="right"
          variant="primary"
          pill={true}
        />
      </div>
    </nav>
  );
};
