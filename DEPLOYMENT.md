# StudyHub Production Deployment Guide

This guide describes how to deploy the StudyHub platform (Spring Boot 3.3 / Java 21 backend, MySQL 8 / Aiven Cloud database, and React 18 / Vite / Nginx frontend) for local production-simulation and cloud environments.

---

## 1. Quick Start: Local Full Stack via Docker Compose

To run the complete platform locally in containerized production mode:

```bash
# 1. Clone repository & prepare environment
cp .env.example .env

# 2. Build and launch all services in detached mode
docker-compose up -d --build

# 3. Verify health
docker-compose ps
```

- **Frontend Web UI**: `http://localhost` (or `http://localhost:80`)
- **Backend API & Actuator Health**: `http://localhost:8080/actuator/health`
- **Swagger / OpenAPI**: `http://localhost:8080/swagger-ui.html`
- **MySQL Database**: `localhost:3306` (`studyhub_db`)

---

## 2. Environment Variables Reference Table

| Variable Name | Default Value | Required? | Description |
| :--- | :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | `prod` | Yes | Active Spring profile (`prod`, `dev`, or `local`). |
| `PORT` | `8080` | No | Port on which the Spring Boot backend listens. |
| `DB_HOST` | `mysql` | Yes | MySQL server host (e.g., Aiven host, AWS RDS, or local container). |
| `DB_PORT` | `3306` | Yes | Database connection port (e.g., `24950` for Aiven). |
| `DB_NAME` | `studyhub_db` | Yes | Database name. |
| `DB_USERNAME` | `studyhub_user` | Yes | Database user. |
| `DB_PASSWORD` | - | Yes | Database password. |
| `DB_SSL_MODE` | `REQUIRED` in prod | No | SSL Mode (`DISABLED`, `REQUIRED`, `VERIFY_CA`). |
| `DB_SSL_PARAMS` | - | No | Extra JDBC SSL parameters for Aiven CA certificate. |
| `JWT_SECRET` | - | Yes | 256-bit secret key used for signing JWT access tokens. |
| `JWT_ACCESS_EXPIRATION_MS` | `900000` (15m) | No | JWT access token validity period in milliseconds. |
| `JWT_REFRESH_EXPIRATION_MS`| `604800000` (7d) | No | Refresh token validity period in milliseconds. |
| `ADMIN_BOOTSTRAP_EMAIL` | `admin@studyhub.edu`| No | Superadmin email initialized on first startup. |
| `ADMIN_BOOTSTRAP_PASSWORD` | `AdminStudyHub#2026`| No | Superadmin initial password. |
| `CORS_ALLOWED_ORIGINS` | `http://localhost` | Yes | Comma-separated list of allowed web frontend URLs. |
| `STORAGE_TYPE` | `LOCAL` | No | Storage mechanism: `LOCAL` or `S3`. |
| `STORAGE_LOCAL_PATH` | `./uploads` | No | Filesystem directory used when `STORAGE_TYPE=LOCAL`. |
| `S3_ENDPOINT` | - | No | S3 / Cloudflare R2 endpoint URL when `STORAGE_TYPE=S3`. |
| `S3_BUCKET` | - | No | S3 bucket name. |
| `S3_REGION` | `us-east-1` | No | S3 region identifier. |
| `S3_ACCESS_KEY` | - | No | AWS / R2 access key ID. |
| `S3_SECRET_KEY` | - | No | AWS / R2 secret access key. |
| `S3_PUBLIC_URL` | - | No | Public CDN / bucket base URL for direct client downloads. |
| `AI_PROVIDER` | `GEMINI` | No | AI provider engine. |
| `GEMINI_API_KEY` | - | No | Google Gemini API key for document summaries. |
| `VITE_API_URL` | `/api/v1` | No | Base API URL configured during frontend build. |
| `VITE_SHOW_DEMO_LOGIN` | `false` | No | Flag to show/hide 1-click demo logins on the sign-in page. |

---

## 3. Connecting to Aiven MySQL Cloud

Aiven requires secure TLS connections. Configure the following variables in `.env`:

```env
DB_HOST=mysql-xxxx-studyhub.aivencloud.com
DB_PORT=24950
DB_NAME=defaultdb
DB_USERNAME=avnadmin
DB_PASSWORD=<aiven-secret-password>
DB_SSL_MODE=REQUIRED
```

If connecting with strict CA verification:
```env
DB_SSL_PARAMS=&trustCertificateKeyStoreUrl=file:/app/certs/ca.pem
```

---

## 4. Frontend Deployment (Vercel & Netlify)

StudyHub includes pre-configured SPA routing fallback rules:

- **Vercel**: Configured in `frontend/vercel.json`:
  ```json
  {
    "rewrites": [{ "source": "/(.*)", "destination": "/index.html" }]
  }
  ```
- **Netlify**: Configured in `frontend/public/_redirects`:
  ```
  /*    /index.html   200
  ```

### WebSocket Protocol Resolution
When deployed behind HTTPS on Vercel or custom domain, StudyHub's `websocket.ts` service automatically selects `wss://` based on `window.location.protocol` or the `VITE_API_URL` prefix.

---

## 5. Security & Production Checklist

1. [x] **Non-root execution**: Backend runs as `studyhub:studyhub` (Alpine UID 100); Frontend Nginx runs as `nginx:nginx`.
2. [x] **Health checks**: Docker containers implement automated health monitoring.
3. [x] **Database Migrations**: Flyway manages incremental schema evolution (`validate` mode in production).
4. [x] **Data Seeder Guard**: Seeder is active only under `!prod` profile.
5. [x] **Audit logs & Rate limiting**: Token bucket filter protects public login and upload endpoints against abuse.
