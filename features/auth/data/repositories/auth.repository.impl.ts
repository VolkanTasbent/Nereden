import type { AuthRepository, AuthCredentials, RegisterData } from '../../domain/repositories/auth.repository';
import { authService } from '@/services/auth';
import { clearTokens, getAccessToken, getRefreshToken, saveTokens } from '@/utils/tokenStorage';
import type { User } from '@/types';

export class HttpAuthRepository implements AuthRepository {
  async signIn(credentials: AuthCredentials): Promise<User> {
    const { user, tokens } = await authService.signIn(credentials.email, credentials.password);
    await saveTokens(tokens);
    return user;
  }

  async signUp(data: RegisterData): Promise<User> {
    const { user, tokens } = await authService.signUp(data.email, data.password, data.fullName);
    await saveTokens(tokens);
    return user;
  }

  async signOut(): Promise<void> {
    try {
      await authService.signOut();
    } finally {
      await clearTokens();
    }
  }

  async resetPassword(email: string): Promise<void> {
    await authService.resetPassword(email);
  }

  async getCurrentUser(): Promise<User | null> {
    const hasSession = (await getAccessToken()) || (await getRefreshToken());
    if (!hasSession) {
      return null;
    }

    try {
      return await authService.getCurrentUser();
    } catch {
      await clearTokens();
      return null;
    }
  }
}

export const authRepository = new HttpAuthRepository();
