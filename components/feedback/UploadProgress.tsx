import { View } from 'react-native';
import Animated, { useAnimatedStyle, withTiming } from 'react-native-reanimated';

import { Text } from '@/components/ui/Text';
import { useTheme } from '@/hooks';

interface UploadProgressProps {
  progress: number;
  label: string;
}

export function UploadProgress({ progress, label }: UploadProgressProps) {
  const { theme } = useTheme();
  const barStyle = useAnimatedStyle(() => ({
    width: withTiming(`${Math.min(progress, 1) * 100}%`, { duration: 300 }),
  }));

  return (
    <View className="w-full">
      <Text variant="bodySm" color="secondary" className="mb-2 text-center">
        {label}
      </Text>
      <View
        className="h-2 w-full rounded-full overflow-hidden"
        style={{ backgroundColor: theme.colors.backgroundTertiary }}
      >
        <Animated.View
          style={[barStyle, { height: '100%', backgroundColor: theme.colors.accent }]}
        />
      </View>
    </View>
  );
}
