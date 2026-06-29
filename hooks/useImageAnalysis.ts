import { useState } from 'react';
import { useRouter } from 'expo-router';

import { aiService } from '@/services/ai';
import { storageService } from '@/services/storage';
import { compressImage } from '@/utils/imageCompress';
import { getErrorMessage, AuthError } from '@/utils/errors';
import { useToastStore } from '@/store/toast.store';

export type AnalysisFlowStep =
  | 'idle'
  | 'compressing'
  | 'uploading'
  | 'starting'
  | 'done'
  | 'error';

export function useImageAnalysis() {
  const router = useRouter();
  const showToast = useToastStore((s) => s.show);
  const [step, setStep] = useState<AnalysisFlowStep>('idle');
  const [progress, setProgress] = useState(0);

  async function analyzeFromUri(uri: string) {
    setStep('compressing');
    setProgress(0.15);

    try {
      const compressed = await compressImage(uri);
      setStep('uploading');
      setProgress(0.4);

      const upload = await storageService.uploadImage(compressed);
      setStep('starting');
      setProgress(0.7);

      const analysis = await aiService.createAnalysisRequest({ imageUrl: upload.url });
      setStep('done');
      setProgress(1);

      router.replace(`/analysis/${analysis.id}`);
    } catch (error) {
      setStep('error');
      showToast(getErrorMessage(error), 'error');
      if (error instanceof AuthError) {
        router.replace('/(auth)/login');
      }
      throw error;
    }
  }

  return { analyzeFromUri, step, progress, reset: () => { setStep('idle'); setProgress(0); } };
}
