import React from 'react';
import * as LucideIcons from "lucide-react";

// --- Interfaz de Tema Corregida para estructura plana ---
interface Theme {
  colors: any; 
}

export interface StatCardProps {
  title: string;
  value: string;
  icon: keyof typeof LucideIcons; 
  iconColor: string; 
  change: string;
  changeContext?: string;
  changeType: 'up' | 'down' | 'no-change';
  hasChart?: boolean;
  theme: Theme; // Recibe el objeto theme (ThemeConfig)
}

export default function StatCard({
  title,
  value,
  icon: IconName,
  iconColor,
  change,
  changeContext,
  changeType,
  hasChart = false,
  theme,
}: StatCardProps) {
  
  // VERIFICACIÓN DE SEGURIDAD
  if (!theme || !theme.colors) return null;

  // ALIAS CORRECTO: Accede directamente a theme.colors
  const c = theme.colors; 
  
  const IconComponent = LucideIcons[IconName as keyof typeof LucideIcons];

  // Estilos dinámicos
  const cardStyle = {
    backgroundColor: c.surface, 
    borderColor: c.border,
  };

  const iconWrapperStyle = {
    color: iconColor,
    padding: '0.5rem',
    borderRadius: '0.5rem',
  };

  // Clases CSS para los cambios
  const changeClass = {
    'up': { color: c.danger },    
    'down': { color: c.success }, 
    'no-change': { color: c.text.secondary },
  }[changeType];

  return (
    <div 
      className={`stat-card ${hasChart ? 'with-chart' : ''}`} 
      style={cardStyle}
    >
      <div className="stat-header">
        <span className="stat-title" style={{ color: c.text.secondary }}>{title}</span>
        
        {IconComponent && (
          <div className="icon-wrapper" style={iconWrapperStyle}>
            <IconComponent size={20} />
          </div>
        )}
      </div>

      <div className="stat-body">
        <div className="stat-value" style={{ color: c.text.primary }}>{value}</div>
        
        <div className={`stat-change stat-change-${changeType}`} style={changeClass}>
          {/* Íconos de cambio */}
          {changeType === 'up' && <LucideIcons.ArrowUp size={14} className="change-icon" />}
          {changeType === 'down' && <LucideIcons.ArrowDown size={14} className="change-icon" />}
          
          <span>{change}</span>
          {changeContext && (
            <span className="change-context" style={{ color: c.text.secondary }}>
              {changeContext}
            </span>
          )}
        </div>
      </div>
      
      {/* Background Chart Placeholder */}
      {hasChart && (
        <div className="chart-background">
            <LucideIcons.Activity size={60} style={{ color: c.primary, opacity: 0.2 }} />
        </div>
      )}
    </div>
  );
}