import { View, type ViewProps } from 'react-native';

import { useTheme } from '@/hooks';
import { cn } from '@/utils/cn';

interface GlassCardProps extends ViewProps {
  padding?: 'sm' | 'md' | 'lg';
  className?: string;
}

const paddingMap = {
  sm: 'p-3',
  md: 'p-4',
  lg: 'p-5',
};

export function GlassCard({
  padding = 'md',
  className,
  children,
  style,
  ...props
}: GlassCardProps) {
  const { theme } = useTheme();

  return (
    <View
      className={cn('rounded-2xl', paddingMap[padding], className)}
      style={[
        {
          backgroundColor: theme.colors.backgroundSecondary,
          borderWidth: 1,
          borderColor: theme.colors.border,
        },
        style,
      ]}
      {...props}
    >
      {children}
    </View>
  );
}
