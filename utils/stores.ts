import type { Store } from '@/types';

export function sortStoresByPrice(stores: Store[]): Store[] {
  return [...stores].sort((a, b) => {
    if (a.price === null && b.price === null) {
      return a.name.localeCompare(b.name, 'tr');
    }
    if (a.price === null) return 1;
    if (b.price === null) return -1;
    if (a.price !== b.price) return a.price - b.price;
    return a.name.localeCompare(b.name, 'tr');
  });
}

export function getCheapestPrice(stores: Store[]): number | null {
  const priced = stores.filter((s) => s.price !== null);
  if (priced.length === 0) return null;
  return Math.min(...priced.map((s) => s.price as number));
}
