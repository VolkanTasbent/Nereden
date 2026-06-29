import { Link } from 'expo-router';
import { useState } from 'react';
import { KeyboardAvoidingView, Platform, ScrollView, View } from 'react-native';

import { Header } from '@/components/layout/Header';
import { Screen } from '@/components/layout/Screen';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Text } from '@/components/ui/Text';
import { useRegister } from '@/features/auth/presentation/hooks/useRegister';

export function RegisterForm() {
  const { register, loading, errors } = useRegister();
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  return (
    <Screen>
      <Header title="Kayıt Ol" showBack />
      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        className="flex-1"
      >
        <ScrollView
          contentContainerClassName="py-6"
          keyboardShouldPersistTaps="handled"
        >
          <View className="gap-4">
            <View>
              <Input placeholder="Ad Soyad" value={fullName} onChangeText={setFullName} />
              {errors.fullName ? (
                <Text variant="caption" color="error" className="mt-1 ml-1">
                  {errors.fullName}
                </Text>
              ) : null}
            </View>
            <View>
              <Input
                placeholder="E-posta"
                value={email}
                onChangeText={setEmail}
                autoCapitalize="none"
                keyboardType="email-address"
              />
              {errors.email ? (
                <Text variant="caption" color="error" className="mt-1 ml-1">
                  {errors.email}
                </Text>
              ) : null}
            </View>
            <View>
              <Input
                placeholder="Şifre"
                value={password}
                onChangeText={setPassword}
                secureTextEntry
              />
              {errors.password ? (
                <Text variant="caption" color="error" className="mt-1 ml-1">
                  {errors.password}
                </Text>
              ) : null}
            </View>
            <View>
              <Input
                placeholder="Şifre Tekrar"
                value={confirmPassword}
                onChangeText={setConfirmPassword}
                secureTextEntry
              />
              {errors.confirmPassword ? (
                <Text variant="caption" color="error" className="mt-1 ml-1">
                  {errors.confirmPassword}
                </Text>
              ) : null}
            </View>

            <View className="pt-2">
              <Button
                label="Kayıt Ol"
                onPress={() =>
                  void register({ fullName, email, password, confirmPassword })
                }
                loading={loading}
                fullWidth
                size="lg"
              />
            </View>

            <View className="flex-row items-center justify-center gap-1.5 mt-4">
              <Text variant="bodySm" color="secondary">
                Zaten hesabınız var mı?
              </Text>
              <Link href="/(auth)/login" asChild>
                <Text variant="bodySm" style={{ fontFamily: 'Inter-SemiBold' }}>
                  Giriş Yap
                </Text>
              </Link>
            </View>
          </View>
        </ScrollView>
      </KeyboardAvoidingView>
    </Screen>
  );
}
