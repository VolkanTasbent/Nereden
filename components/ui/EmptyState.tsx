import { View } from 'react-native';

import { Button } from '@/components/ui/Button';
import { Text } from '@/components/ui/Text';

interface EmptyStateProps {
  title: string;
  description?: string;
  actionLabel?: string;
  onAction?: () => void;
}

export function EmptyState({
  title,
  description,
  actionLabel,
  onAction,
}: EmptyStateProps) {
  return (
    <View
      accessibilityRole="text"
      className="flex-1 items-center justify-center px-8 py-12"
    >
      <Text variant="titleMd" className="text-center mb-2">
        {title}
      </Text>
      {description ? (
        <Text variant="bodySm" color="secondary" className="text-center mb-6">
          {description}
        </Text>
      ) : null}
      {actionLabel && onAction ? (
        <Button label={actionLabel} variant="secondary" onPress={onAction} />
      ) : null}
    </View>
  );
}
