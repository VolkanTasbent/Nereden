import { useTranslation } from 'react-i18next';

import { PlaceholderScreen } from '@/components/layout/PlaceholderScreen';

export default function NotificationsScreen() {
  const { t } = useTranslation();

  return (
    <PlaceholderScreen
      title={t('settings.notifications')}
      showBack
    />
  );
}
