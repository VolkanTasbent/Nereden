import { ScrollView, Pressable } from 'react-native';

import { Text } from '@/components/ui/Text';
import { PRODUCT_CATEGORIES } from '@/constants/categories';
import type { ProductCategory } from '@/types';
import { useTheme } from '@/hooks';

interface CategoryFilterProps {
  selected: ProductCategory | null;
  onSelect: (category: ProductCategory | null) => void;
}

export function CategoryFilter({ selected, onSelect }: CategoryFilterProps) {
  const { theme } = useTheme();

  return (
    <ScrollView
      horizontal
      showsHorizontalScrollIndicator={false}
      contentContainerStyle={{ gap: 8, paddingVertical: 4 }}
    >
      {PRODUCT_CATEGORIES.map((cat) => {
        const isActive = selected === cat.id;
        return (
          <Pressable
            key={cat.label}
            onPress={() => onSelect(cat.id)}
            style={{
              paddingHorizontal: 14,
              paddingVertical: 8,
              borderRadius: 999,
              backgroundColor: isActive ? theme.colors.accent : theme.colors.backgroundSecondary,
              borderWidth: 1,
              borderColor: isActive ? theme.colors.accent : theme.colors.border,
            }}
          >
            <Text
              variant="caption"
              style={{
                color: isActive ? theme.colors.accentForeground : theme.colors.foregroundSecondary,
                fontFamily: 'Inter-Medium',
              }}
            >
              {cat.label}
            </Text>
          </Pressable>
        );
      })}
    </ScrollView>
  );
}
