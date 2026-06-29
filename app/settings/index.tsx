import { Linking, Pressable, ScrollView, View } from 'react-native';
import Constants from 'expo-constants';
import { Ionicons } from '@expo/vector-icons';
import { useTranslation } from 'react-i18next';

import { Header } from '@/components/layout/Header';
import { Screen } from '@/components/layout/Screen';
import { Text } from '@/components/ui/Text';
import { appConfig } from '@/constants/config';
import { useTheme } from '@/hooks';

interface SettingsRowProps {
  icon: keyof typeof Ionicons.glyphMap;
  label: string;
  value?: string;
  onPress?: () => void;
}

function SettingsRow({ icon, label, value, onPress }: SettingsRowProps) {
  const { theme } = useTheme();

  return (
    <Pressable
      onPress={onPress}
      disabled={!onPress}
      className="flex-row items-center gap-3 px-4 py-3.5"
    >
      <Ionicons name={icon} size={20} color={theme.colors.foregroundSecondary} />
      <Text variant="body" className="flex-1">
        {label}
      </Text>
      {value ? (
        <Text variant="bodySm" color="secondary">
          {value}
        </Text>
      ) : onPress ? (
        <Ionicons name="open-outline" size={16} color={theme.colors.foregroundTertiary} />
      ) : null}
    </Pressable>
  );
}

export default function SettingsScreen() {
  const { t } = useTranslation();
  const { theme } = useTheme();

  const version = Constants.expoConfig?.version ?? '1.0.0';
  const versionCode = Constants.expoConfig?.android?.versionCode;

  async function openUrl(url: string) {
    if (!url) return;
    const canOpen = await Linking.canOpenURL(url);
    if (canOpen) {
      await Linking.openURL(url);
    }
  }

  async function openEmail() {
    await Linking.openURL(`mailto:${appConfig.supportEmail}`);
  }

  return (
    <Screen>
      <Header title={t('settings.title')} showBack />
      <ScrollView contentContainerStyle={{ paddingBottom: 40 }}>
        <Text variant="overline" color="secondary" className="mb-2 px-1">
          {t('settings.legal')}
        </Text>
        <View
          className="rounded-2xl overflow-hidden mb-6"
          style={{
            backgroundColor: theme.colors.backgroundSecondary,
            borderWidth: 1,
            borderColor: theme.colors.border,
          }}
        >
          <SettingsRow
            icon="shield-checkmark-outline"
            label={t('settings.privacyPolicy')}
            onPress={() => void openUrl(appConfig.privacyPolicyUrl)}
          />
          {appConfig.termsUrl ? (
            <View style={{ borderTopWidth: 1, borderTopColor: theme.colors.border }}>
              <SettingsRow
                icon="document-text-outline"
                label={t('settings.terms')}
                onPress={() => void openUrl(appConfig.termsUrl)}
              />
            </View>
          ) : null}
        </View>

        <Text variant="overline" color="secondary" className="mb-2 px-1">
          {t('settings.support')}
        </Text>
        <View
          className="rounded-2xl overflow-hidden mb-6"
          style={{
            backgroundColor: theme.colors.backgroundSecondary,
            borderWidth: 1,
            borderColor: theme.colors.border,
          }}
        >
          <SettingsRow
            icon="mail-outline"
            label={t('settings.contact')}
            value={appConfig.supportEmail}
            onPress={() => void openEmail()}
          />
        </View>

        <Text variant="overline" color="secondary" className="mb-2 px-1">
          {t('settings.about')}
        </Text>
        <View
          className="rounded-2xl px-4 py-3.5"
          style={{
            backgroundColor: theme.colors.backgroundSecondary,
            borderWidth: 1,
            borderColor: theme.colors.border,
          }}
        >
          <Text variant="bodySm" color="secondary">
            {t('settings.version', {
              version,
              build: versionCode ? String(versionCode) : '-',
            })}
          </Text>
        </View>
      </ScrollView>
    </Screen>
  );
}
