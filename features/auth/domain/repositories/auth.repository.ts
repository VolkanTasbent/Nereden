import type { User } from '@/types';

export interface AuthCredentials {
  email: string;
  password: string;
}

export interface RegisterData extends AuthCredentials {
  fullName: string;
}

export interface AuthRepository {
  signIn(credentials: AuthCredentials): Promise<User>;
  signUp(data: RegisterData): Promise<User>;
  signOut(): Promise<void>;
  resetPassword(email: string): Promise<void>;
  getCurrentUser(): Promise<User | null>;
}
