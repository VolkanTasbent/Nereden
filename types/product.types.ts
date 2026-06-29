export type ProductCategory =
  | 'fashion'
  | 'furniture'
  | 'electronics'
  | 'decoration'
  | 'accessories'
  | 'other';

export interface PriceRange {
  min: number;
  max: number;
  currency: 'TRY';
}

export interface Store {
  id: string;
  name: string;
  url: string;
  logoUrl: string | null;
  price: number | null;
  currency: 'TRY';
  inStock: boolean;
}

export interface Product {
  id: string;
  title: string;
  description: string | null;
  imageUrl: string;
  category: ProductCategory;
  priceRange: PriceRange | null;
  stores: Store[];
  similarityScore: number | null;
  isExactMatch: boolean;
  brand: string | null;
  createdAt: string;
}

export interface ProductMatch {
  exactMatch: Product | null;
  similarProducts: Product[];
  cheaperAlternatives: Product[];
  estimatedPriceRange: PriceRange | null;
}
