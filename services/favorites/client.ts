import { appConfig } from '@/constants/config';
import { ApiClient } from '@/services/api/client';
import type { Product } from '@/types';
import { mapApiProduct, type ApiProductSummary } from '@/services/product/mappers';

const apiClient = new ApiClient(appConfig.apiUrl);

export interface FavoriteItem {
  id: string;
  productId: string;
  product: Product;
  createdAt: string;
}

export const favoritesService = {
  async getAll(): Promise<FavoriteItem[]> {
    const response = await apiClient.request<Array<{
      id: string;
      productId: string;
      product: ApiProductSummary;
      createdAt: string;
    }>>('/favorites', { auth: true });

    return response.map((f) => ({
      id: f.id,
      productId: f.productId,
      product: mapApiProduct(f.product),
      createdAt: f.createdAt,
    }));
  },

  async add(productId: string): Promise<void> {
    await apiClient.request(`/favorites/${productId}`, {
      method: 'POST',
      auth: true,
    });
  },

  async remove(productId: string): Promise<void> {
    await apiClient.request(`/favorites/${productId}`, {
      method: 'DELETE',
      auth: true,
    });
  },

  async isFavorite(productId: string): Promise<boolean> {
    return apiClient.request<boolean>(`/favorites/${productId}/status`, {
      auth: true,
    });
  },
};
