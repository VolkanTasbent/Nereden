import { Image } from 'expo-image';
import { View } from 'react-native';

import { Text } from '@/components/ui/Text';
import { useTheme } from '@/hooks';
import { cn } from '@/utils/cn';

interface AvatarProps {
  name?: string | null | undefined;
  imageUrl?: string | null | undefined;
  size?: 'sm' | 'md' | 'lg';
  className?: string;
}

const sizeMap = {
  sm: 36,
  md: 48,
  lg: 64,
};

export function Avatar({ name, imageUrl, size = 'md', className }: AvatarProps) {
  const { theme } = useTheme();
  const dimension = sizeMap[size];
  const initials = (name ?? '?')
    .split(' ')
    .map((part) => part[0])
    .join('')
    .slice(0, 2)
    .toUpperCase();

  if (imageUrl) {
    return (
      <Image
        source={{ uri: imageUrl }}
        style={{ width: dimension, height: dimension, borderRadius: dimension / 2 }}
        className={cn(className)}
        accessibilityLabel={name ?? 'Profil fotoğrafı'}
      />
    );
  }

  return (
    <View
      className={cn('items-center justify-center rounded-full', className)}
      style={{
        width: dimension,
        height: dimension,
        backgroundColor: theme.colors.backgroundTertiary,
        borderWidth: 1,
        borderColor: theme.colors.border,
      }}
    >
      <Text variant={size === 'lg' ? 'titleSm' : 'caption'} color="secondary">
        {initials}
      </Text>
    </View>
  );
}
