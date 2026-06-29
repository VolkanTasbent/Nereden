import { Pressable, ActivityIndicator, Platform, type PressableProps } from 'react-native';
import * as Haptics from 'expo-haptics';

import { Text } from '@/components/ui/Text';
import { useTheme } from '@/hooks';
import { cn } from '@/utils/cn';

type ButtonVariant = 'primary' | 'secondary' | 'ghost' | 'accent';
type ButtonSize = 'sm' | 'md' | 'lg';

interface ButtonProps extends Omit<PressableProps, 'children'> {
  label: string;
  variant?: ButtonVariant;
  size?: ButtonSize;
  loading?: boolean;
  fullWidth?: boolean;
  className?: string;
}

const sizeStyles: Record<ButtonSize, string> = {
  sm: 'h-9 px-4 rounded-full',
  md: 'h-11 px-5 rounded-full',
  lg: 'h-12 px-6 rounded-full',
};

export function Button({
  label,
  variant = 'primary',
  size = 'md',
  loading = false,
  fullWidth = false,
  disabled,
  className,
  onPress,
  ...props
}: ButtonProps) {
  const { theme } = useTheme();
  const isDisabled = disabled || loading;

  const variantStyles: Record<ButtonVariant, { bg: string; text: string; border?: string }> = {
    primary: {
      bg: theme.colors.accent,
      text: theme.colors.accentForeground,
    },
    secondary: {
      bg: theme.colors.backgroundSecondary,
      text: theme.colors.foreground,
      border: theme.colors.border,
    },
    ghost: {
      bg: 'transparent',
      text: theme.colors.foreground,
    },
    accent: {
      bg: theme.colors.accent,
      text: theme.colors.accentForeground,
    },
  };

  const styles = variantStyles[variant];

  return (
    <Pressable
      accessibilityRole="button"
      accessibilityState={{ disabled: isDisabled, busy: loading }}
      disabled={isDisabled}
      className={cn(
        'items-center justify-center flex-row',
        sizeStyles[size],
        fullWidth && 'w-full',
        isDisabled && 'opacity-40',
        className,
      )}
      style={{
        backgroundColor: styles.bg,
        borderWidth: styles.border ? 1 : 0,
        borderColor: styles.border,
      }}
      onPress={(event) => {
        if (Platform.OS !== 'web') {
          void Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
        }
        onPress?.(event);
      }}
      {...props}
    >
      {loading ? (
        <ActivityIndicator color={styles.text} />
      ) : (
        <Text variant="titleSm" style={{ color: styles.text }}>
          {label}
        </Text>
      )}
    </Pressable>
  );
}
