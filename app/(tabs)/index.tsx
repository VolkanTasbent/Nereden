import { useRouter } from 'expo-router';
import { useTranslation } from 'react-i18next';
import {
  ActivityIndicator,
  FlatList,
  Pressable,
  RefreshControl,
  View,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useState } from 'react';

import { Screen } from '@/components/layout/Screen';
import { CategoryFilter } from '@/components/ui/CategoryFilter';
import { ProductCard } from '@/components/ui/ProductCard';
import { Text } from '@/components/ui/Text';
import { useFeed } from '@/hooks/useProducts';
import { useTheme } from '@/hooks';
import type { ProductCategory } from '@/types';

export default function HomeScreen() {
  const { t } = useTranslation();
  const router = useRouter();
  const { theme } = useTheme();
  const [category, setCategory] = useState<ProductCategory | null>(null);
  const { data, isLoading, refetch, isRefetching, fetchNextPage, hasNextPage, isFetchingNextPage } =
    useFeed(category);

  const products = data?.pages.flatMap((p) => p.items) ?? [];

  const header = (
    <View className="mb-2">
      <View className="pt-2 pb-6">
        <Text variant="displayMd" className="mb-1">
          {t('home.title')}
        </Text>
        <Text variant="bodySm" color="secondary">
          {t('home.subtitle')}
        </Text>
      </View>

      <Pressable
        accessibilityLabel="Kamera ile fotoğraf çek"
        onPress={() => router.push('/(modals)/camera')}
        className="flex-row items-center gap-4 p-4 rounded-2xl mb-3"
        style={{
          backgroundColor: theme.colors.accent,
        }}
      >
        <View
          className="h-11 w-11 items-center justify-center rounded-full"
          style={{ backgroundColor: 'rgba(255,255,255,0.15)' }}
        >
          <Ionicons name="scan-outline" size={22} color={theme.colors.accentForeground} />
        </View>
        <View className="flex-1">
          <Text variant="titleSm" style={{ color: theme.colors.accentForeground }}>
            Fotoğraf ile bul
          </Text>
          <Text variant="caption" style={{ color: 'rgba(255,255,255,0.7)', marginTop: 2 }}>
            Çek veya galeriden seç
          </Text>
        </View>
        <Ionicons name="chevron-forward" size={18} color="rgba(255,255,255,0.6)" />
      </Pressable>

      <Pressable
        accessibilityLabel="Galeriden seç"
        onPress={() => router.push('/(modals)/gallery-picker')}
        className="flex-row items-center justify-center gap-2 py-3 mb-6"
      >
        <Ionicons name="images-outline" size={16} color={theme.colors.foregroundSecondary} />
        <Text variant="bodySm" color="secondary">
          Galeriden seç
        </Text>
      </Pressable>

      <Text variant="overline" color="secondary" className="mb-3">
        Keşfet
      </Text>
      <CategoryFilter selected={category} onSelect={setCategory} />
      <View className="h-5" />
    </View>
  );

  return (
    <Screen padded={false} className="px-5">
      <FlatList
        data={products}
        keyExtractor={(item) => item.id}
        numColumns={2}
        columnWrapperStyle={{ justifyContent: 'space-between' }}
        ListHeaderComponent={header}
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
            <ActivityIndicator color={theme.colors.foreground} className="my-12" />
          ) : (
            <Text variant="bodySm" color="secondary" className="text-center my-12">
              Henüz ürün yok. Fotoğraf çekerek başlayın!
            </Text>
          )
        }
        renderItem={({ item }) => <ProductCard product={item} />}
      />
    </Screen>
  );
}
