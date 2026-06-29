import * as ImagePicker from 'expo-image-picker';
import { useRouter } from 'expo-router';
import { useEffect, useRef, useState } from 'react';
import { ActivityIndicator, View } from 'react-native';
import { Ionicons } from '@expo/vector-icons';

import { UploadProgress } from '@/components/feedback/UploadProgress';
import { Screen } from '@/components/layout/Screen';
import { Header } from '@/components/layout/Header';
import { Button } from '@/components/ui/Button';
import { Text } from '@/components/ui/Text';
import { useImageAnalysis } from '@/hooks/useImageAnalysis';
import { useTheme } from '@/hooks';

export default function GalleryPickerScreen() {
  const router = useRouter();
  const { theme } = useTheme();
  const started = useRef(false);
  const [permissionDenied, setPermissionDenied] = useState(false);
  const { analyzeFromUri, step, progress } = useImageAnalysis();

  useEffect(() => {
    if (!started.current) {
      started.current = true;
      void pickImage();
    }
  }, []);

  async function pickImage() {
    const permission = await ImagePicker.requestMediaLibraryPermissionsAsync();
    if (!permission.granted) {
      setPermissionDenied(true);
      return;
    }

    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ['images'],
      quality: 1,
      allowsEditing: true,
      aspect: [1, 1],
    });

    if (result.canceled || !result.assets[0]) {
      router.back();
      return;
    }

    try {
      await analyzeFromUri(result.assets[0].uri);
    } catch {
      router.back();
    }
  }

  const stepLabel =
    step === 'compressing'
      ? 'Görsel sıkıştırılıyor...'
      : step === 'uploading'
        ? 'Görsel yükleniyor...'
        : step === 'starting'
          ? 'Analiz başlatılıyor...'
          : 'Galeri açılıyor...';

  if (permissionDenied) {
    return (
      <Screen className="items-center justify-center px-8">
        <Header title="Galeriden Seç" showBack />
        <Ionicons name="images-outline" size={64} color={theme.colors.foregroundTertiary} />
        <Text variant="titleMd" className="text-center mt-4 mb-2">
          Galeri izni gerekli
        </Text>
        <Text variant="bodySm" color="secondary" className="text-center mb-6">
          Fotoğraf seçmek için galeri erişimine izin verin.
        </Text>
        <Button label="Tekrar Dene" onPress={() => void pickImage()} />
      </Screen>
    );
  }

  return (
    <Screen>
      <Header title="Galeriden Seç" showBack />
      <View className="flex-1 items-center justify-center gap-6 px-8">
        <ActivityIndicator size="large" color={theme.colors.accent} />
        <UploadProgress progress={step === 'idle' ? 0.1 : progress} label={stepLabel} />
      </View>
    </Screen>
  );
}
