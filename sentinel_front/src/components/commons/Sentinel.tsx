import Logo from "@/assets/icons/sentinel-logo.svg?react";
import { useTheme } from '../../utils/store/themeContext';

export default function Sentinel() {
    
      const { theme, toggleTheme, mode } = useTheme();
    return( <div style={{
    fontSize: '18px',
    fontWeight: 'bold',
    letterSpacing: '3px',
    marginBottom: '40px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    gap: '8px'
  }}>
    <Logo style={{ width: 20, height: 20, fill: theme.colors.primary }} />
    <span style={{ color: theme.colors.text.primary }}>
      SEN<span style={{ color: theme.colors.primary }}>TIN</span>EL
    </span>
  </div>)
 }