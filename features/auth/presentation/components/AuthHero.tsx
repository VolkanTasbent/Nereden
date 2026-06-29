import { View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';

import { Text } from '@/components/ui/Text';
import { useTheme } from '@/hooks';

const FEATURES = [
  { icon: 'scan-outline' as const, label: 'Görsel arama' },
  { icon: 'pricetag-outline' as const, label: 'Fiyat kıyas' },
  { icon: 'sparkles-outline' as const, label: 'AI eşleşme' },
];

interface AuthHeroProps {
  title?: string;
  subtitle?: string;
}

export function AuthHero({
  title = 'Nereden?',
  subtitle = 'Fotoğraf çek, ürünü bul, en iyi fiyatı yakala.',
}: AuthHeroProps) {
  const { theme } = useTheme();

  return (
    <View className="items-center mb-8">
      <View
        className="h-16 w-16 items-center justify-center rounded-2xl mb-5"
        style={{ backgroundColor: theme.colors.accent }}
      >
        <Ionicons name="scan-outline" size={30} color={theme.colors.accentForeground} />
      </View>

      <Text variant="displayLg" className="text-center mb-2">
        {title}
      </Text>
      <Text variant="body" color="secondary" className="text-center px-4">
        {subtitle}
      </Text>

      <View className="flex-row flex-wrap justify-center gap-2 mt-6 px-2">
        {FEATURES.map((feature) => (
          <View
            key={feature.label}
            className="flex-row items-center gap-1.5 px-3 py-2 rounded-full"
            style={{
              backgroundColor: theme.colors.backgroundSecondary,
              borderWidth: 1,
              borderColor: theme.colors.border,
            }}
          >
            <Ionicons name={feature.icon} size={14} color={theme.colors.foregroundSecondary} />
            <Text variant="caption" color="secondary">
              {feature.label}
            </Text>
          </View>
        ))}
      </View>
    </View>
  );
}
