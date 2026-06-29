import { Image } from 'expo-image';
import { useRouter } from 'expo-router';
import { Pressable, View } from 'react-native';

import { Text } from '@/components/ui/Text';
import type { Product } from '@/types';
import { formatPriceRange } from '@/utils/format';
import { useTheme } from '@/hooks';

interface ProductCardProps {
  product: Product;
  variant?: 'grid' | 'list';
}

export function ProductCard({ product, variant = 'grid' }: ProductCardProps) {
  const router = useRouter();
  const { theme } = useTheme();

  if (variant === 'list') {
    return (
      <Pressable
        onPress={() => router.push(`/product/${product.id}`)}
        className="flex-row gap-3 p-3 rounded-2xl mb-2"
        style={{
          backgroundColor: theme.colors.backgroundSecondary,
          borderWidth: 1,
          borderColor: theme.colors.border,
        }}
      >
        <Image
          source={{ uri: product.imageUrl }}
          style={{ width: 64, height: 64, borderRadius: 12 }}
          contentFit="cover"
        />
        <View className="flex-1 justify-center">
          <Text variant="bodySm" numberOfLines={2}>
            {product.title}
          </Text>
          {product.priceRange ? (
            <Text variant="caption" className="mt-1" style={{ color: theme.colors.foreground }}>
              {formatPriceRange(product.priceRange.min, product.priceRange.max)}
            </Text>
          ) : null}
        </View>
      </Pressable>
    );
  }

  return (
    <Pressable
      onPress={() => router.push(`/product/${product.id}`)}
      className="flex-1 mb-5"
      style={{ maxWidth: '48%' }}
    >
      <View
        className="rounded-2xl overflow-hidden mb-2.5"
        style={{
          backgroundColor: theme.colors.backgroundSecondary,
          borderWidth: 1,
          borderColor: theme.colors.border,
        }}
      >
        <Image
          source={{ uri: product.imageUrl }}
          style={{ width: '100%', height: 168 }}
          contentFit="cover"
        />
      </View>
      <Text variant="bodySm" numberOfLines={2} className="mb-0.5">
        {product.title}
      </Text>
      {product.brand ? (
        <Text variant="caption" color="secondary" numberOfLines={1}>
          {product.brand}
        </Text>
      ) : null}
      {product.priceRange ? (
        <Text variant="caption" className="mt-1" style={{ color: theme.colors.foreground }}>
          {formatPriceRange(product.priceRange.min, product.priceRange.max)}
        </Text>
      ) : null}
    </Pressable>
  );
}
