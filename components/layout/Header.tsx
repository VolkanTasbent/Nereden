import { View, Pressable } from 'react-native';
import { useRouter } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';

import { Text } from '@/components/ui/Text';
import { useTheme } from '@/hooks';
import { cn } from '@/utils/cn';

interface HeaderProps {
  title: string;
  subtitle?: string;
  showBack?: boolean;
  rightAction?: React.ReactNode;
  className?: string;
}

export function Header({
  title,
  subtitle,
  showBack = false,
  rightAction,
  className,
}: HeaderProps) {
  const router = useRouter();
  const { theme } = useTheme();

  return (
    <View className={cn('flex-row items-center justify-between pt-2 pb-5', className)}>
      <View className="flex-1 flex-row items-center gap-3">
        {showBack ? (
          <Pressable
            accessibilityRole="button"
            accessibilityLabel="Geri"
            onPress={() => router.back()}
            className="h-9 w-9 items-center justify-center rounded-full"
            style={{
              backgroundColor: theme.colors.backgroundSecondary,
              borderWidth: 1,
              borderColor: theme.colors.border,
            }}
          >
            <Ionicons name="chevron-back" size={20} color={theme.colors.foreground} />
          </Pressable>
        ) : null}
        <View className="flex-1">
          <Text variant="titleLg">{title}</Text>
          {subtitle ? (
            <Text variant="bodySm" color="secondary" className="mt-0.5">
              {subtitle}
            </Text>
          ) : null}
        </View>
      </View>
      {rightAction}
    </View>
  );
}
