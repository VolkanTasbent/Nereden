import { appConfig } from '@/constants/config';
import { ApiClient } from '@/services/api/client';
import type { Product, ProductCategory } from '@/types';

import { mapApiProduct, type ApiProductSummary } from './mappers';

const apiClient = new ApiClient(appConfig.apiUrl);

export interface FeedParams {
  page?: number;
  limit?: number;
  category?: ProductCategory | null;
}

export interface SearchParams extends FeedParams {
  query: string;
}

export const productService = {
  async getProduct(id: string): Promise<Product> {
    const response = await apiClient.request<ApiProductSummary>(`/products/${id}`, {
      auth: true,
    });
    return mapApiProduct(response);
  },

  async getFeed(params: FeedParams = {}): Promise<{ items: Product[]; total: number; page: number }> {
    const page = params.page ?? 0;
    const limit = params.limit ?? 20;
    const category = params.category ? `&category=${params.category}` : '';

    const response = await apiClient.requestPaginated<ApiProductSummary[]>(
      `/products/feed?page=${page}&limit=${limit}${category}`,
      { auth: true },
    );

    return {
      items: response.data.map(mapApiProduct),
      total: response.meta?.total ?? response.data.length,
      page,
    };
  },

  async search(params: SearchParams): Promise<{ items: Product[]; total: number; page: number }> {
    const page = params.page ?? 0;
    const limit = params.limit ?? 20;
    const category = params.category ? `&category=${params.category}` : '';
    const q = encodeURIComponent(params.query);

    const response = await apiClient.requestPaginated<ApiProductSummary[]>(
      `/products/search?q=${q}&page=${page}&limit=${limit}${category}`,
      { auth: true },
    );

    return {
      items: response.data.map(mapApiProduct),
      total: response.meta?.total ?? response.data.length,
      page,
    };
  },
};
