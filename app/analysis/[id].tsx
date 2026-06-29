import { useLocalSearchParams, useRouter } from 'expo-router';
import { useEffect, useState } from 'react';
import { Image } from 'expo-image';
import { View } from 'react-native';
import Animated, {
  FadeIn,
  FadeInDown,
  useAnimatedStyle,
  useSharedValue,
  withRepeat,
  withSequence,
  withTiming,
} from 'react-native-reanimated';
import { useTranslation } from 'react-i18next';
import { Ionicons } from '@expo/vector-icons';

import { Screen } from '@/components/layout/Screen';
import { Button } from '@/components/ui/Button';
import { Text } from '@/components/ui/Text';
import { aiService } from '@/services/ai';
import { useTheme } from '@/hooks';
import { getErrorMessage, AuthError } from '@/utils/errors';
import { useToastStore } from '@/store/toast.store';

const STEPS = [
  { key: 'processing', label: 'Görsel analiz ediliyor', icon: 'scan-outline' as const },
  { key: 'matching', label: 'Benzer ürünler aranıyor', icon: 'search-outline' as const },
  { key: 'pricing', label: 'Fiyatlar karşılaştırılıyor', icon: 'pricetag-outline' as const },
];

export default function AnalysisScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const { t } = useTranslation();
  const router = useRouter();
  const { theme } = useTheme();
  const showToast = useToastStore((s) => s.show);
  const [status, setStatus] = useState('pending');
  const [activeStep, setActiveStep] = useState(0);
  const [imageUrl, setImageUrl] = useState<string | null>(null);
  const [failed, setFailed] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const pulse = useSharedValue(1);

  useEffect(() => {
    pulse.value = withRepeat(
      withSequence(withTiming(1.08, { duration: 700 }), withTiming(1, { duration: 700 })),
      -1,
      false,
    );
  }, [pulse]);

  useEffect(() => {
    if (!id) return;

    let active = true;
    let stepTimer = 0;

    const interval = setInterval(async () => {
      try {
        const request = await aiService.getAnalysisStatus(id);
        if (!active) return;

        setStatus(request.status);
        setImageUrl(request.imageUrl);

        if (request.status === 'processing' || request.status === 'pending') {
          stepTimer += 1;
          setActiveStep(Math.min(stepTimer % STEPS.length, STEPS.length - 1));
        }

        if (request.status === 'completed') {
          clearInterval(interval);
          const result = await aiService.getAnalysisResult(id);
          const productId = result.matches.exactMatch?.id;
          router.replace(
            productId ? `/product/${productId}?analysisId=${id}` : `/product/new?analysisId=${id}`,
          );
        }

        if (request.status === 'failed') {
          clearInterval(interval);
          setFailed(true);
          setErrorMessage(
            request.errorMessage ?? 'Analiz başarısız oldu. Tekrar deneyebilirsiniz.',
          );
        }
      } catch (error) {
        if (active && status === 'completed') return;
        if (active) {
          setFailed(true);
          setErrorMessage(getErrorMessage(error));
          if (error instanceof AuthError) {
            router.replace('/(auth)/login');
          }
        }
      }
    }, 800);

    return () => {
      active = false;
      clearInterval(interval);
    };
  }, [id, router, status]);

  async function handleRetry() {
    if (!id) return;
    setFailed(false);
    setErrorMessage(null);
    setStatus('pending');
    try {
      await aiService.retryAnalysis(id);
      showToast('Analiz yeniden başlatıldı.', 'info');
    } catch (error) {
      showToast(getErrorMessage(error), 'error');
    }
  }

  const pulseStyle = useAnimatedStyle(() => ({
    transform: [{ scale: pulse.value }],
  }));

  if (failed) {
    return (
      <Screen className="justify-center px-8">
        <View className="items-center gap-4">
          <Ionicons name="alert-circle-outline" size={64} color={theme.colors.error} />
          <Text variant="titleMd" className="text-center">
            Analiz tamamlanamadı
          </Text>
          <Text variant="bodySm" color="secondary" className="text-center">
            {errorMessage}
          </Text>
          <Button label="Tekrar Dene" onPress={() => void handleRetry()} fullWidth />
          <Button label="Geri Dön" variant="ghost" onPress={() => router.back()} fullWidth />
        </View>
      </Screen>
    );
  }

  return (
    <Screen>
      <View className="flex-1 items-center justify-center px-6">
        {imageUrl ? (
          <Animated.View entering={FadeIn.duration(400)} className="mb-8">
            <Image
              source={{ uri: imageUrl }}
              style={{ width: 160, height: 160, borderRadius: 24 }}
              contentFit="cover"
            />
            <Animated.View
              style={[
                pulseStyle,
                {
                  position: 'absolute',
                  inset: -8,
                  borderRadius: 32,
                  borderWidth: 2,
                  borderColor: theme.colors.accent,
                  opacity: 0.4,
                },
              ]}
            />
          </Animated.View>
        ) : (
          <Animated.View
            style={[
              pulseStyle,
              {
                width: 120,
                height: 120,
                borderRadius: 60,
                backgroundColor: theme.colors.accentMuted,
                marginBottom: 32,
              },
            ]}
          />
        )}

        <Text variant="titleLg" className="text-center mb-2">
          {t('analysis.title')}
        </Text>
        <Text variant="body" color="secondary" className="text-center mb-10">
          {t('analysis.subtitle')}
        </Text>

        <View className="w-full gap-4">
          {STEPS.map((step, index) => {
            const isActive = index === activeStep;
            const isDone = index < activeStep || status === 'completed';
            return (
              <Animated.View
                key={step.key}
                entering={FadeInDown.delay(index * 120).duration(400)}
                className="flex-row items-center gap-3 p-3.5 rounded-xl"
                style={{
                  backgroundColor: theme.colors.backgroundSecondary,
                  borderWidth: 1,
                  borderColor: isActive ? theme.colors.foreground : theme.colors.border,
                }}
              >
                <Ionicons
                  name={isDone ? 'checkmark-circle' : step.icon}
                  size={20}
                  color={isDone ? theme.colors.success : isActive ? theme.colors.foreground : theme.colors.foregroundTertiary}
                />
                <Text variant="bodySm" color={isActive ? 'primary' : 'secondary'}>
                  {step.label}
                </Text>
              </Animated.View>
            );
          })}
        </View>
      </View>
    </Screen>
  );
}
