import { Image } from 'expo-image';
import { useMemo } from 'react';
import { Linking, Pressable, ScrollView, Share, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';

import { Header } from '@/components/layout/Header';
import { Screen } from '@/components/layout/Screen';
import { Badge } from '@/components/ui/Badge';
import { Card } from '@/components/ui/Card';
import { Text } from '@/components/ui/Text';
import { useFavoriteStatus, useFavoriteToggle } from '@/hooks/useProducts';
import type { Product } from '@/types';
import { formatCurrency, formatPriceRange } from '@/utils/format';
import { isDirectProductUrl } from '@/utils/productUrl';
import { getCheapestPrice, sortStoresByPrice } from '@/utils/stores';
import { useTheme } from '@/hooks';
import { useToastStore } from '@/store/toast.store';

interface ProductResultViewProps {
  product: Product;
  similarProducts?: Product[];
  cheaperAlternatives?: Product[];
  estimatedPriceRange?: Product['priceRange'];
}

export function ProductResultView({
  product,
  similarProducts = [],
  cheaperAlternatives = [],
  estimatedPriceRange,
}: ProductResultViewProps) {
  const { theme } = useTheme();
  const showToast = useToastStore((s) => s.show);
  const { data: isFavorite = false } = useFavoriteStatus(product.id);
  const toggleFavorite = useFavoriteToggle(product.id);
  const sortedStores = useMemo(
    () => sortStoresByPrice(product.stores.filter((store) => isDirectProductUrl(store.url))),
    [product.stores],
  );
  const cheapestPrice = useMemo(() => getCheapestPrice(sortedStores), [sortedStores]);

  async function handleToggleFavorite() {
    try {
      await toggleFavorite.mutateAsync(isFavorite);
      showToast(isFavorite ? 'Favorilerden çıkarıldı' : 'Favorilere eklendi', 'success');
    } catch {
      showToast('İşlem başarısız oldu.', 'error');
    }
  }

  async function handleShare() {
    const priceText = product.priceRange
      ? formatPriceRange(product.priceRange.min, product.priceRange.max)
      : '';
    try {
      await Share.share({
        message: `${product.title}${product.brand ? ` — ${product.brand}` : ''}\n${priceText}\n\nNereden? ile bulundu`,
      });
    } catch {
      showToast('Paylaşım iptal edildi.', 'info');
    }
  }

  return (
    <Screen edges={['top', 'bottom']}>
      <Header
        title="Ürün Sonucu"
        showBack
        rightAction={
          <View className="flex-row gap-2">
            <Pressable
              accessibilityLabel="Paylaş"
              onPress={() => void handleShare()}
              className="h-10 w-10 items-center justify-center rounded-full"
              style={{ backgroundColor: theme.colors.backgroundSecondary }}
            >
              <Ionicons name="share-outline" size={20} color={theme.colors.foreground} />
            </Pressable>
            <Pressable
              accessibilityLabel={isFavorite ? 'Favorilerden çıkar' : 'Favorilere ekle'}
              onPress={() => void handleToggleFavorite()}
              className="h-10 w-10 items-center justify-center rounded-full"
              style={{ backgroundColor: theme.colors.backgroundSecondary }}
            >
              <Ionicons
                name={isFavorite ? 'heart' : 'heart-outline'}
                size={22}
                color={isFavorite ? theme.colors.error : theme.colors.foreground}
              />
            </Pressable>
          </View>
        }
      />
      <ScrollView showsVerticalScrollIndicator={false} contentContainerClassName="pb-8">
        <Card padding="none" variant="elevated" className="overflow-hidden mb-6">
          <Image
            source={{ uri: product.imageUrl }}
            style={{ width: '100%', height: 280 }}
            contentFit="cover"
          />
          <View className="p-4">
            {product.isExactMatch ? <Badge label="Aynı Ürün" variant="success" className="mb-2" /> : null}
            <Text variant="titleLg" className="mb-1">
              {product.title}
            </Text>
            {product.brand ? (
              <Text variant="bodySm" color="secondary" className="mb-2">
                {product.brand}
              </Text>
            ) : null}
            {product.priceRange ? (
              <Text variant="titleMd" color="accent">
                {formatPriceRange(product.priceRange.min, product.priceRange.max)}
              </Text>
            ) : null}
          </View>
        </Card>

        {estimatedPriceRange ? (
          <Section title="Tahmini Fiyat Aralığı">
            <Text variant="titleMd">
              {formatPriceRange(estimatedPriceRange.min, estimatedPriceRange.max)}
            </Text>
          </Section>
        ) : null}

        {sortedStores.length > 0 ? (
          <Section title={`Satıcılar (${sortedStores.length})`}>
            {sortedStores.map((store, index) => {
              const isCheapest =
                cheapestPrice !== null && store.price !== null && store.price === cheapestPrice;
              return (
                <Pressable
                  key={store.id}
                  onPress={() => void Linking.openURL(store.url)}
                  className="flex-row items-center py-3 border-b"
                  style={{
                    borderColor: theme.colors.border,
                    backgroundColor: isCheapest ? '#E8F8ED' : 'transparent',
                    marginHorizontal: isCheapest ? -12 : 0,
                    paddingHorizontal: isCheapest ? 12 : 0,
                    borderRadius: isCheapest ? 12 : 0,
                  }}
                >
                  <View
                    className="h-7 w-7 items-center justify-center rounded-full mr-3"
                    style={{
                      backgroundColor: isCheapest
                        ? theme.colors.success
                        : theme.colors.backgroundTertiary,
                    }}
                  >
                    <Text
                      variant="caption"
                      style={{
                        color: isCheapest ? '#fff' : theme.colors.foregroundSecondary,
                        fontWeight: '600',
                      }}
                    >
                      {index + 1}
                    </Text>
                  </View>
                  <View className="flex-row items-center gap-3 flex-1 pr-3">
                    <Ionicons
                      name="storefront-outline"
                      size={20}
                      color={theme.colors.foregroundSecondary}
                    />
                    <View className="flex-1">
                      <View className="flex-row items-center gap-2 flex-wrap">
                        <Text variant="body">{store.name}</Text>
                        {isCheapest ? <Badge label="En ucuz" variant="success" /> : null}
                      </View>
                      <Text variant="caption" color="secondary">
                        Doğrudan ürün sayfası
                      </Text>
                    </View>
                  </View>
                  {store.price !== null ? (
                    <Text
                      variant="titleSm"
                      style={{ color: isCheapest ? theme.colors.success : theme.colors.accent }}
                    >
                      {formatCurrency(store.price)}
                    </Text>
                  ) : (
                    <Text variant="caption" color="secondary">
                      Fiyat yok
                    </Text>
                  )}
                </Pressable>
              );
            })}
          </Section>
        ) : null}

        {similarProducts.length > 0 ? (
          <Section title="Benzer Ürünler">
            {similarProducts.map((item) => (
              <ProductRow key={item.id} product={item} />
            ))}
          </Section>
        ) : null}

        {cheaperAlternatives.length > 0 ? (
          <Section title="Daha Uygun Alternatifler">
            {cheaperAlternatives.map((item) => (
              <ProductRow key={item.id} product={item} />
            ))}
          </Section>
        ) : null}
      </ScrollView>
    </Screen>
  );
}

function Section({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <View className="mb-6">
      <Text variant="titleSm" className="mb-3">
        {title}
      </Text>
      <Card>{children}</Card>
    </View>
  );
}

function ProductRow({ product }: { product: Product }) {
  return (
    <View className="flex-row items-center gap-3 py-3 border-b border-border">
      <Image
        source={{ uri: product.imageUrl }}
        style={{ width: 56, height: 56, borderRadius: 12 }}
      />
      <View className="flex-1">
        <Text variant="bodySm" numberOfLines={2}>
          {product.title}
        </Text>
        {product.priceRange ? (
          <Text variant="caption" color="accent">
            {formatPriceRange(product.priceRange.min, product.priceRange.max)}
          </Text>
        ) : null}
      </View>
    </View>
  );
}
