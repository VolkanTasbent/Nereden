import { useTranslation } from 'react-i18next';
import {
  ActivityIndicator,
  FlatList,
  RefreshControl,
  View,
} from 'react-native';

import { Header } from '@/components/layout/Header';
import { Screen } from '@/components/layout/Screen';
import { EmptyState } from '@/components/ui/EmptyState';
import { ProductCard } from '@/components/ui/ProductCard';
import { useFavorites } from '@/hooks/useProducts';
import { useTheme } from '@/hooks';

export default function FavoritesScreen() {
  const { t } = useTranslation();
  const { theme } = useTheme();
  const { data, isLoading, refetch, isRefetching } = useFavorites();

  const products = data?.map((f) => f.product) ?? [];

  return (
    <Screen padded={false} className="px-5">
      <View className="mb-4">
        <Header title={t('tabs.favorites')} subtitle="Kaydettiğiniz ürünler" />
      </View>

      {isLoading ? (
        <ActivityIndicator color={theme.colors.foreground} className="my-12" />
      ) : products.length === 0 ? (
        <EmptyState
          title="Henüz favori yok"
          description="Beğendiğiniz ürünleri favorilere ekleyin."
        />
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
          renderItem={({ item }) => <ProductCard product={item} />}
        />
      )}
    </Screen>
  );
}
