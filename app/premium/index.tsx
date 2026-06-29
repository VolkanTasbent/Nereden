import { useTranslation } from 'react-i18next';

import { PlaceholderScreen } from '@/components/layout/PlaceholderScreen';

export default function PremiumScreen() {
  const { t } = useTranslation();

  return (
    <PlaceholderScreen
      title={t('premium.title')}
      subtitle={t('premium.subtitle')}
      showBack
    />
  );
}
