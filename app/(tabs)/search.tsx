import { useTranslation } from 'react-i18next';
import { useCallback, useEffect, useState } from 'react';
import {
  ActivityIndicator,
  FlatList,
  RefreshControl,
  View,
} from 'react-native';

import { Header } from '@/components/layout/Header';
import { Screen } from '@/components/layout/Screen';
import { CategoryFilter } from '@/components/ui/CategoryFilter';
import { Input } from '@/components/ui/Input';
import { ProductCard } from '@/components/ui/ProductCard';
import { Text } from '@/components/ui/Text';
import { useSearch } from '@/hooks/useProducts';
import { historyService } from '@/services/history/client';
import { useTheme } from '@/hooks';
import type { ProductCategory } from '@/types';

export default function SearchScreen() {
  const { t } = useTranslation();
  const { theme } = useTheme();
  const [query, setQuery] = useState('');
  const [debouncedQuery, setDebouncedQuery] = useState('');
  const [category, setCategory] = useState<ProductCategory | null>(null);

  useEffect(() => {
    const timer = setTimeout(() => setDebouncedQuery(query.trim()), 400);
    return () => clearTimeout(timer);
  }, [query]);

  const { data, isLoading, refetch, isRefetching, fetchNextPage, hasNextPage, isFetchingNextPage } =
    useSearch(debouncedQuery, category);

  const products = data?.pages.flatMap((p) => p.items) ?? [];

  const saveSearch = useCallback(async () => {
    if (debouncedQuery.length >= 2) {
      await historyService.saveTextSearch(debouncedQuery);
    }
  }, [debouncedQuery]);

  useEffect(() => {
    if (debouncedQuery.length >= 2 && products.length > 0) {
      void saveSearch();
    }
  }, [debouncedQuery, products.length, saveSearch]);

  return (
    <Screen padded={false} className="px-5">
      <View className="mb-2">
        <Header title={t('tabs.search')} subtitle="Ürün ve kategori arayın" />
        <Input
          placeholder="Ne arıyorsunuz?"
          value={query}
          onChangeText={setQuery}
          autoCapitalize="none"
          returnKeyType="search"
        />
        <View className="mt-3">
          <CategoryFilter selected={category} onSelect={setCategory} />
        </View>
      </View>

      {debouncedQuery.length < 2 ? (
        <Text variant="bodySm" color="secondary" className="text-center mt-12">
          Aramaya başlamak için en az 2 karakter yazın.
        </Text>
      ) : (
        <FlatList
          data={products}
          keyExtractor={(item) => item.id}
          numColumns={2}
          columnWrapperStyle={{ justifyContent: 'space-between' }}
          contentContainerStyle={{ paddingBottom: 100 }}
          refreshControl={
            <RefreshControl refreshing={isRefetching} onRefresh={() => void refetch()} />
          }
          onEndReached={() => {
            if (hasNextPage && !isFetchingNextPage) void fetchNextPage();
          }}
          onEndReachedThreshold={0.4}
          ListFooterComponent={
            isFetchingNextPage ? (
              <ActivityIndicator color={theme.colors.foreground} className="my-4" />
            ) : null
          }
          ListEmptyComponent={
            isLoading ? (
              <ActivityIndicator color={theme.colors.accent} className="my-12" />
            ) : (
              <Text variant="bodySm" color="secondary" className="text-center my-12">
                Sonuç bulunamadı.
              </Text>
            )
          }
          renderItem={({ item }) => <ProductCard product={item} />}
        />
      )}
    </Screen>
  );
}
