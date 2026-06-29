import { useRouter } from 'expo-router';
import { Pressable, ScrollView, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useTranslation } from 'react-i18next';

import { Screen } from '@/components/layout/Screen';
import { Avatar } from '@/components/ui/Avatar';
import { Text } from '@/components/ui/Text';
import { appConfig } from '@/constants/config';
import { SignOutUseCase } from '@/features/auth/domain/use-cases/auth.use-cases';
import { authRepository } from '@/features/auth';
import { useAuth } from '@/hooks';
import { useTheme } from '@/hooks';

const signOutUseCase = new SignOutUseCase(authRepository);

interface MenuItem {
  icon: keyof typeof Ionicons.glyphMap;
  label: string;
  href: string;
}

export default function ProfileScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const { user, reset } = useAuth();
  const { theme } = useTheme();

  const menuItems: MenuItem[] = [
    { icon: 'time-outline', label: 'Geçmiş', href: '/history' },
    { icon: 'notifications-outline', label: 'Bildirimler', href: '/notifications' },
    { icon: 'settings-outline', label: t('settings.title'), href: '/settings' },
    { icon: 'diamond-outline', label: t('premium.title'), href: '/premium' },
  ];

  if (!appConfig.isProduction && user?.role === 'admin') {
    menuItems.push({ icon: 'shield-outline', label: 'Admin', href: '/admin' });
  }

  async function handleLogout() {
    await signOutUseCase.execute();
    reset();
    router.replace('/(auth)/login');
  }

  return (
    <Screen>
      <View className="pt-2 pb-6">
        <Text variant="titleLg">{t('tabs.profile')}</Text>
      </View>

      <ScrollView contentContainerStyle={{ paddingBottom: 100 }}>
        <View
          className="flex-row items-center gap-4 p-4 rounded-2xl mb-6"
          style={{
            backgroundColor: theme.colors.backgroundSecondary,
            borderWidth: 1,
            borderColor: theme.colors.border,
          }}
        >
          <Avatar name={user?.fullName} imageUrl={user?.avatarUrl} size="lg" />
          <View className="flex-1">
            <Text variant="titleMd">{user?.fullName ?? 'Kullanıcı'}</Text>
            <Text variant="bodySm" color="secondary" className="mt-0.5">
              {user?.email}
            </Text>
            {user?.isPremium ? (
              <Text variant="caption" color="secondary" className="mt-1">
                Premium
              </Text>
            ) : null}
          </View>
        </View>

        <View
          className="rounded-2xl overflow-hidden mb-6"
          style={{
            backgroundColor: theme.colors.backgroundSecondary,
            borderWidth: 1,
            borderColor: theme.colors.border,
          }}
        >
          {menuItems.map((item, index) => (
            <Pressable
              key={item.href}
              onPress={() => router.push(item.href as never)}
              className="flex-row items-center gap-3 px-4 py-3.5"
              style={{
                borderBottomWidth: index < menuItems.length - 1 ? 1 : 0,
                borderBottomColor: theme.colors.border,
              }}
            >
              <Ionicons name={item.icon} size={20} color={theme.colors.foregroundSecondary} />
              <Text variant="body" className="flex-1">
                {item.label}
              </Text>
              <Ionicons name="chevron-forward" size={16} color={theme.colors.foregroundTertiary} />
            </Pressable>
          ))}
        </View>

        <Pressable
          onPress={() => void handleLogout()}
          className="flex-row items-center justify-center gap-2 py-3.5 rounded-full"
          style={{
            borderWidth: 1,
            borderColor: theme.colors.border,
            backgroundColor: theme.colors.backgroundSecondary,
          }}
        >
          <Text variant="bodySm" color="secondary">
            {t('auth.logout')}
          </Text>
        </Pressable>
      </ScrollView>
    </Screen>
  );
}
