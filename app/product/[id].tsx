import { useLocalSearchParams } from 'expo-router';
import { useCallback, useEffect, useState } from 'react';
import { ActivityIndicator, View } from 'react-native';

import { ErrorState } from '@/components/ui/ErrorState';
import { ProductResultView } from '@/features/product/presentation/components/ProductResultView';
import { aiService } from '@/services/ai';
import { productService } from '@/services/product/client';
import type { Product } from '@/types';
import { useTheme } from '@/hooks';
import { getErrorMessage } from '@/utils/errors';

export default function ProductScreen() {
  const { id, analysisId } = useLocalSearchParams<{ id: string; analysisId?: string }>();
  const { theme } = useTheme();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [product, setProduct] = useState<Product | null>(null);
  const [similar, setSimilar] = useState<Product[]>([]);
  const [cheaper, setCheaper] = useState<Product[]>([]);
  const [estimatedRange, setEstimatedRange] = useState<Product['priceRange']>(null);

  const load = useCallback(async () => {
    if (!id) return;
    setLoading(true);
    setError(null);

    try {
      if (analysisId) {
        const result = await aiService.getAnalysisResult(analysisId);
        if (result.matches.exactMatch) {
          setProduct(result.matches.exactMatch);
        } else if (id !== 'new') {
          setProduct(await productService.getProduct(id));
        }
        setSimilar(result.matches.similarProducts);
        setCheaper(result.matches.cheaperAlternatives);
        setEstimatedRange(result.matches.estimatedPriceRange);
      } else {
        setProduct(await productService.getProduct(id));
      }
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [id, analysisId]);

  useEffect(() => {
    void load();
  }, [load]);

  if (loading) {
    return (
      <View className="flex-1 items-center justify-center" style={{ backgroundColor: theme.colors.background }}>
        <ActivityIndicator size="large" color={theme.colors.accent} />
      </View>
    );
  }

  if (error || !product) {
    return <ErrorState message={error ?? 'Ürün bulunamadı.'} onRetry={() => void load()} />;
  }

  return (
    <ProductResultView
      product={product}
      similarProducts={similar}
      cheaperAlternatives={cheaper}
      estimatedPriceRange={estimatedRange}
    />
  );
}
