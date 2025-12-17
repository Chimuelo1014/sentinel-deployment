// src/ui/components/Button.tsx
import { useTheme } from '../../utils/store/themeContext';
import React from 'react';
import { Link } from "react-router-dom";

interface ButtonProps {
  text: string;
  url?: string;
  variant?: 'primary' | 'secondary' | 'success' | 'danger' | 'warning' | 'info';
  size?: 'small' | 'medium' | 'large';
  disabled?: boolean;
  onClick?: () => void;
  fullWidth?: boolean;
  icon?: string;
  iconPosition?: 'left' | 'right';
  pill?: boolean;
}

export const Button: React.FC<ButtonProps> = ({
  text,
  url,
  variant = 'primary',
  size = 'medium',
  disabled = false,
  onClick,
  fullWidth = false,
  icon,
  iconPosition = 'right',
  pill = false,
}) => {
  const { theme, mode } = useTheme();

  const getColorByVariant = (variant: string): string => {
    const colorMap: Record<string, string> = {
      primary: theme.colors.primary,
      secondary: theme.colors.secondary,
      success: theme.colors.success,
      danger: theme.colors.danger,
      warning: theme.colors.warning,
      info: theme.colors.info,
    };
    return colorMap[variant] || theme.colors.primary;
  };

  const getSizeStyles = (size: string) => {
    const sizeMap: Record<string, { padding: string; fontSize: string }> = {
      small: { padding: '3px 3px', fontSize: '12px' },
      medium: { padding: '5px 2px 5px 15px', fontSize: '12px' },
      large: { padding: '10px 10px', fontSize: '16px' },
    };
    return sizeMap[size] || sizeMap.medium;
  };

  const color = getColorByVariant(variant);
  const sizeStyles = getSizeStyles(size);

  // Validar tema y cambiar colores
  const buttonColor = mode === 'dark' ? '#FFFFFF' : theme.colors.primary;
  const iconCircleColor = mode === 'dark' ? theme.colors.primary : '#FFFFFF';
  const textColor = mode === 'dark' ? theme.colors.primary : '#FFFFFF';

  const baseStyles: React.CSSProperties = {
    display: 'inline-flex',
    alignItems: 'center',
    gap: '10px',
    border: 'none',
    backgroundColor: buttonColor,
    color: textColor,
    fontSize: sizeStyles.fontSize,
    borderRadius: pill ? '50px' : '8px',
    cursor: disabled ? 'not-allowed' : 'pointer',
    fontWeight: '600',
    letterSpacing: '0.5px',
    transition: 'all 0.3s ease',
    opacity: disabled ? 0.6 : 1,
    width: fullWidth ? '100%' : 'auto',
    textDecoration: 'none',
    justifyContent: 'center',
  };

  const iconStyle: React.CSSProperties = {
    width: '37px',
    height: '37px',
    borderRadius: '50%',
    margin: '2px',
    backgroundColor: iconCircleColor,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    color: buttonColor,
    fontSize: '12px',
    fontWeight: 'bold',
  };
  
  const textStyle: React.CSSProperties={
    padding: sizeStyles.padding,
  }

  const handleMouseEnter = (e: React.MouseEvent<HTMLButtonElement | HTMLAnchorElement>) => {
    if (!disabled) {
      (e.currentTarget as HTMLElement).style.transform = 'scale(1.05)';
      (e.currentTarget as HTMLElement).style.boxShadow = `0 0 20px ${color}66`;
      (e.currentTarget as HTMLElement).style.backgroundColor = 
        mode === 'dark' ? theme.colors.primaryLight : theme.colors.primary;
    }
  };

  const handleMouseLeave = (e: React.MouseEvent<HTMLButtonElement | HTMLAnchorElement>) => {
    (e.currentTarget as HTMLElement).style.transform = 'scale(1)';
    (e.currentTarget as HTMLElement).style.boxShadow = 'none';
    (e.currentTarget as HTMLElement).style.backgroundColor = buttonColor;
  };

  const buttonContent = (
    <>
      {icon && iconPosition === 'left' && <span style={iconStyle}>{icon}</span>}
      <span style={textStyle}>{text}</span>
      {icon && iconPosition === 'right' && <span style={iconStyle}>{icon}</span>}
    </>
  );

  // Si tiene URL, renderiza como link
  if (url) {
    return (
      <a
        href={url}
        style={baseStyles}
        onMouseEnter={handleMouseEnter}
        onMouseLeave={handleMouseLeave}
        className="sentinel-button"
      >
        {buttonContent}
      </a>
    );
  }

  // Si no tiene URL, renderiza como button
  return (<Link to="/auth">
    <button
      style={baseStyles}
      disabled={disabled}
      onClick={onClick}
      onMouseEnter={handleMouseEnter}
      onMouseLeave={handleMouseLeave}
      className="sentinel-button"
    >
      {buttonContent}
      
    </button>
    </Link>
  );
};