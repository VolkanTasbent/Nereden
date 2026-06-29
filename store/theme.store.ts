import { create } from 'zustand';

import type { ThemePreference } from '@/types';

interface ThemeState {
  preference: ThemePreference;
  resolvedTheme: 'light' | 'dark';
  setPreference: (preference: ThemePreference) => void;
  setResolvedTheme: (theme: 'light' | 'dark') => void;
}

export const useThemeStore = create<ThemeState>((set) => ({
  preference: 'system',
  resolvedTheme: 'light',
  setPreference: (preference) => set({ preference }),
  setResolvedTheme: (resolvedTheme) => set({ resolvedTheme }),
}));
