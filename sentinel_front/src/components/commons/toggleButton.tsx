// src/ui/components/IconButton.tsx
import { CSSProperties, ReactNode } from 'react';

interface IconButtonProps {
  icon: ReactNode;
  onClick: () => void;
  color?: string;
  size?: number;
  ariaLabel?: string;
}

export const IconButton = ({ 
  icon, 
  onClick, 
  color = '#00ff88', 
  size = 20,
  ariaLabel 
}: IconButtonProps) => {
  const buttonStyle: CSSProperties = {
    background: 'none',
    border: 'none',
    color: color,
    fontSize: size,
    cursor: 'pointer',
    padding: '8px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    transition: 'transform 0.2s ease, opacity 0.2s ease',
    opacity: 0.9
  };

  return (
    <button
      onClick={onClick}
      style={buttonStyle}
      aria-label={ariaLabel}
      onMouseEnter={(e) => {
        e.currentTarget.style.transform = 'scale(1.1)';
        e.currentTarget.style.opacity = '1';
      }}
      onMouseLeave={(e) => {
        e.currentTarget.style.transform = 'scale(1)';
        e.currentTarget.style.opacity = '0.9';
      }}
    >
      {icon}
    </button>
  );
};