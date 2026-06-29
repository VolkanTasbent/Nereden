# Nereden? — Spring Boot API

Backend REST API for the Nereden? mobile application.

## Stack

| Layer | Technology |
|-------|------------|
| Framework | Spring Boot 3.4 |
| Language | Java 17 |
| Database | PostgreSQL 16 |
| ORM | Spring Data JPA |
| Security | Spring Security + JWT |
| API Docs | SpringDoc OpenAPI (Swagger) |
| Build | Maven |

## Architecture

```
backend/src/main/java/com/nereden/api/
├── domain/           # Entities & repository interfaces
├── application/      # DTOs, services (use cases)
├── infrastructure/   # Security, config, persistence adapters
└── presentation/     # REST controllers
```

## API Endpoints (skeleton)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/health` | Health check |
| POST | `/api/auth/register` | Kayıt |
| POST | `/api/auth/login` | Giriş |
| POST | `/api/auth/logout` | Çıkış |
| POST | `/api/auth/forgot-password` | Şifre sıfırlama |
| POST | `/api/auth/refresh` | Token yenileme |
| GET | `/api/auth/me` | Mevcut kullanıcı |
| POST | `/api/storage/upload` | Görsel yükleme |
| POST | `/api/analysis` | Analiz başlat |
| GET | `/api/analysis/{id}` | Analiz durumu |

Swagger UI: `http://localhost:8080/api/swagger-ui.html`

## Quick Start

```bash
# 1. PostgreSQL başlat
cd backend
cp .env.example .env
docker compose up -d

# 2. API'yi çalıştır (Java 17 + Maven gerekli)
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Şema yönetimi **Flyway** ile yapılır (`src/main/resources/db/migration/`). İlk çalıştırmada migration'lar otomatik uygulanır.

### Docker olmadan (Homebrew PostgreSQL)

```bash
brew services start postgresql@17
psql -d postgres -f scripts/init-db.sql
```

Mobil uygulama `.env`:
```
EXPO_PUBLIC_API_URL=http://localhost:8080/api
EXPO_PUBLIC_AI_API_URL=http://localhost:8080/api
```

> iOS simulator için `localhost` çalışır. Android emulator için `http://10.0.2.2:8080/api` kullanın.

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_HOST` | localhost | PostgreSQL host |
| `DB_PORT` | 5432 | PostgreSQL port |
| `DB_NAME` | nereden | Database name |
| `DB_USER` | nereden | Database user |
| `DB_PASSWORD` | nereden | Database password |
| `JWT_SECRET` | — | JWT imzalama anahtarı |
| `SERVER_PORT` | 8080 | API port |
| `UPLOAD_DIR` | ./uploads | Yerel dosya depolama |
| `GEMINI_API_KEY` | — | Google Gemini vision (ücretsiz kota, önerilen) |
| `GEMINI_MODEL` | gemini-2.5-flash | Gemini model adı |
| `GEMINI_PRO_MODEL` | gemini-2.5-pro | Düşük güven için pro model |
| `OPENAI_API_KEY` | — | OpenAI vision (alternatif, ücretli) |
| `SERPAPI_KEY` | — | Gerçek mağaza fiyatları (opsiyonel) |
| `SEARCHAPI_KEY` | — | Google Lens ürün arama ([searchapi.io](https://www.searchapi.io)) |

## Status

**Phase 0** — Proje iskeleti hazır. Controller'lar ve entity'ler tanımlı; servis implementasyonları roadmap'e göre eklenecek.
