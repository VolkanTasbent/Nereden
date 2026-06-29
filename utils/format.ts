export function formatCurrency(
  amount: number,
  currency: 'TRY' = 'TRY',
  locale = 'tr-TR',
): string {
  return new Intl.NumberFormat(locale, {
    style: 'currency',
    currency,
    maximumFractionDigits: 0,
  }).format(amount);
}

export function formatPriceRange(
  min: number,
  max: number,
  currency: 'TRY' = 'TRY',
  locale = 'tr-TR',
): string {
  if (min === max) {
    return formatCurrency(min, currency, locale);
  }

  return `${formatCurrency(min, currency, locale)} – ${formatCurrency(max, currency, locale)}`;
}

export function formatRelativeTime(date: string, locale = 'tr-TR'): string {
  const rtf = new Intl.RelativeTimeFormat(locale, { numeric: 'auto' });
  const diffMs = new Date(date).getTime() - Date.now();
  const diffMinutes = Math.round(diffMs / (1000 * 60));

  if (Math.abs(diffMinutes) < 60) {
    return rtf.format(diffMinutes, 'minute');
  }

  const diffHours = Math.round(diffMinutes / 60);
  if (Math.abs(diffHours) < 24) {
    return rtf.format(diffHours, 'hour');
  }

  const diffDays = Math.round(diffHours / 24);
  return rtf.format(diffDays, 'day');
}
