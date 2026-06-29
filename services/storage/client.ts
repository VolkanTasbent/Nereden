import { Platform } from 'react-native';

import { appConfig } from '@/constants/config';
import { ApiClient } from '@/services/api/client';

export interface UploadImageResult {
  url: string;
  key: string;
}

export interface StorageService {
  uploadImage(uri: string): Promise<UploadImageResult>;
}

const apiClient = new ApiClient(appConfig.apiUrl);

async function buildUploadFormData(uri: string): Promise<FormData> {
  const formData = new FormData();

  if (Platform.OS === 'web') {
    const response = await fetch(uri);
    const blob = await response.blob();
    const file = new File([blob], 'analysis.jpg', {
      type: blob.type.startsWith('image/') ? blob.type : 'image/jpeg',
    });
    formData.append('file', file);
  } else {
    formData.append('file', {
      uri,
      type: 'image/jpeg',
      name: 'analysis.jpg',
    } as unknown as Blob);
  }

  return formData;
}

export const storageService: StorageService = {
  async uploadImage(uri) {
    const formData = await buildUploadFormData(uri);

    return apiClient.request<UploadImageResult>('/storage/upload', {
      method: 'POST',
      auth: true,
      body: formData,
    });
  },
};
