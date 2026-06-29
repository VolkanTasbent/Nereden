import type { Product, ProductCategory } from '@/types';
import { sortStoresByPrice } from '@/utils/stores';

export interface ApiProductSummary {
  id: string;
  title: string;
  description: string | null;
  imageUrl: string;
  category: string;
  priceRange: { min: number; max: number; currency: string } | null;
  stores: Array<{
    id: string;
    name: string;
    url: string;
    logoUrl: string | null;
    price: number | null;
    currency: string;
    inStock: boolean;
  }>;
  similarityScore?: number | null;
  exactMatch?: boolean;
  brand: string | null;
  createdAt: string;
}

export function mapApiProduct(raw: ApiProductSummary): Product {
  return {
    id: raw.id,
    title: raw.title,
    description: raw.description,
    imageUrl: raw.imageUrl,
    category: raw.category as ProductCategory,
    priceRange: raw.priceRange
      ? { min: Number(raw.priceRange.min), max: Number(raw.priceRange.max), currency: 'TRY' }
      : null,
    stores: sortStoresByPrice(
      (raw.stores ?? []).map((s) => ({
        id: s.id,
        name: s.name,
        url: s.url,
        logoUrl: s.logoUrl,
        price: s.price !== null ? Number(s.price) : null,
        currency: 'TRY' as const,
        inStock: s.inStock,
      })),
    ),
    similarityScore: raw.similarityScore ?? null,
    isExactMatch: raw.exactMatch ?? false,
    brand: raw.brand,
    createdAt: raw.createdAt,
  };
}
