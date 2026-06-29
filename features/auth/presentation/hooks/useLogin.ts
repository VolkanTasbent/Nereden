import { useState } from 'react';
import { useRouter } from 'expo-router';

import { SignInUseCase } from '@/features/auth/domain/use-cases/auth.use-cases';
import { authRepository } from '@/features/auth';
import { useAuthStore } from '@/store';
import { useToastStore } from '@/store/toast.store';
import { getErrorMessage } from '@/utils/errors';
import type { LoginFormData } from '@/utils/validation';
import { loginSchema } from '@/utils/validation';

const signInUseCase = new SignInUseCase(authRepository);

export function useLogin() {
  const router = useRouter();
  const setUser = useAuthStore((s) => s.setUser);
  const showToast = useToastStore((s) => s.show);
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<Partial<Record<keyof LoginFormData, string>>>({});

  async function login(data: LoginFormData) {
    const parsed = loginSchema.safeParse(data);
    if (!parsed.success) {
      const fieldErrors: Partial<Record<keyof LoginFormData, string>> = {};
      parsed.error.errors.forEach((err) => {
        const field = err.path[0] as keyof LoginFormData;
        fieldErrors[field] = err.message;
      });
      setErrors(fieldErrors);
      return;
    }

    setErrors({});
    setLoading(true);
    try {
      const user = await signInUseCase.execute(parsed.data);
      setUser(user);
      showToast('Hoş geldiniz!', 'success');
      router.replace('/(tabs)');
    } catch (error) {
      showToast(getErrorMessage(error), 'error');
    } finally {
      setLoading(false);
    }
  }

  return { login, loading, errors };
}
