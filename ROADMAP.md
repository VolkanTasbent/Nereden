# Nereden? — Development Roadmap

## Phase 0 — Foundation ✅ (Current)

- [x] Expo + TypeScript project bootstrap
- [x] Folder structure (Clean Architecture)
- [x] Design system tokens & base components
- [x] Expo Router navigation skeleton
- [x] Providers (Query, Theme, i18n)
- [x] REST API / Auth / Storage / AI service clients (skeleton)
- [x] Zustand stores (auth, theme, app)
- [x] i18n (TR/EN)
- [x] Type definitions

---

## Phase 1 — Core Infrastructure (Week 1–2)

### 1.1 Spring Boot Backend (`backend/`)
- [x] Spring Boot 3.4 proje iskeleti
- [x] Clean Architecture paket yapısı
- [x] Entity'ler: `User`, `AnalysisRequest`
- [x] Controller iskeleti: auth, storage, analysis, health
- [x] PostgreSQL şema taslağı (`V1__init.sql`)
- [x] Docker Compose (PostgreSQL)
- [ ] JWT implementasyonu (Spring Security filter)
- [ ] Auth servis implementasyonu (register, login, refresh)
- [ ] Görsel yükleme servisi (local storage → S3)
- [ ] Swagger dokümantasyonu doğrulama

### 1.2 Authentication (mobil)
- [ ] `HttpAuthRepository` implementasyonu
- [ ] Token saklama (`expo-secure-store`)
- [ ] Auth session bootstrap (root layout)
- [ ] Login / Register / Forgot Password ekranları
- [ ] Protected route guard

### 1.3 Design System Polish
- [ ] Load Inter font family (`expo-font`)
- [ ] Tab bar icons (`@expo/vector-icons`)
- [ ] Reanimated micro-interactions (card press, tab switch)
- [ ] Toast notification component
- [ ] Avatar component

---

## Phase 2 — Core User Flow (Week 3–4)

### 2.1 Camera & Gallery
- [ ] `expo-camera` live preview with capture
- [ ] `expo-image-picker` multi-format support
- [ ] Image compression before upload
- [ ] Upload progress UI
- [ ] Permission handling & empty states

### 2.2 AI Analysis Pipeline
- [ ] Backend: görsel al → vision model çağır → ürün eşleştir
- [ ] Ürün eşleştirme servisi entegrasyonu
- [ ] Durum takibi: polling (`/analysis/:id`)
- [ ] Analysis ekranı (animasyonlu progress)
- [ ] Hata kurtarma (retry, yeniden yükleme)

### 2.3 Product Results
- [ ] Product detail screen layout
- [ ] Exact match / similar / cheaper sections
- [ ] Store list with deep links
- [ ] Price range formatting (TRY)
- [ ] Share product action

---

## Phase 3 — Discovery & Engagement (Week 5–6)

### 3.1 Home Feed
- [ ] Curated product grid (Pinterest-style masonry)
- [ ] Category filters (fashion, furniture, electronics…)
- [ ] Pull-to-refresh
- [ ] Infinite scroll pagination

### 3.2 Search
- [ ] Text search with debounce
- [ ] Recent searches
- [ ] Category chips
- [ ] Search results grid

### 3.3 Favorites & History
- [ ] Add/remove favorite (optimistic updates)
- [ ] Favorites grid with swipe-to-delete
- [ ] Search history list with re-analyze action
- [ ] Clear history

---

## Phase 4 — Profile & Settings (Week 7)

### 4.1 Profile
- [ ] Profile header (avatar, name, stats)
- [ ] Edit profile (name, avatar upload)
- [ ] Navigation hub to sub-screens

### 4.2 Settings
- [ ] Theme toggle (light / dark / system)
- [ ] Language switcher (TR / EN)
- [ ] Notification preferences
- [ ] Haptics toggle
- [ ] Account deletion flow
- [ ] Privacy policy / Terms links

### 4.3 Notifications
- [ ] Expo push notifications setup
- [ ] In-app notification list
- [ ] Mark as read / mark all read
- [ ] Deep link from notification → product

---

## Phase 5 — Premium & Monetization (Week 8)

### 5.1 Premium Subscription
- [ ] Paywall screen design
- [ ] RevenueCat or native IAP integration
- [ ] Feature gating: daily search limits for free users
- [ ] Premium badge in profile
- [ ] Restore purchases

### 5.2 Usage Limits
- [ ] Track `search_count` per user per day
- [ ] Soft paywall on limit reached
- [ ] Priority queue for premium analysis

---

## Phase 6 — Admin Panel (Week 9)

### 6.1 Admin Access
- [ ] Role check middleware
- [ ] Admin-only route guard

### 6.2 Admin Features
- [ ] User management list
- [ ] Analysis request monitoring
- [ ] Product catalog moderation
- [ ] Basic analytics dashboard (DAU, searches, conversions)

---

## Phase 7 — Polish & Launch (Week 10–12)

### 7.1 Performance
- [ ] Image caching (`expo-image`)
- [ ] List virtualization (`FlashList`)
- [ ] Bundle size audit
- [ ] Startup time optimization

### 7.2 Quality
- [ ] E2E tests (Maestro)
- [ ] Unit tests for use-cases & utils
- [ ] Error tracking (Sentry)
- [ ] Analytics (PostHog / Amplitude)

### 7.3 App Store
- [ ] App icons & splash screen (brand)
- [ ] App Store / Play Store screenshots
- [ ] Privacy nutrition labels
- [ ] Beta testing (TestFlight / Internal Testing)
- [ ] Production release

---

## Priority Matrix

| Priority | Feature | Business Impact |
|----------|---------|-----------------|
| P0 | Auth + Camera + AI Analysis + Product Result | Core value proposition |
| P1 | Home Feed + Search + Favorites | Retention & discovery |
| P2 | Profile + Settings + History | User trust & control |
| P3 | Notifications | Re-engagement |
| P4 | Premium | Revenue |
| P5 | Admin | Operations |

---

## Definition of Done (per feature)

- [ ] Domain use-cases implemented & typed
- [ ] Repository ile REST API entegrasyonu
- [ ] React Query hooks with loading/error/empty states
- [ ] Screen UI matches design system
- [ ] i18n strings (TR + EN)
- [ ] Accessibility labels
- [ ] Error handling with user-friendly messages
- [ ] Works on iOS and Android

---

## Next Immediate Steps

1. Copy `.env.example` → `.env` ve API URL'ini tanımla
2. `npm start` ile navigasyon iskeletini doğrula
3. **Phase 1.1** — Backend REST API tasarımına başla
4. **Phase 1.2** — Authentication (ilk gerçek özellik)
