const DEFAULT_DEV_API_URL = 'http://localhost:8080/api';

function normalizeUrl(url: string): string {
  return url.replace(/\/$/, '');
}

function resolveApiUrl(envKey: 'EXPO_PUBLIC_API_URL' | 'EXPO_PUBLIC_AI_API_URL'): string {
  const value = process.env[envKey];

  if (value) {
    return normalizeUrl(value);
  }

  if (__DEV__) {
    return DEFAULT_DEV_API_URL;
  }

  throw new Error(
    `${envKey} is required for production builds. Set it in EAS secrets or .env.production.`,
  );
}

const appEnv = process.env.EXPO_PUBLIC_APP_ENV ?? (__DEV__ ? 'development' : 'production');

export const appConfig = {
  env: appEnv,
  isProduction: appEnv === 'production',
  apiUrl: resolveApiUrl('EXPO_PUBLIC_API_URL'),
  aiApiUrl: resolveApiUrl('EXPO_PUBLIC_AI_API_URL'),
  privacyPolicyUrl: process.env.EXPO_PUBLIC_PRIVACY_POLICY_URL ?? '',
  termsUrl: process.env.EXPO_PUBLIC_TERMS_URL ?? '',
  supportEmail: process.env.EXPO_PUBLIC_SUPPORT_EMAIL ?? 'destek@nereden.com',
} as const;
