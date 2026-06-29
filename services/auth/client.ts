import type { User } from '@/types';
import { appConfig } from '@/constants/config';
import { ApiClient } from '@/services/api/client';

import { mapApiUserToUser, type ApiAuthResponse, type ApiUserResponse } from './mappers';

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
}

export interface AuthService {
  signIn(email: string, password: string): Promise<{ user: User; tokens: AuthTokens }>;
  signUp(email: string, password: string, fullName: string): Promise<{ user: User; tokens: AuthTokens }>;
  signOut(): Promise<void>;
  resetPassword(email: string): Promise<void>;
  getCurrentUser(): Promise<User>;
  refreshToken(refreshToken: string): Promise<{ user: User; tokens: AuthTokens }>;
}

const apiClient = new ApiClient(appConfig.apiUrl);

function mapAuthResponse(response: ApiAuthResponse): { user: User; tokens: AuthTokens } {
  return {
    user: mapApiUserToUser(response.user),
    tokens: response.tokens,
  };
}

export const authService: AuthService = {
  async signIn(email, password) {
    const response = await apiClient.request<ApiAuthResponse>('/auth/login', {
      method: 'POST',
      body: { email, password },
    });
    return mapAuthResponse(response);
  },

  async signUp(email, password, fullName) {
    const response = await apiClient.request<ApiAuthResponse>('/auth/register', {
      method: 'POST',
      body: { email, password, fullName },
    });
    return mapAuthResponse(response);
  },

  async signOut() {
    await apiClient.request('/auth/logout', {
      method: 'POST',
      auth: true,
    });
  },

  resetPassword: (email) =>
    apiClient.request('/auth/forgot-password', { method: 'POST', body: { email } }),

  async getCurrentUser() {
    const response = await apiClient.request<ApiUserResponse>('/auth/me', {
      auth: true,
    });
    return mapApiUserToUser(response);
  },

  async refreshToken(refreshToken) {
    const response = await apiClient.request<ApiAuthResponse>('/auth/refresh', {
      method: 'POST',
      body: { refreshToken },
    });
    return mapAuthResponse(response);
  },
};
