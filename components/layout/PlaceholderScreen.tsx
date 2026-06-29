import { View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useTranslation } from 'react-i18next';

import { Header } from '@/components/layout/Header';
import { Screen } from '@/components/layout/Screen';
import { Text } from '@/components/ui/Text';
import { useTheme } from '@/hooks';

interface PlaceholderScreenProps {
  title: string;
  subtitle?: string;
  showBack?: boolean;
}

export function PlaceholderScreen({
  title,
  subtitle,
  showBack = false,
}: PlaceholderScreenProps) {
  const { t } = useTranslation();
  const { theme } = useTheme();

  return (
    <Screen>
      <Header
        title={title}
        {...(subtitle !== undefined ? { subtitle } : {})}
        showBack={showBack}
      />
      <View className="flex-1 items-center justify-center gap-4 px-8">
        <View
          className="h-14 w-14 items-center justify-center rounded-2xl"
          style={{ backgroundColor: theme.colors.backgroundTertiary }}
        >
          <Ionicons name="time-outline" size={28} color={theme.colors.foregroundSecondary} />
        </View>
        <Text variant="titleMd" className="text-center">
          {t('common.comingSoon')}
        </Text>
        <Text variant="bodySm" color="secondary" className="text-center">
          {t('common.comingSoonDescription')}
        </Text>
      </View>
    </Screen>
  );
}
