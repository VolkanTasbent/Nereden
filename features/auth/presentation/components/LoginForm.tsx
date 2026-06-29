import { Link } from 'expo-router';
import { useState } from 'react';
import { KeyboardAvoidingView, Platform, Pressable, ScrollView, View } from 'react-native';
import Animated, { FadeInDown } from 'react-native-reanimated';

import { Screen } from '@/components/layout/Screen';
import { Button } from '@/components/ui/Button';
import { LabeledInput } from '@/components/ui/LabeledInput';
import { Text } from '@/components/ui/Text';
import { appConfig } from '@/constants/config';
import { AuthHero } from '@/features/auth/presentation/components/AuthHero';
import { useLogin } from '@/features/auth/presentation/hooks/useLogin';
import { useTheme } from '@/hooks';

export function LoginForm() {
  const { login, loading, errors } = useLogin();
  const { theme } = useTheme();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  return (
    <Screen padded={false}>
      <View
        pointerEvents="none"
        style={{
          position: 'absolute',
          top: -80,
          right: -60,
          width: 220,
          height: 220,
          borderRadius: 110,
          backgroundColor: theme.colors.backgroundTertiary,
          opacity: 0.7,
        }}
      />
      <View
        pointerEvents="none"
        style={{
          position: 'absolute',
          bottom: 120,
          left: -90,
          width: 180,
          height: 180,
          borderRadius: 90,
          backgroundColor: theme.colors.backgroundTertiary,
          opacity: 0.5,
        }}
      />

      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        className="flex-1"
      >
        <ScrollView
          contentContainerStyle={{
            flexGrow: 1,
            justifyContent: 'center',
            paddingHorizontal: 24,
            paddingVertical: 40,
          }}
          keyboardShouldPersistTaps="handled"
          showsVerticalScrollIndicator={false}
        >
          <AuthHero />

          <Animated.View
            entering={FadeInDown.duration(500).delay(100)}
            className="rounded-3xl p-5"
            style={{
              backgroundColor: theme.colors.backgroundSecondary,
              borderWidth: 1,
              borderColor: theme.colors.border,
              shadowColor: '#000',
              shadowOffset: { width: 0, height: 8 },
              shadowOpacity: 0.06,
              shadowRadius: 24,
              elevation: 4,
            }}
          >
            <Text variant="titleMd" className="mb-1">
              Giriş yap
            </Text>
            <Text variant="bodySm" color="secondary" className="mb-5">
              Hesabına erişmek için bilgilerini gir.
            </Text>

            <View className="gap-4">
              <LabeledInput
                label="E-posta"
                placeholder="ornek@email.com"
                value={email}
                onChangeText={setEmail}
                autoCapitalize="none"
                keyboardType="email-address"
                autoComplete="email"
                error={errors.email}
              />

              <LabeledInput
                label="Şifre"
                placeholder="••••••••"
                value={password}
                onChangeText={setPassword}
                secureTextEntry
                autoComplete="password"
                error={errors.password}
              />

              {!appConfig.isProduction ? (
                <Link href="/(auth)/forgot-password" asChild>
                  <Pressable className="self-end py-1">
                    <Text variant="bodySm" color="secondary">
                      Şifremi unuttum
                    </Text>
                  </Pressable>
                </Link>
              ) : null}

              <Button
                label="Giriş Yap"
                onPress={() => void login({ email, password })}
                loading={loading}
                fullWidth
                size="lg"
              />
            </View>
          </Animated.View>

          <Animated.View
            entering={FadeInDown.duration(500).delay(200)}
            className="flex-row items-center justify-center gap-1.5 mt-8"
          >
            <Text variant="bodySm" color="secondary">
              Hesabın yok mu?
            </Text>
            <Link href="/(auth)/register" asChild>
              <Pressable>
                <Text variant="bodySm" style={{ fontFamily: 'Inter-SemiBold' }}>
                  Kayıt ol
                </Text>
              </Pressable>
            </Link>
          </Animated.View>
        </ScrollView>
      </KeyboardAvoidingView>
    </Screen>
  );
}
