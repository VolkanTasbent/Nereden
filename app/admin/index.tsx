import { useTranslation } from 'react-i18next';

import { PlaceholderScreen } from '@/components/layout/PlaceholderScreen';

export default function AdminScreen() {
  const { t } = useTranslation();

  return (
    <PlaceholderScreen
      title={t('admin.title')}
      showBack
    />
  );
}
