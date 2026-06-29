import { useAuthStore } from '@/store';

export function useAuth() {
  const { user, isAuthenticated, isLoading, setUser, setLoading, reset } =
    useAuthStore();

  return {
    user,
    isAuthenticated,
    isLoading,
    setUser,
    setLoading,
    reset,
  };
}
