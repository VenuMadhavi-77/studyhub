# StudyHub Production Deployment Guide

This guide describes how to deploy the StudyHub platform (**Spring Boot 3.3 / Java 21** backend + **React 18 / Vite** frontend) entirely on **Render**, with a free cloud MySQL database.

---

## 1. Architecture Overview

```
┌──────────────────────────────────────────────────┐
│                  Render.com                       │
│                                                   │
│  ┌─────────────────┐    ┌──────────────────────┐  │
│  │  studyhub-       │    │  studyhub-frontend   │  │
│  │  backend         │    │  (Static Site)       │  │
│  │  (Docker Web     │◄───│  React 18 / Vite     │  │
│  │   Service)       │    │  dist → Render CDN   │  │
│  │  Port 8080       │    │                      │  │
│  └────────┬─────────┘    └──────────────────────┘  │
│           │                                        │
└───────────┼────────────────────────────────────────┘
            │  TLS
┌───────────▼───────────────┐
│  Aiven / TiDB Cloud       │
│  MySQL 8.0 (Free Tier)    │
│  studyhub_db               │
└────────────────────────────┘
```

---

## 2. Prerequisites

| Requirement | Where to get it |
|:---|:---|
| GitHub account with your repo | [github.com](https://github.com) |
| Render account (free) | [render.com](https://render.com) — sign up with GitHub |
| Cloud MySQL database (free) | [aiven.io](https://aiven.io) or [tidbcloud.com](https://tidbcloud.com) |
| Google Gemini API key (optional) | [aistudio.google.com](https://aistudio.google.com/apikey) |

---

## 3. Step 1 — Set Up Free Cloud MySQL

1. Go to [aiven.io](https://aiven.io/) or [tidbcloud.com](https://tidbcloud.com/) and create a **free MySQL** instance.
2. Create a database named `studyhub_db`.
3. Note your connection details:
   - **Host** (e.g. `mysql-xxx.aivencloud.com`)
   - **Port** (e.g. `24950` for Aiven, `4000` for TiDB)
   - **Username** (e.g. `avnadmin` or `root`)
   - **Password**

---

## 4. Step 2 — Deploy Using Render Blueprint (Recommended)

The easiest method — deploys **both** backend and frontend in one click using the included [`render.yaml`](render.yaml).

1. Go to **[dashboard.render.com/blueprints](https://dashboard.render.com/blueprints)**.
2. Click **New Blueprint Instance**.
3. Connect your GitHub repo: `VenuMadhavi-77/studyhub`.
4. Render will detect `render.yaml` and show two services:
   - `studyhub-backend` (Docker Web Service)
   - `studyhub-frontend` (Static Site)
5. Fill in the prompted environment variables:
   - `DB_HOST` → your cloud MySQL host
   - `DB_USERNAME` → your database username
   - `DB_PASSWORD` → your database password
   - `GEMINI_API_KEY` → your Google Gemini key (optional)
   - `VITE_API_URL` → `https://studyhub-backend.onrender.com/api/v1` (replace with your actual backend URL after creation)
6. Click **Apply**. Both services will be built and deployed automatically.

---

## 5. Step 2 (Alternative) — Deploy Manually on Render

If you prefer manual setup instead of the Blueprint:

### 5a. Backend — Docker Web Service

1. In the Render Dashboard, click **New +** → **Web Service**.
2. Connect your repository: `VenuMadhavi-77/studyhub`.
3. Configure:
   - **Environment**: `Docker`
   - **Docker Context**: `./backend`
   - **Dockerfile Path**: `./backend/Dockerfile`
   - **Plan**: `Free`
4. Add **Environment Variables**:

   | Variable | Value |
   |:---|:---|
   | `SPRING_PROFILES_ACTIVE` | `prod` |
   | `PORT` | `8080` |
   | `STORAGE_LOCAL_PATH` | `/tmp/uploads` |
   | `DB_HOST` | `<your-cloud-mysql-host>` |
   | `DB_PORT` | `<your-cloud-mysql-port>` |
   | `DB_NAME` | `studyhub_db` |
   | `DB_USERNAME` | `<your-db-username>` |
   | `DB_PASSWORD` | `<your-db-password>` |
   | `DB_SSL_MODE` | `PREFERRED` |
   | `JWT_SECRET` | *(auto-generated or paste a 256-bit base64 key)* |
   | `CORS_ALLOWED_ORIGINS` | `https://studyhub-frontend.onrender.com` |
   | `ADMIN_BOOTSTRAP_PASSWORD` | `<your-chosen-admin-password>` |
   | `GEMINI_API_KEY` | `<optional-google-gemini-key>` |

5. Click **Create Web Service**. Render will build the Docker image and start Tomcat.
6. Copy your backend URL (e.g. `https://studyhub-backend.onrender.com`).

### 5b. Frontend — Static Site

1. Click **New +** → **Static Site**.
2. Connect the same repository.
3. Configure:
   - **Root Directory**: `frontend`
   - **Build Command**: `npm install && npm run build`
   - **Publish Directory**: `dist`
4. Add **Environment Variable**:

   | Variable | Value |
   |:---|:---|
   | `VITE_API_URL` | `https://studyhub-backend.onrender.com/api/v1` |

5. Under **Redirects / Rewrites**, add a catch-all rewrite:
   - **Source**: `/*`
   - **Destination**: `/index.html`
   - **Action**: `Rewrite`
6. Click **Create Static Site**.

---

## 6. Environment Variables Reference

| Variable | Default | Required | Description |
|:---|:---|:---:|:---|
| `SPRING_PROFILES_ACTIVE` | `prod` | ✅ | Active Spring profile |
| `PORT` | `8080` | — | Backend server port |
| `DB_HOST` | — | ✅ | MySQL server host |
| `DB_PORT` | `3306` | ✅ | Database port |
| `DB_NAME` | `studyhub_db` | ✅ | Database name |
| `DB_USERNAME` | — | ✅ | Database user |
| `DB_PASSWORD` | — | ✅ | Database password |
| `DB_SSL_MODE` | `PREFERRED` | — | SSL mode (`DISABLED`, `PREFERRED`, `REQUIRED`) |
| `JWT_SECRET` | — | ✅ | 256-bit key for signing JWT tokens |
| `CORS_ALLOWED_ORIGINS` | — | ✅ | Frontend URL (e.g. `https://studyhub-frontend.onrender.com`) |
| `STORAGE_LOCAL_PATH` | `/tmp/uploads` | — | File upload directory |
| `GEMINI_API_KEY` | — | — | Google Gemini API key for AI summaries |
| `VITE_API_URL` | `/api/v1` | ✅ | Backend API base URL (set during frontend build) |
| `ADMIN_BOOTSTRAP_EMAIL` | `admin@studyhub.edu` | — | Initial admin email |
| `ADMIN_BOOTSTRAP_PASSWORD` | — | — | Initial admin password |

---

## 7. Post-Deployment Verification

After both services are live:

```bash
# Check backend health
curl https://studyhub-backend.onrender.com/actuator/health

# Expected response:
# {"status":"UP"}
```

Then visit your frontend URL (e.g. `https://studyhub-frontend.onrender.com`) and verify:
- [ ] Login page loads
- [ ] Registration works
- [ ] API calls reach the backend (check browser DevTools → Network tab)

---

## 8. Important Notes

- **Free Tier Cold Starts**: Render free-tier services spin down after 15 minutes of inactivity. The first request after sleep takes ~30–60 seconds.
- **File Uploads**: On the free tier, uploaded files are stored in `/tmp/uploads` and will be lost on redeploy. For persistent storage, upgrade to a paid plan or use S3/R2.
- **CORS**: Make sure `CORS_ALLOWED_ORIGINS` on the backend matches your frontend's exact URL (including `https://`).
- **Database Migrations**: Flyway runs automatically on startup. The first deploy will create all tables.

---

## 9. Security & Production Checklist

- [x] Non-root container execution (Alpine UID 100)
- [x] Health check endpoint (`/actuator/health`)
- [x] Flyway database migration management
- [x] Data seeder disabled in production (`!prod` profile guard)
- [x] Rate limiting on login and upload endpoints
- [x] JWT with refresh token rotation
