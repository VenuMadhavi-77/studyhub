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

## 4. Cloud Deployment (Render & Vercel)

### Architecture in the Cloud
- **Backend**: Spring Boot 3 runs on **Render** as a Docker Web Service (`backend/Dockerfile`).
- **Database**: Free cloud MySQL on **Aiven**, **TiDB Cloud**, or **Railway**.
- **Frontend**: React 18 runs on **Vercel** or **Render Static Site**.

---

### Step 1: Set Up Free Cloud MySQL (Aiven / TiDB)
1. Go to [aiven.io](https://aiven.io/) or [tidbcloud.com](https://tidbcloud.com/) and create a free MySQL database named `studyhub_db`.
2. Note your connection details:
   - Host (e.g. `mysql-xxx.aivencloud.com`)
   - Port (e.g. `24950` or `3306`)
   - User (e.g. `avnadmin` or `root`)
   - Password

---

### Step 2: Deploy Backend on Render (Free)
1. Log in to [render.com](https://render.com/) with GitHub.
2. Click **New +** $\rightarrow$ **Web Service**.
3. Select your repository: **`VenuMadhavi-77/studyhub`**.
4. Configure:
   - **Language / Environment**: `Docker`
   - **Docker Context**: `./backend`
   - **Dockerfile Path**: `./backend/Dockerfile`
   - **Plan**: `Free`
5. Under **Environment Variables**, add:
   - `SPRING_PROFILES_ACTIVE`: `prod`
   - `PORT`: `8080`
   - `STORAGE_LOCAL_PATH`: `/tmp/uploads`
   - `DB_HOST`: `<your-cloud-mysql-host>`
   - `DB_PORT`: `<your-cloud-mysql-port>`
   - `DB_NAME`: `studyhub_db`
   - `DB_USERNAME`: `<your-db-username>`
   - `DB_PASSWORD`: `<your-db-password>`
   - `DB_SSL_MODE`: `PREFERRED`
   - `JWT_SECRET`: `dGhpc2lzYXZlcnlzZWNyZXRrZXlmb3JzdHVkeWh1YmFwcGxpY2F0aW9uMjAyNmJ5c3ByaW5nYm9vdDM=`
   - `CORS_ALLOWED_ORIGINS`: `*`
   - `ADMIN_BOOTSTRAP_PASSWORD`: `<your-chosen-admin-password>`
   - `GEMINI_API_KEY`: `<optional-google-gemini-key>`
6. Click **Create Web Service**. Render will build the Docker container and start Tomcat.
7. Copy your backend URL (e.g. `https://studyhub-backend.onrender.com`).

---

### Step 3: Deploy Frontend on Vercel (Free)
1. Log in to [vercel.com](https://vercel.com/) with GitHub.
2. Click **Add New...** $\rightarrow$ **Project**.
3. Import **`VenuMadhavi-77/studyhub`**.
4. Configure project settings:
   - **Framework Preset**: `Vite`
   - **Root Directory**: Click *Edit* and select **`frontend`**
   - **Build Command**: `npm run build`
   - **Output Directory**: `dist`
5. Under **Environment Variables**, add:
   - `VITE_API_URL`: `https://studyhub-backend.onrender.com/api/v1` *(replace with your Render backend URL)*
6. Click **Deploy**. Vercel will build and deploy your React app in ~45 seconds!

---

### Step 4: Alternative: Deploy Both on Render using `render.yaml`
If you prefer having both services on Render, StudyHub includes a zero-config [`render.yaml`](render.yaml) Blueprint:
1. Go to [dashboard.render.com/blueprints](https://dashboard.render.com/blueprints).
2. Click **New Blueprint Instance**.
3. Select `VenuMadhavi-77/studyhub`.
4. Enter your MySQL credentials and click **Apply**. Both backend and frontend will be provisioned together automatically!

---

## 5. Security & Production Checklist

1. [x] **Non-root execution**: Backend runs as `studyhub:studyhub` (Alpine UID 100); Frontend Nginx runs as `nginx:nginx`.
2. [x] **Health checks**: Docker containers implement automated health monitoring.
3. [x] **Database Migrations**: Flyway manages incremental schema evolution (`validate` mode in production).
4. [x] **Data Seeder Guard**: Seeder is active only under `!prod` profile.
5. [x] **Audit logs & Rate limiting**: Token bucket filter protects public login and upload endpoints against abuse.
