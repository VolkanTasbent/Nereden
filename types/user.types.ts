export type UserRole = 'user' | 'premium' | 'admin';

export interface User {
  id: string;
  email: string;
  fullName: string | null;
  avatarUrl: string | null;
  role: UserRole;
  isPremium: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface UserProfile extends User {
  locale: string;
  notificationEnabled: boolean;
  searchCount: number;
  favoriteCount: number;
}
