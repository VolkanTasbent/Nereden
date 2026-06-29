import { useState } from 'react';
import { KeyboardAvoidingView, Platform, ScrollView, View } from 'react-native';

import { Header } from '@/components/layout/Header';
import { Screen } from '@/components/layout/Screen';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Text } from '@/components/ui/Text';
import { useForgotPassword } from '@/features/auth/presentation/hooks/useForgotPassword';

export function ForgotPasswordForm() {
  const { submit, loading, emailError } = useForgotPassword();
  const [email, setEmail] = useState('');

  return (
    <Screen>
      <Header title="Şifremi Unuttum" showBack />
      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        className="flex-1"
      >
        <ScrollView contentContainerClassName="py-6">
          <Text variant="body" color="secondary" className="mb-6">
            E-posta adresinize şifre sıfırlama bağlantısı göndereceğiz.
          </Text>
          <View className="gap-4">
            <View>
              <Input
                placeholder="E-posta"
                value={email}
                onChangeText={setEmail}
                autoCapitalize="none"
                keyboardType="email-address"
              />
              {emailError ? (
                <Text variant="caption" color="error" className="mt-1 ml-1">
                  {emailError}
                </Text>
              ) : null}
            </View>
            <Button
              label="Bağlantı Gönder"
              onPress={() => void submit(email)}
              loading={loading}
              fullWidth
            />
          </View>
        </ScrollView>
      </KeyboardAvoidingView>
    </Screen>
  );
}
