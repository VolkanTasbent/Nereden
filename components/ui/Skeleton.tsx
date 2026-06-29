import { View } from 'react-native';

import { useTheme } from '@/hooks';
import { cn } from '@/utils/cn';

interface SkeletonProps {
  width?: number | `${number}%`;
  height?: number;
  rounded?: 'sm' | 'md' | 'lg' | 'full';
  className?: string;
}

const roundedMap = {
  sm: 8,
  md: 12,
  lg: 16,
  full: 9999,
};

export function Skeleton({
  width = '100%',
  height = 16,
  rounded = 'md',
  className,
}: SkeletonProps) {
  const { theme } = useTheme();

  return (
    <View
      accessibilityLabel="Yükleniyor"
      accessibilityRole="progressbar"
      className={cn(className)}
      style={{
        width,
        height,
        borderRadius: roundedMap[rounded],
        backgroundColor: theme.colors.backgroundTertiary,
      }}
    />
  );
}
