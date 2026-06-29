# Google Play Store — Yayın Rehberi

Bu rehber, Nereden? uygulamasını Google Play'e yüklemek için gereken adımları içerir.

## Ön koşullar

1. [Google Play Console](https://play.google.com/console) hesabı (tek seferlik $25)
2. [Expo hesabı](https://expo.dev) ve EAS CLI
3. **HTTPS** üzerinde çalışan production backend API
4. Yayınlanmış **Gizlilik Politikası** URL'si (zorunlu)

## 1. Production API'yi hazırla

Mobil uygulama production build'de `localhost` kullanamaz. Backend'i bir sunucuya deploy et (Railway, Fly.io, AWS, vb.) ve HTTPS URL al:

```
https://api.senin-domainin.com/api
```

Backend `.env` örneği:

```bash
CORS_ORIGINS=https://nereden.app
JWT_SECRET=<güçlü-256-bit-anahtar>
DB_HOST=...
GEMINI_API_KEY=...
```

## 2. Gizlilik politikasını yayınla

1. `docs/PRIVACY_POLICY.md` dosyasını düzenle (`{{SUPPORT_EMAIL}}` değiştir)
2. GitHub Pages, Notion veya web sitende yayınla
3. Public URL al (ör. `https://nereden.app/privacy`)

## 3. EAS projesini başlat

```bash
npm install -g eas-cli
eas login
eas init
```

`eas init` sonrası oluşan `projectId`'yi not al veya `EAS_PROJECT_ID` ortam değişkeni olarak kullan.

## 4. Production secret'ları ayarla

```bash
eas secret:create --scope project --name EXPO_PUBLIC_API_URL --value "https://api.senin-domainin.com/api"
eas secret:create --scope project --name EXPO_PUBLIC_AI_API_URL --value "https://api.senin-domainin.com/api"
eas secret:create --scope project --name EXPO_PUBLIC_PRIVACY_POLICY_URL --value "https://senin-domainin.com/privacy"
eas secret:create --scope project --name EXPO_PUBLIC_SUPPORT_EMAIL --value "destek@senin-domainin.com"
```

## 5. Production AAB build

```bash
npm run build:android
# veya
eas build --platform android --profile production
```

Build tamamlanınca `.aab` dosyasını indir.

## 6. Play Console'da uygulama oluştur

| Alan | Değer |
|------|-------|
| Uygulama adı | Nereden? |
| Paket adı | `com.nereden.app` |
| Kategori | Alışveriş |
| İçerik derecelendirmesi | Anketi doldur |

### Mağaza listesi

- **Kısa açıklama** (80 karakter): Fotoğraf çek, ürünü bul, fiyatları karşılaştır.
- **Tam açıklama**: Uygulamanın özelliklerini anlat
- **Ekran görüntüleri**: En az 2 telefon ekranı (1080×1920 veya benzeri)
- **Yüksek kalite ikon**: 512×512 PNG
- **Özellik grafiği**: 1024×500 PNG (opsiyonel ama önerilir)

### Veri güvenliği formu

Play Console'da şunları beyan et:

| Veri | Toplanıyor | Amaç |
|------|------------|------|
| E-posta | Evet | Hesap yönetimi |
| Fotoğraflar | Evet | Ürün arama |
| Arama geçmişi | Evet | Uygulama işlevselliği |

Veriler şifrelenerek aktarılır (HTTPS). Kullanıcı veri silme talebi: destek e-postası.

### İzin beyanları

- `CAMERA` — Ürün fotoğrafı çekmek için
- `READ_MEDIA_IMAGES` — Galeriden fotoğraf seçmek için

## 7. Internal test → Production

1. İlk yüklemede **Internal testing** track kullan
2. Test kullanıcılarıyla doğrula (giriş, kamera, analiz, favoriler)
3. Sorun yoksa **Production**'a promote et

## 8. Her yeni sürümde

1. `app.config.ts` içinde `version` artır (ör. `1.0.1`)
2. `eas build --platform android --profile production` (`versionCode` otomatik artar)
3. Play Console'da yeni sürümü yükle

## Komutlar

```bash
npm run build:android      # Production AAB
npm run build:android:preview  # Test APK
npm run submit:android     # Play Console'a gönder (service account gerekli)
```

## Sorun giderme

| Sorun | Çözüm |
|-------|-------|
| API bağlanmıyor | `EXPO_PUBLIC_API_URL` secret'ını kontrol et |
| Gizlilik politikası reddi | Public URL'nin erişilebilir olduğundan emin ol |
| Target SDK hatası | `expo-build-properties` SDK 35 kullanıyor |
| Kamera izni reddi | Play Console'da izin açıklamasını doldur |

## Dosya yapısı

```
app.config.ts          # Android package, versionCode, izinler
eas.json               # Build profilleri
constants/config.ts    # Production env doğrulama
docs/PRIVACY_POLICY.md # Gizlilik politikası şablonu
.env.production.example
```
