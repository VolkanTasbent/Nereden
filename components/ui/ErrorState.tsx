import { View } from 'react-native';

import { Button } from '@/components/ui/Button';
import { Text } from '@/components/ui/Text';

interface ErrorStateProps {
  title?: string;
  message: string;
  onRetry?: () => void;
}

export function ErrorState({
  title = 'Bir hata oluştu',
  message,
  onRetry,
}: ErrorStateProps) {
  return (
    <View
      accessibilityRole="alert"
      className="flex-1 items-center justify-center px-8 py-12"
    >
      <Text variant="titleMd" color="error" className="text-center mb-2">
        {title}
      </Text>
      <Text variant="bodySm" color="secondary" className="text-center mb-6">
        {message}
      </Text>
      {onRetry ? (
        <Button label="Tekrar Dene" variant="accent" onPress={onRetry} />
      ) : null}
    </View>
  );
}
