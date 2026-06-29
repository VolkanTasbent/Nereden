import { useColorScheme } from 'react-native';
import { useEffect, type ReactNode } from 'react';

import { useThemeStore } from '@/store';

interface ThemeProviderProps {
  children: ReactNode;
}

export function ThemeProvider({ children }: ThemeProviderProps) {
  const systemScheme = useColorScheme();
  const { preference, setResolvedTheme } = useThemeStore();

  useEffect(() => {
    if (preference === 'system') {
      setResolvedTheme(systemScheme === 'dark' ? 'dark' : 'light');
      return;
    }

    setResolvedTheme(preference);
  }, [preference, systemScheme, setResolvedTheme]);

  return <>{children}</>;
}
