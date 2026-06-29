import { useEffect } from 'react';
import { Pressable, View } from 'react-native';
import Animated, {
  useAnimatedStyle,
  useSharedValue,
  withSpring,
  withTiming,
} from 'react-native-reanimated';

import { Text } from '@/components/ui/Text';
import { useTheme } from '@/hooks';
import { useToastStore } from '@/store/toast.store';

export function Toast() {
  const { visible, message, type, hide } = useToastStore();
  const { theme, isDark } = useTheme();
  const translateY = useSharedValue(-100);

  useEffect(() => {
    translateY.value = visible
      ? withSpring(0, { damping: 18, stiffness: 220 })
      : withTiming(-100, { duration: 200 });
  }, [visible, translateY]);

  const animatedStyle = useAnimatedStyle(() => ({
    transform: [{ translateY: translateY.value }],
  }));

  if (!visible) {
    return null;
  }

  const bgColor =
    type === 'error'
      ? theme.colors.error
      : type === 'success'
        ? theme.colors.success
        : isDark
          ? theme.colors.backgroundTertiary
          : theme.colors.foreground;

  const textColor =
    type === 'error' || type === 'success'
      ? '#FFFFFF'
      : isDark
        ? theme.colors.foreground
        : theme.colors.background;

  return (
    <Animated.View
      style={[
        animatedStyle,
        {
          position: 'absolute',
          top: 56,
          left: 20,
          right: 20,
          zIndex: 9999,
        },
      ]}
    >
      <Pressable onPress={hide}>
        <View
          className="rounded-2xl px-4 py-3 shadow-elevated"
          style={{ backgroundColor: bgColor }}
        >
          <Text variant="bodySm" style={{ color: textColor, textAlign: 'center' }}>
            {message}
          </Text>
        </View>
      </Pressable>
    </Animated.View>
  );
}
