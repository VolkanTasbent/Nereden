# Nereden?

AI-powered visual product discovery for the Turkish market. Snap a photo, find the product, compare prices, discover alternatives.

## Project Structure

```
nereden/
├── app/              # Mobile screens (Expo Router)
├── backend/          # Spring Boot API
├── components/       # Design system
├── features/         # Mobile feature modules
└── services/         # Mobile API clients
```

## Quick Start

### Mobile
```bash
cp .env.example .env
npm install
npm start
```

### Backend
```bash
cd backend
cp .env.example .env
docker compose up -d          # PostgreSQL 16
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

> Docker yoksa: `psql -d postgres -f backend/scripts/init-db.sql` ile veritabanını oluşturun.

## Documentation

- [ARCHITECTURE.md](./ARCHITECTURE.md) — Technical architecture, navigation, design system
- [ROADMAP.md](./ROADMAP.md) — Phased development plan

## Tech Stack

React Native · Expo · TypeScript · Spring Boot · PostgreSQL

## Google Play

Play Store yayını için [PLAY_STORE.md](./PLAY_STORE.md) dosyasına bakın.

## Project Status

**Phase 0 complete** — Architecture, navigation skeleton, design system foundation. Feature implementation follows [ROADMAP.md](./ROADMAP.md).
