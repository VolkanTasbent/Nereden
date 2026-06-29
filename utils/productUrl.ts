export function isDirectProductUrl(url: string): boolean {
  const lower = url.toLowerCase();
  if (
    lower.includes('/sr?q=') ||
    lower.includes('/ara?q=') ||
    lower.includes('/s?k=') ||
    lower.includes('/arama?')
  ) {
    return false;
  }
  if (lower.includes('trendyol.com') && lower.includes('-p-')) return true;
  if (lower.includes('hepsiburada.com') && (lower.includes('-p-') || lower.includes('-pm-'))) return true;
  if (lower.includes('amazon.com.tr') && (lower.includes('/dp/') || lower.includes('/gp/product/'))) return true;
  if (lower.includes('n11.com') && lower.includes('/urun/')) return true;
  return false;
}
