import { ActivityIndicator, Modal, View } from 'react-native';

import { Text } from '@/components/ui/Text';
import { useTheme } from '@/hooks';

interface LoadingOverlayProps {
  visible: boolean;
  message?: string;
}

export function LoadingOverlay({ visible, message }: LoadingOverlayProps) {
  const { theme } = useTheme();

  return (
    <Modal transparent visible={visible} animationType="fade">
      <View className="flex-1 items-center justify-center bg-black/40 px-8">
        <View className="w-full max-w-sm items-center rounded-3xl bg-background p-8 dark:bg-background-dark-secondary">
          <ActivityIndicator size="large" color={theme.colors.accent} />
          {message ? (
            <Text variant="bodySm" color="secondary" className="mt-4 text-center">
              {message}
            </Text>
          ) : null}
        </View>
      </View>
    </Modal>
  );
}
