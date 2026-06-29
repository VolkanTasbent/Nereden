import { Text as RNText, type TextProps as RNTextProps } from 'react-native';

import { typography, type TypographyVariant } from '@/constants/typography';
import { useTheme } from '@/hooks';
import { cn } from '@/utils/cn';

type TextColor = 'primary' | 'secondary' | 'tertiary' | 'accent' | 'error';

interface TextProps extends RNTextProps {
  variant?: TypographyVariant;
  color?: TextColor;
  className?: string;
}

const colorMap: Record<TextColor, keyof ReturnType<typeof useTheme>['theme']['colors']> = {
  primary: 'foreground',
  secondary: 'foregroundSecondary',
  tertiary: 'foregroundTertiary',
  accent: 'accent',
  error: 'error',
};

export function Text({
  variant = 'body',
  color = 'primary',
  className,
  style,
  ...props
}: TextProps) {
  const { theme } = useTheme();
  const variantStyle = typography[variant];

  return (
    <RNText
      accessibilityRole={variant.startsWith('display') || variant.startsWith('title') ? 'header' : 'text'}
      className={cn(className)}
      style={[
        variantStyle,
        { color: theme.colors[colorMap[color]] },
        style,
      ]}
      {...props}
    />
  );
}
