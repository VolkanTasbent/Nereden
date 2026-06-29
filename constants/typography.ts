export const typography = {
  displayLg: {
    fontSize: 32,
    lineHeight: 38,
    letterSpacing: -0.5,
    fontFamily: 'Inter-Bold',
  },
  displayMd: {
    fontSize: 26,
    lineHeight: 32,
    letterSpacing: -0.4,
    fontFamily: 'Inter-Bold',
  },
  titleLg: {
    fontSize: 20,
    lineHeight: 26,
    letterSpacing: -0.3,
    fontFamily: 'Inter-SemiBold',
  },
  titleMd: {
    fontSize: 17,
    lineHeight: 22,
    letterSpacing: -0.2,
    fontFamily: 'Inter-SemiBold',
  },
  titleSm: {
    fontSize: 15,
    lineHeight: 20,
    letterSpacing: -0.1,
    fontFamily: 'Inter-SemiBold',
  },
  body: {
    fontSize: 16,
    lineHeight: 22,
    fontFamily: 'Inter-Regular',
  },
  bodySm: {
    fontSize: 14,
    lineHeight: 20,
    fontFamily: 'Inter-Regular',
  },
  caption: {
    fontSize: 12,
    lineHeight: 16,
    fontFamily: 'Inter-Regular',
  },
  overline: {
    fontSize: 11,
    lineHeight: 14,
    letterSpacing: 0.6,
    fontFamily: 'Inter-Medium',
    textTransform: 'uppercase' as const,
  },
} as const;

export type TypographyVariant = keyof typeof typography;
