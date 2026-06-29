import '../global.css';

import {
  Inter_400Regular,
  Inter_500Medium,
  Inter_600SemiBold,
  Inter_700Bold,
  useFonts,
} from '@expo-google-fonts/inter';
import { Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import { useEffect } from 'react';
import { ActivityIndicator, View } from 'react-native';
import * as SplashScreen from 'expo-splash-screen';

import { Toast } from '@/components/feedback/Toast';
import { useAuthBootstrap } from '@/hooks/useAuthBootstrap';
import { useTheme } from '@/hooks';
import { AppProviders } from '@/providers';
import { useAppStore } from '@/store';

SplashScreen.preventAutoHideAsync().catch(() => undefined);

function RootNavigation() {
  const { isDark, theme } = useTheme();
  const setAppReady = useAppStore((state) => state.setAppReady);
  useAuthBootstrap();

  const [fontsLoaded] = useFonts({
    'Inter-Regular': Inter_400Regular,
    'Inter-Medium': Inter_500Medium,
    'Inter-SemiBold': Inter_600SemiBold,
    'Inter-Bold': Inter_700Bold,
  });

  useEffect(() => {
    if (fontsLoaded) {
      setAppReady(true);
      void SplashScreen.hideAsync();
    }
  }, [fontsLoaded, setAppReady]);

  if (!fontsLoaded) {
    return (
      <View className="flex-1 items-center justify-center" style={{ backgroundColor: theme.colors.background }}>
        <ActivityIndicator color={theme.colors.accent} />
      </View>
    );
  }

  return (
    <>
      <StatusBar style={isDark ? 'light' : 'dark'} />
      <Stack screenOptions={{ headerShown: false, animation: 'fade' }}>
        <Stack.Screen name="index" />
        <Stack.Screen name="(auth)" />
        <Stack.Screen name="(tabs)" />
        <Stack.Screen
          name="(modals)"
          options={{ presentation: 'fullScreenModal', animation: 'slide_from_bottom' }}
        />
        <Stack.Screen name="analysis/[id]" options={{ animation: 'slide_from_right' }} />
        <Stack.Screen name="product/[id]" options={{ animation: 'slide_from_right' }} />
        <Stack.Screen name="history/index" options={{ animation: 'slide_from_right' }} />
        <Stack.Screen name="notifications/index" options={{ animation: 'slide_from_right' }} />
        <Stack.Screen name="settings/index" options={{ animation: 'slide_from_right' }} />
        <Stack.Screen name="premium/index" options={{ animation: 'slide_from_right' }} />
        <Stack.Screen name="admin" options={{ animation: 'slide_from_right' }} />
      </Stack>
      <Toast />
    </>
  );
}

export default function RootLayout() {
  return (
    <AppProviders>
      <RootNavigation />
    </AppProviders>
  );
}
