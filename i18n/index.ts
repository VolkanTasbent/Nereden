import * as Localization from 'expo-localization';
import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';

import en from './locales/en.json';
import tr from './locales/tr.json';

const resources = {
  tr: { translation: tr },
  en: { translation: en },
} as const;

const deviceLocale = Localization.getLocales()[0]?.languageCode ?? 'tr';

void i18n.use(initReactI18next).init({
  resources,
  lng: deviceLocale.startsWith('tr') ? 'tr' : 'en',
  fallbackLng: 'tr',
  interpolation: {
    escapeValue: false,
  },
  compatibilityJSON: 'v4',
});

export default i18n;
