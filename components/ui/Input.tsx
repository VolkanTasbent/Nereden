import { TextInput, type TextInputProps } from 'react-native';

import { useTheme } from '@/hooks';
import { cn } from '@/utils/cn';

interface InputProps extends TextInputProps {
  error?: string | undefined;
  className?: string;
}

export function Input({ error, className, style, ...props }: InputProps) {
  const { theme } = useTheme();

  return (
    <TextInput
      accessibilityHint={error}
      placeholderTextColor={theme.colors.foregroundTertiary}
      className={cn('h-11 rounded-xl px-4 text-body border', className)}
      style={[
        {
          color: theme.colors.foreground,
          backgroundColor: theme.colors.backgroundSecondary,
          borderColor: error ? theme.colors.error : theme.colors.border,
        },
        style,
      ]}
      {...props}
    />
  );
}
