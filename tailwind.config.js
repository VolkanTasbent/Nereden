/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./app/**/*.{js,jsx,ts,tsx}', './components/**/*.{js,jsx,ts,tsx}'],
  presets: [require('nativewind/preset')],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        background: {
          DEFAULT: '#F5F5F7',
          secondary: '#FFFFFF',
          tertiary: '#EFEFF4',
          dark: '#000000',
          'dark-secondary': '#1C1C1E',
          'dark-tertiary': '#2C2C2E',
        },
        foreground: {
          DEFAULT: '#0A0A0A',
          secondary: '#6E6E73',
          tertiary: '#AEAEB2',
          dark: '#F5F5F7',
          'dark-secondary': '#98989D',
          'dark-tertiary': '#636366',
        },
        primary: {
          DEFAULT: '#0A0A0A',
          foreground: '#FFFFFF',
          muted: '#3A3A3C',
        },
        accent: {
          DEFAULT: '#0A0A0A',
          foreground: '#FFFFFF',
          muted: '#EFEFF4',
        },
        success: {
          DEFAULT: '#34C759',
          foreground: '#FFFFFF',
          muted: '#E8F8ED',
        },
        warning: {
          DEFAULT: '#FF9F0A',
          foreground: '#FFFFFF',
          muted: '#FFF4E5',
        },
        error: {
          DEFAULT: '#FF3B30',
          foreground: '#FFFFFF',
          muted: '#FFEBEA',
        },
        border: {
          DEFAULT: '#E5E5EA',
          dark: '#38383A',
        },
        glass: {
          light: 'rgba(255, 255, 255, 0.88)',
          dark: 'rgba(28, 28, 30, 0.88)',
        },
      },
      fontFamily: {
        sans: ['Inter-Regular'],
        'sans-medium': ['Inter-Medium'],
        'sans-semibold': ['Inter-SemiBold'],
        'sans-bold': ['Inter-Bold'],
      },
      fontSize: {
        'display-lg': ['32px', { lineHeight: '38px', letterSpacing: '-0.5px' }],
        'display-md': ['26px', { lineHeight: '32px', letterSpacing: '-0.4px' }],
        'title-lg': ['20px', { lineHeight: '26px', letterSpacing: '-0.3px' }],
        'title-md': ['17px', { lineHeight: '22px', letterSpacing: '-0.2px' }],
        'title-sm': ['15px', { lineHeight: '20px', letterSpacing: '-0.1px' }],
        body: ['16px', { lineHeight: '22px' }],
        'body-sm': ['14px', { lineHeight: '20px' }],
        caption: ['12px', { lineHeight: '16px' }],
        overline: ['11px', { lineHeight: '14px', letterSpacing: '0.6px' }],
      },
      borderRadius: {
        sm: '8px',
        md: '12px',
        lg: '16px',
        xl: '20px',
        '2xl': '24px',
        '3xl': '32px',
      },
      boxShadow: {
        card: '0 1px 2px rgba(0, 0, 0, 0.04)',
        elevated: '0 4px 24px rgba(0, 0, 0, 0.06)',
        glass: '0 8px 32px rgba(0, 0, 0, 0.08)',
      },
      spacing: {
        18: '72px',
        22: '88px',
      },
    },
  },
  plugins: [],
};
