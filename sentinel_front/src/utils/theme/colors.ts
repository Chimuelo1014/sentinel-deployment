export const palette = {
  colors: {
    layout: {
      // Colores primarios - Rosa/Magenta como en la imagen
      primary: '#FF1B6D',
      primaryLight: '#FF4D8D',
      primaryDark: '#a50540ff',
       onPrimary: '#FFFFFF',
      
      // Colores secundarios
      secondary: '#1A1A1A',
      secondaryLight: '#2D2D2D',
      secondaryDark: '#0F0F0F',
      
      // Neutrales
      background: '#000000ff',
      surface: '#1A1A1A',
      surfaceLight: '#2D2D2D',
      onSurface: '#FFFFFF',
      
      text: {
        primary: '#FFFFFF',
        secondary: '#B0B0B0',
        tertiary: '#808080',
      },
      
      // Estados
      success: '#10B981',
      warning: '#F59E0B',
      danger: '#EF4444',
      info: '#3B82F6',
      onDanger: '#FFFFFF',
      
      // Bordes
      border: '#333333',
      borderLight: '#404040',
      borderDark: '#1F1F1F',
    },
  },
};

export type PaletteColors = typeof palette.colors.layout;