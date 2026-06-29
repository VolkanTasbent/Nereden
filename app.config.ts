import type { ConfigContext } from 'expo/config';

const APP_NAME = 'Nereden?';
const APP_SLUG = 'nereden';
const APP_VERSION = '1.0.0';
const ANDROID_VERSION_CODE = 1;
const ANDROID_PACKAGE = 'com.nereden.app';
const IOS_BUNDLE_ID = 'com.nereden.app';

export default ({ config }: ConfigContext) => ({
  ...config,
  name: APP_NAME,
  slug: APP_SLUG,
  version: APP_VERSION,
  orientation: 'portrait',
  scheme: 'nereden',
  userInterfaceStyle: 'automatic',
  icon: './assets/icon.png',
  splash: {
    image: './assets/splash-icon.png',
    resizeMode: 'contain',
    backgroundColor: '#F5F5F7',
  },
  ios: {
    supportsTablet: false,
    bundleIdentifier: IOS_BUNDLE_ID,
    buildNumber: '1',
    infoPlist: {
      NSCameraUsageDescription:
        'Ürün fotoğrafı çekerek görsel arama yapabilmeniz için kamera erişimi gereklidir.',
      NSPhotoLibraryUsageDescription:
        'Galeriden ürün fotoğrafı seçerek arama yapabilmeniz için fotoğraf erişimi gereklidir.',
      ITSAppUsesNonExemptEncryption: false,
    },
  },
  android: {
    package: ANDROID_PACKAGE,
    versionCode: ANDROID_VERSION_CODE,
    adaptiveIcon: {
      backgroundColor: '#F5F5F7',
      foregroundImage: './assets/android-icon-foreground.png',
      backgroundImage: './assets/android-icon-background.png',
      monochromeImage: './assets/android-icon-monochrome.png',
    },
    permissions: ['android.permission.CAMERA', 'android.permission.READ_MEDIA_IMAGES'],
    blockedPermissions: [
      'android.permission.RECORD_AUDIO',
      'android.permission.READ_EXTERNAL_STORAGE',
      'android.permission.WRITE_EXTERNAL_STORAGE',
      'android.permission.ACCESS_FINE_LOCATION',
      'android.permission.ACCESS_COARSE_LOCATION',
    ],
    softwareKeyboardLayoutMode: 'resize',
    allowBackup: false,
  },
  web: {
    bundler: 'metro',
    favicon: './assets/favicon.png',
  },
  plugins: [
    'expo-router',
    'expo-font',
    'expo-secure-store',
    'expo-localization',
    [
      'expo-camera',
      {
        cameraPermission:
          'Ürün fotoğrafı çekerek görsel arama yapabilmeniz için kamera erişimi gereklidir.',
        recordAudioAndroid: false,
      },
    ],
    [
      'expo-image-picker',
      {
        photosPermission:
          'Galeriden ürün fotoğrafı seçerek arama yapabilmeniz için fotoğraf erişimi gereklidir.',
      },
    ],
    [
      'expo-build-properties',
      {
        android: {
          compileSdkVersion: 35,
          targetSdkVersion: 35,
          minSdkVersion: 24,
          buildToolsVersion: '35.0.0',
        },
      },
    ],
  ],
  experiments: {
    typedRoutes: true,
  },
  extra: {
    privacyPolicyUrl: process.env.EXPO_PUBLIC_PRIVACY_POLICY_URL,
    supportEmail: process.env.EXPO_PUBLIC_SUPPORT_EMAIL,
    ...(process.env.EAS_PROJECT_ID
      ? { eas: { projectId: process.env.EAS_PROJECT_ID } }
      : {}),
  },
});
