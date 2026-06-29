import { useInfiniteQuery, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { favoritesService } from '@/services/favorites/client';
import { historyService } from '@/services/history/client';
import { productService } from '@/services/product/client';
import type { ProductCategory } from '@/types';

export function useFeed(category: ProductCategory | null) {
  return useInfiniteQuery({
    queryKey: ['feed', category],
    queryFn: ({ pageParam = 0 }) =>
      productService.getFeed({ page: pageParam, limit: 12, category }),
    getNextPageParam: (lastPage, pages) => {
      const loaded = pages.reduce((sum, p) => sum + p.items.length, 0);
      return loaded < lastPage.total ? lastPage.page + 1 : undefined;
    },
    initialPageParam: 0,
  });
}

export function useSearch(query: string, category: ProductCategory | null) {
  return useInfiniteQuery({
    queryKey: ['search', query, category],
    queryFn: ({ pageParam = 0 }) =>
      productService.search({ query, page: pageParam, limit: 12, category }),
    getNextPageParam: (lastPage, pages) => {
      const loaded = pages.reduce((sum, p) => sum + p.items.length, 0);
      return loaded < lastPage.total ? lastPage.page + 1 : undefined;
    },
    initialPageParam: 0,
    enabled: query.length >= 2,
  });
}

export function useFavorites() {
  return useQuery({
    queryKey: ['favorites'],
    queryFn: () => favoritesService.getAll(),
  });
}

export function useFavoriteStatus(productId: string) {
  return useQuery({
    queryKey: ['favorite-status', productId],
    queryFn: () => favoritesService.isFavorite(productId),
    enabled: Boolean(productId),
  });
}

export function useFavoriteToggle(productId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (isFavorite: boolean) => {
      if (isFavorite) {
        await favoritesService.remove(productId);
      } else {
        await favoritesService.add(productId);
      }
    },
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['favorites'] });
      void queryClient.invalidateQueries({ queryKey: ['favorite-status', productId] });
    },
  });
}

export function useHistory() {
  return useQuery({
    queryKey: ['history'],
    queryFn: () => historyService.getAll(),
  });
}

export function useClearHistory() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => historyService.clear(),
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: ['history'] }),
  });
}

export function useRemoveHistoryItem() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => historyService.remove(id),
    onSuccess: () => void queryClient.invalidateQueries({ queryKey: ['history'] }),
  });
}
