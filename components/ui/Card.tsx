import { View, type ViewProps } from 'react-native';

import { useTheme } from '@/hooks';
import { cn } from '@/utils/cn';

interface CardProps extends ViewProps {
  variant?: 'default' | 'elevated' | 'outline';
  padding?: 'none' | 'sm' | 'md' | 'lg';
  className?: string;
}

const paddingMap = {
  none: '',
  sm: 'p-3',
  md: 'p-4',
  lg: 'p-5',
};

export function Card({
  variant = 'default',
  padding = 'md',
  className,
  style,
  children,
  ...props
}: CardProps) {
  const { theme } = useTheme();

  return (
    <View
      accessibilityRole="none"
      className={cn('rounded-2xl', paddingMap[padding], className)}
      style={[
        {
          backgroundColor: theme.colors.backgroundSecondary,
          borderWidth: variant === 'outline' || variant === 'default' ? 1 : 0,
          borderColor: theme.colors.border,
        },
        variant === 'elevated' && {
          shadowColor: '#000',
          shadowOffset: { width: 0, height: 2 },
          shadowOpacity: 0.04,
          shadowRadius: 8,
          elevation: 2,
        },
        style,
      ]}
      {...props}
    >
      {children}
    </View>
  );
}
