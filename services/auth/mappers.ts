import type { UserRole } from '@/types';

export interface ApiUserResponse {
  id: string;
  email: string;
  fullName: string | null;
  avatarUrl: string | null;
  role: string;
  isPremium: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ApiAuthResponse {
  user: ApiUserResponse;
  tokens: {
    accessToken: string;
    refreshToken: string;
  };
}

export function mapApiUserToUser(apiUser: ApiUserResponse): import('@/types').User {
  const role = apiUser.role.toLowerCase() as UserRole;
  return {
    id: apiUser.id,
    email: apiUser.email,
    fullName: apiUser.fullName,
    avatarUrl: apiUser.avatarUrl,
    role: ['user', 'premium', 'admin'].includes(role) ? role : 'user',
    isPremium: apiUser.isPremium,
    createdAt: apiUser.createdAt,
    updatedAt: apiUser.updatedAt,
  };
}
