export type ThemePreference = 'light' | 'dark' | 'system';

export interface AppSettings {
  theme: ThemePreference;
  locale: string;
  hapticsEnabled: boolean;
  notificationsEnabled: boolean;
}

export type NotificationType =
  | 'analysis_complete'
  | 'price_drop'
  | 'new_match'
  | 'system';

export interface AppNotification {
  id: string;
  userId: string;
  type: NotificationType;
  title: string;
  body: string;
  data: Record<string, string> | null;
  isRead: boolean;
  createdAt: string;
}

export interface SearchHistoryItem {
  id: string;
  userId: string;
  query: string | null;
  imageUrl: string | null;
  productId: string | null;
  createdAt: string;
}

export interface FavoriteItem {
  id: string;
  userId: string;
  productId: string;
  createdAt: string;
}
