import { useState } from 'react';
import { Pressable, TextInput, View, type TextInputProps } from 'react-native';
import { Ionicons } from '@expo/vector-icons';

import { Text } from '@/components/ui/Text';
import { useTheme } from '@/hooks';
import { cn } from '@/utils/cn';

interface LabeledInputProps extends TextInputProps {
  label: string;
  error?: string | undefined;
  className?: string;
}

export function LabeledInput({
  label,
  error,
  className,
  secureTextEntry,
  style,
  ...props
}: LabeledInputProps) {
  const { theme } = useTheme();
  const [hidden, setHidden] = useState(Boolean(secureTextEntry));
  const isSecure = Boolean(secureTextEntry);

  return (
    <View className={cn('gap-1.5', className)}>
      <Text variant="caption" color="secondary" style={{ fontFamily: 'Inter-Medium' }}>
        {label}
      </Text>
      <View className="relative">
        <TextInput
          accessibilityHint={error}
          placeholderTextColor={theme.colors.foregroundTertiary}
          secureTextEntry={isSecure && hidden}
          className="h-12 rounded-xl px-4 text-body border"
          style={[
            {
              color: theme.colors.foreground,
              backgroundColor: theme.colors.backgroundTertiary,
              borderColor: error ? theme.colors.error : 'transparent',
              paddingRight: isSecure ? 44 : 16,
            },
            style,
          ]}
          {...props}
        />
        {isSecure ? (
          <Pressable
            accessibilityRole="button"
            accessibilityLabel={hidden ? 'Şifreyi göster' : 'Şifreyi gizle'}
            onPress={() => setHidden((value) => !value)}
            className="absolute right-0 top-0 h-12 w-11 items-center justify-center"
          >
            <Ionicons
              name={hidden ? 'eye-outline' : 'eye-off-outline'}
              size={18}
              color={theme.colors.foregroundTertiary}
            />
          </Pressable>
        ) : null}
      </View>
      {error ? (
        <Text variant="caption" color="error">
          {error}
        </Text>
      ) : null}
    </View>
  );
}
