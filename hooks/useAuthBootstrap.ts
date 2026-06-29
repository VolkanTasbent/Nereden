import { useEffect } from 'react';

import { authRepository } from '@/features/auth';
import { useAuthStore } from '@/store';

export function useAuthBootstrap() {
  const { setUser, setLoading } = useAuthStore();

  useEffect(() => {
    let mounted = true;

    async function bootstrap() {
      setLoading(true);
      try {
        const user = await authRepository.getCurrentUser();
        if (mounted) {
          setUser(user);
        }
      } catch {
        if (mounted) {
          setUser(null);
        }
      }
    }

    void bootstrap();

    return () => {
      mounted = false;
    };
  }, [setUser, setLoading]);
}
