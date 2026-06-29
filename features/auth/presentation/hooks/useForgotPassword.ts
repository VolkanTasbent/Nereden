import { useState } from 'react';
import { useRouter } from 'expo-router';

import { authRepository } from '@/features/auth';
import { useToastStore } from '@/store/toast.store';
import { getErrorMessage } from '@/utils/errors';
import { emailSchema } from '@/utils/validation';

export function useForgotPassword() {
  const router = useRouter();
  const showToast = useToastStore((s) => s.show);
  const [loading, setLoading] = useState(false);
  const [emailError, setEmailError] = useState<string | undefined>();

  async function submit(email: string) {
    const parsed = emailSchema.safeParse(email);
    if (!parsed.success) {
      setEmailError(parsed.error.errors[0]?.message);
      return;
    }

    setEmailError(undefined);
    setLoading(true);
    try {
      await authRepository.resetPassword(parsed.data);
      showToast('Sıfırlama bağlantısı gönderildi.', 'success');
      router.back();
    } catch (error) {
      showToast(getErrorMessage(error), 'error');
    } finally {
      setLoading(false);
    }
  }

  return { submit, loading, emailError };
}
