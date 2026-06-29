import { useRouter } from 'expo-router';
import { useRef, useState } from 'react';
import { ActivityIndicator, Pressable, View } from 'react-native';
import { CameraView, useCameraPermissions } from 'expo-camera';
import { Ionicons } from '@expo/vector-icons';

import { UploadProgress } from '@/components/feedback/UploadProgress';
import { Screen } from '@/components/layout/Screen';
import { Text } from '@/components/ui/Text';
import { useImageAnalysis } from '@/hooks/useImageAnalysis';
import { useTheme } from '@/hooks';

export default function CameraScreen() {
  const router = useRouter();
  const { theme } = useTheme();
  const [permission, requestPermission] = useCameraPermissions();
  const [capturing, setCapturing] = useState(false);
  const cameraRef = useRef<CameraView>(null);
  const { analyzeFromUri, step, progress } = useImageAnalysis();

  const isBusy = capturing || step !== 'idle';

  async function handleCapture() {
    if (!cameraRef.current || isBusy) return;

    setCapturing(true);
    try {
      const photo = await cameraRef.current.takePictureAsync({ quality: 0.9 });
      if (!photo?.uri) {
        throw new Error('Fotoğraf çekilemedi.');
      }
      await analyzeFromUri(photo.uri);
    } catch {
      // toast handled in hook
    } finally {
      setCapturing(false);
    }
  }

  const stepLabel =
    step === 'compressing'
      ? 'Görsel sıkıştırılıyor...'
      : step === 'uploading'
        ? 'Görsel yükleniyor...'
        : step === 'starting'
          ? 'Analiz başlatılıyor...'
          : '';

  if (!permission) {
    return (
      <Screen className="items-center justify-center">
        <ActivityIndicator color={theme.colors.accent} />
      </Screen>
    );
  }

  if (!permission.granted) {
    return (
      <Screen className="items-center justify-center px-8">
        <Ionicons name="camera-outline" size={64} color={theme.colors.foregroundTertiary} />
        <Text variant="titleMd" className="text-center mb-2 mt-4">
          Kamera izni gerekli
        </Text>
        <Text variant="bodySm" color="secondary" className="text-center mb-6">
          Ürün fotoğrafı çekmek için kamera erişimine izin verin.
        </Text>
        <Pressable
          onPress={() => void requestPermission()}
          className="h-12 px-6 items-center justify-center rounded-2xl"
          style={{ backgroundColor: theme.colors.accent }}
        >
          <Text variant="titleSm" style={{ color: '#fff' }}>
            İzin Ver
          </Text>
        </Pressable>
      </Screen>
    );
  }

  return (
    <View className="flex-1 bg-black">
      <CameraView ref={cameraRef} style={{ flex: 1 }} facing="back" />
      <View
        className="absolute inset-0 justify-between p-6 pt-16"
        style={{ pointerEvents: 'box-none' }}
      >
        <Pressable
          accessibilityLabel="Kapat"
          onPress={() => router.back()}
          className="h-11 w-11 items-center justify-center rounded-full bg-black/40 self-start"
        >
          <Ionicons name="close" size={24} color="#fff" />
        </Pressable>

        {step !== 'idle' ? (
          <View className="px-4 pb-4">
            <UploadProgress progress={progress} label={stepLabel} />
          </View>
        ) : (
          <View className="items-center pb-8">
            <Pressable
              accessibilityLabel="Fotoğraf çek"
              onPress={() => void handleCapture()}
              disabled={isBusy}
              className="h-20 w-20 items-center justify-center rounded-full border-4 border-white"
            >
              {isBusy ? (
                <ActivityIndicator color="#fff" />
              ) : (
                <View className="h-16 w-16 rounded-full bg-white" />
              )}
            </Pressable>
          </View>
        )}
      </View>
    </View>
  );
}
