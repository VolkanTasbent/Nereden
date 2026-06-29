import { appConfig } from '@/constants/config';
import { clearTokens, getRefreshToken, saveTokens } from '@/utils/tokenStorage';

interface AuthTokens {
  accessToken: string;
  refreshToken: string;
}

const baseUrl = appConfig.apiUrl;

let refreshInFlight: Promise<AuthTokens | null> | null = null;

export async function tryRefreshTokens(): Promise<AuthTokens | null> {
  if (refreshInFlight) {
    return refreshInFlight;
  }

  refreshInFlight = (async () => {
    const refreshToken = await getRefreshToken();
    if (!refreshToken) {
      return null;
    }

    try {
      const response = await fetch(`${baseUrl}/auth/refresh`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken }),
      });

      if (!response.ok) {
        return null;
      }

      const payload = (await response.json()) as {
        data: { tokens: AuthTokens };
      };

      await saveTokens(payload.data.tokens);
      return payload.data.tokens;
    } catch {
      return null;
    } finally {
      refreshInFlight = null;
    }
  })();

  return refreshInFlight;
}

export async function clearSession(): Promise<void> {
  await clearTokens();
  const { useAuthStore } = await import('@/store/auth.store');
  useAuthStore.getState().reset();
}
