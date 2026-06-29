import { darkColors, lightColors } from './colors';
import { borderRadius, spacing, animation } from './spacing';
import { typography } from './typography';

export const theme = {
  light: {
    colors: lightColors,
    spacing,
    borderRadius,
    animation,
    typography,
  },
  dark: {
    colors: darkColors,
    spacing,
    borderRadius,
    animation,
    typography,
  },
} as const;

export type ThemeMode = keyof typeof theme;
export type AppTheme = (typeof theme)[ThemeMode];
