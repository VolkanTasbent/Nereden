import { Image } from 'expo-image';
import { useRouter } from 'expo-router';
import {
  ActivityIndicator,
  FlatList,
  Pressable,
  RefreshControl,
  View,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';

import { Header } from '@/components/layout/Header';
import { Screen } from '@/components/layout/Screen';
import { Button } from '@/components/ui/Button';
import { EmptyState } from '@/components/ui/EmptyState';
import { Text } from '@/components/ui/Text';
import { useClearHistory, useHistory, useRemoveHistoryItem } from '@/hooks/useProducts';
import { useTheme } from '@/hooks';
import { formatRelativeTime } from '@/utils/format';

export default function HistoryScreen() {
  const router = useRouter();
  const { theme } = useTheme();
  const { data, isLoading, refetch, isRefetching } = useHistory();
  const clearHistory = useClearHistory();
  const removeItem = useRemoveHistoryItem();

  return (
    <Screen padded={false} className="px-5">
      <Header title="Geçmiş" subtitle="Önceki aramalarınız" showBack />

      {data && data.length > 0 ? (
        <View className="mb-4">
          <Button
            label="Geçmişi Temizle"
            variant="ghost"
            onPress={() => void clearHistory.mutateAsync()}
            loading={clearHistory.isPending}
          />
        </View>
      ) : null}

      {isLoading ? (
        <ActivityIndicator color={theme.colors.accent} className="my-12" />
      ) : !data || data.length === 0 ? (
        <EmptyState
          title="Geçmiş boş"
          description="Yaptığınız aramalar ve analizler burada görünecek."
        />
      ) : (
        <FlatList
          data={data}
          keyExtractor={(item) => item.id}
          refreshControl={
            <RefreshControl refreshing={isRefetching} onRefresh={() => void refetch()} />
          }
          renderItem={({ item }) => (
            <Pressable
              onPress={() => {
                if (item.productId) {
                  router.push(`/product/${item.productId}`);
                }
              }}
              className="flex-row items-center gap-3 p-3 rounded-2xl mb-2"
              style={{ backgroundColor: theme.colors.backgroundSecondary }}
            >
              {item.imageUrl ? (
                <Image
                  source={{ uri: item.imageUrl }}
                  style={{ width: 48, height: 48, borderRadius: 10 }}
                />
              ) : (
                <View
                  className="w-12 h-12 items-center justify-center rounded-xl"
                  style={{ backgroundColor: theme.colors.accentMuted }}
                >
                  <Ionicons name="search" size={20} color={theme.colors.accent} />
                </View>
              )}
              <View className="flex-1">
                <Text variant="bodySm" numberOfLines={1}>
                  {item.query ?? item.productTitle ?? 'Görsel arama'}
                </Text>
                <Text variant="caption" color="secondary">
                  {formatRelativeTime(item.createdAt)}
                </Text>
              </View>
              <Pressable
                accessibilityLabel="Sil"
                onPress={() => void removeItem.mutateAsync(item.id)}
                hitSlop={8}
              >
                <Ionicons name="close-circle" size={22} color={theme.colors.foregroundTertiary} />
              </Pressable>
            </Pressable>
          )}
        />
      )}
    </Screen>
  );
}
