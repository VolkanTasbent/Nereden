import { useState } from 'react';
import { useRouter } from 'expo-router';

import { SignUpUseCase } from '@/features/auth/domain/use-cases/auth.use-cases';
import { authRepository } from '@/features/auth';
import { useAuthStore } from '@/store';
import { useToastStore } from '@/store/toast.store';
import { getErrorMessage } from '@/utils/errors';
import type { RegisterFormData } from '@/utils/validation';
import { registerSchema } from '@/utils/validation';

const signUpUseCase = new SignUpUseCase(authRepository);

export function useRegister() {
  const router = useRouter();
  const setUser = useAuthStore((s) => s.setUser);
  const showToast = useToastStore((s) => s.show);
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<Partial<Record<keyof RegisterFormData, string>>>({});

  async function register(data: RegisterFormData) {
    const parsed = registerSchema.safeParse(data);
    if (!parsed.success) {
      const fieldErrors: Partial<Record<keyof RegisterFormData, string>> = {};
      parsed.error.errors.forEach((err) => {
        const field = err.path[0] as keyof RegisterFormData;
        fieldErrors[field] = err.message;
      });
      setErrors(fieldErrors);
      return;
    }

    setErrors({});
    setLoading(true);
    try {
      const user = await signUpUseCase.execute({
        email: parsed.data.email,
        password: parsed.data.password,
        fullName: parsed.data.fullName,
      });
      setUser(user);
      showToast('Hesabınız oluşturuldu!', 'success');
      router.replace('/(tabs)');
    } catch (error) {
      showToast(getErrorMessage(error), 'error');
    } finally {
      setLoading(false);
    }
  }

  return { register, loading, errors };
}
