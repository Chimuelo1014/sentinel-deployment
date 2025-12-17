import { Outlet, RouterProvider } from 'react-router-dom';
import { router } from './utils/router';
import { ThemeProvider } from './utils/store/themeContext';
import { useThemeStyles } from '@hooks/useThemeStyles';
import './App.css';
import { useTheme} from "./utils/store/themeContext";

export function AppContent() {
  const { theme } = useTheme();
  const { getCSSVariables } = useThemeStyles();

  return (
    <div
      style={{
        backgroundColor: theme.colors.background,
        color: theme.colors.text.primary,
        minHeight: "100vh",
        overflow: "hidden",
      }}
    >

      <div style={getCSSVariables()}>
        <Outlet /> 
      </div>

    </div>
  );
}

function App() {
  return (
    <ThemeProvider>
      <RouterProvider router={router} />
    </ThemeProvider>
  );
}

export default App;
