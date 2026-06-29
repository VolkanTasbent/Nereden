import { appConfig } from '@/constants/config';
import { ApiClient } from '@/services/api/client';

const apiClient = new ApiClient(appConfig.apiUrl);

export interface HistoryItem {
  id: string;
  query: string | null;
  imageUrl: string | null;
  productId: string | null;
  productTitle: string | null;
  createdAt: string;
}

export const historyService = {
  async getAll(): Promise<HistoryItem[]> {
    return apiClient.request<HistoryItem[]>('/history', {
      auth: true,
    });
  },

  async saveTextSearch(query: string): Promise<void> {
    await apiClient.request('/history', {
      method: 'POST',
      auth: true,
      body: { query },
    });
  },

  async clear(): Promise<void> {
    await apiClient.request('/history', {
      method: 'DELETE',
      auth: true,
    });
  },

  async remove(id: string): Promise<void> {
    await apiClient.request(`/history/${id}`, {
      method: 'DELETE',
      auth: true,
    });
  },
};
