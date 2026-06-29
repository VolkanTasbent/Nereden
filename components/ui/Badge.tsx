import { View } from 'react-native';

import { Text } from '@/components/ui/Text';
import { useTheme } from '@/hooks';
import { cn } from '@/utils/cn';

interface BadgeProps {
  label: string;
  variant?: 'default' | 'accent' | 'success' | 'warning';
  className?: string;
}

export function Badge({ label, variant = 'default', className }: BadgeProps) {
  const { theme } = useTheme();

  const colors = {
    default: {
      bg: theme.colors.backgroundTertiary,
      text: theme.colors.foregroundSecondary,
    },
    accent: {
      bg: theme.colors.accentMuted,
      text: theme.colors.accent,
    },
    success: {
      bg: '#E8F8ED',
      text: theme.colors.success,
    },
    warning: {
      bg: '#FFF4E5',
      text: theme.colors.warning,
    },
  }[variant];

  return (
    <View
      className={cn('self-start rounded-full px-3 py-1', className)}
      style={{ backgroundColor: colors.bg }}
    >
      <Text variant="caption" style={{ color: colors.text }}>
        {label}
      </Text>
    </View>
  );
}
