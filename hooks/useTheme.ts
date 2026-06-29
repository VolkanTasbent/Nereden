import { useThemeStore } from '@/store';
import { theme, type AppTheme } from '@/constants';

export function useTheme(): {
  theme: AppTheme;
  isDark: boolean;
  preference: 'light' | 'dark' | 'system';
} {
  const { resolvedTheme, preference } = useThemeStore();

  return {
    theme: theme[resolvedTheme],
    isDark: resolvedTheme === 'dark',
    preference,
  };
}
