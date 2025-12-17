import { palette } from './colors';

export type ThemeMode = 'dark' | 'light';

export interface ThemeConfig {
  colors: typeof palette.colors.layout;
  mode: ThemeMode;
}
export const darkTheme: ThemeConfig = {
  mode: 'dark',
  colors: {
    ...palette.colors.layout,

    primary: '#FF1B6D',
    onPrimary: '#FFFFFF',

    background: '#000000ff',
    surface: '#1A1A1A',
    onSurface: '#FFFFFF',

    danger: '#E53935',
    onDanger: '#FFFFFF',

    text: {
      primary: '#FFFFFF',
      secondary: '#B0B0B0',
      tertiary: '#808080',
    },

    border: '#2A2A2A',
  },
};


// Tema claro
export const lightTheme: ThemeConfig = {
  mode: 'light',
  colors: {
    ...palette.colors.layout,

    primary: '#FF1B6D',
    primaryLight: '#FF4D8D',
    primaryDark: '#a50540ff',
    onPrimary: '#FFFFFF',

    background: '#FFFFFF',
    surface: '#F5F5F5',
    surfaceLight: '#EFEFEF',
    onSurface: '#0A0A0A',

    danger: '#D32F2F',
    onDanger: '#FFFFFF',

    text: {
      primary: '#0A0A0A',
      secondary: '#4A4A4A',
      tertiary: '#808080',
    },
    border: '#E0E0E0',
    borderLight: '#F0F0F0',
    borderDark: '#D0D0D0',
  },
};
