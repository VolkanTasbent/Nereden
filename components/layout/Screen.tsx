import { View, type ViewProps } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { useTheme } from '@/hooks';
import { cn } from '@/utils/cn';

interface ScreenProps extends ViewProps {
  edges?: ('top' | 'bottom' | 'left' | 'right')[];
  padded?: boolean;
  className?: string;
}

export function Screen({
  edges = ['top', 'bottom'],
  padded = true,
  className,
  style,
  children,
  ...props
}: ScreenProps) {
  const insets = useSafeAreaInsets();
  const { theme } = useTheme();

  return (
    <View
      className={cn('flex-1', padded && 'px-5', className)}
      style={[
        {
          backgroundColor: theme.colors.background,
          paddingTop: edges.includes('top') ? insets.top : 0,
          paddingBottom: edges.includes('bottom') ? insets.bottom : 0,
          paddingLeft: edges.includes('left') ? insets.left : 0,
          paddingRight: edges.includes('right') ? insets.right : 0,
        },
        style,
      ]}
      {...props}
    >
      {children}
    </View>
  );
}
