import { useMemo } from 'react';
import { useTheme } from '@utils/store/themeContext';

export const useThemeStyles = () => {
  const { theme } = useTheme();

  return useMemo(() => ({
    getCSSVariables: () => ({
      '--color-primary': theme.colors.primary,
      '--color-primary-light': theme.colors.primaryLight,
      '--color-primary-dark': theme.colors.primaryDark,
      '--color-secondary': theme.colors.secondary,
      '--color-secondary-light': theme.colors.secondaryLight,
      '--color-background': theme.colors.background,
      '--color-surface': theme.colors.surface,
      '--color-surface-light': theme.colors.surfaceLight,
      '--color-text-primary': theme.colors.text.primary,
      '--color-text-secondary': theme.colors.text.secondary,
      '--color-text-tertiary': theme.colors.text.tertiary,
      '--color-border': theme.colors.border,
      '--color-border-light': theme.colors.borderLight,
      '--color-success': theme.colors.success,
      '--color-warning': theme.colors.warning,
      '--color-danger': theme.colors.danger,
    } as React.CSSProperties),
    colors: theme.colors,
  }), [theme]);
};