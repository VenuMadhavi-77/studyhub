<div align="center">

# 🎓 StudyHub
### Collaborative Academic Resource Platform with AI Study Assistance

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21%20LTS-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![React](https://img.shields.io/badge/React-18.3-61DAFB?style=for-the-badge&logo=react&logoColor=black)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.6-3178C6?style=for-the-badge&logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Google Gemini](https://img.shields.io/badge/AI-Gemini%201.5%20Flash-8E44AD?style=for-the-badge&logo=google&logoColor=white)](https://ai.google.dev/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-F59E0B?style=for-the-badge)](LICENSE)

<br/>

**A full-stack web application designed for university departments to share verified study resources, solve academic doubts with AI, and collaborate in real-time study sessions.**

[Live Preview](#-application-preview) • [Core Features](#-core-features) • [Quickstart](#-quickstart--installation) • [AI Integration](#-ai-subsystem-live-vs-fallback) • [Architecture](#-system-architecture) • [Test Suites](#-verification--testing-suites) • [API Directory](#-api-endpoints-overview) • [Deployment](#-production-deployment)

</div>

---

## 📸 Application Preview

<div align="center">
  <img src="docs/dashboard_preview.png" alt="StudyHub Application Dashboard" width="900" style="border-radius: 12px; box-shadow: 0 8px 30px rgba(0,0,0,0.25);" />
  <p><em>StudyHub Student Dashboard: Verified Course Notes, Bayesian Trust Ratings, Gemini AI Quick Assistant, and Live Collaborative Study Rooms</em></p>
</div>

---

## 📌 The Problem StudyHub Solves

In most academic departments, study materials are scattered across unorganized chat groups, drive links, and untracked forums. Students struggle to find verified, high-quality exam notes, while spam and duplicate uploads go unchecked.

**StudyHub** centralizes departmental academic resources with quality assurance:
1. **Curated & Peer-Rated Notes**: Every document has a Bayesian-weighted reliability score derived from peer reviews, with built-in protections against self-review manipulation.
2. **Built-in AI Academic Assistance**: Integrated Google Gemini 1.5 Flash generates instant summaries, key revision takeaways, and answers student doubts with step-by-step explanations.
3. **Synchronized Study Tools**: Real-time collaborative whiteboard rooms and Pomodoro focus timers keep study groups engaged within the same platform.

---

## ✨ Core Features

<table>
<tr>
<td width="50%" valign="top">

### 📚 Peer-Reviewed Resource Repository
- **Multi-Format Uploads**: PDF lecture notes, video links, and reference articles.
- **Magic-Byte Header Validation**: Server verifies genuine `%PDF-` file headers to block malicious uploads (`LocalFileSystemStorageService.java`).
- **Bayesian Reliability Scoring**: Mathematical weighted averaging prevents score manipulation on new uploads.
- **Self-Review Prevention**: Integrity check blocks students from reviewing or rating their own uploads (HTTP 400).
- **Taxonomy Filtering**: Browse by Department, Subject, Semester, and custom tags.

</td>
<td width="50%" valign="top">

### 🤖 Gemini AI Study Assistant
- **On-Demand Summarization**: Generates executive summaries, key bullet concepts, and exam revision notes for any document.
- **Document Metadata Extraction**: Analyzes document text via Apache PDFBox to suggest subjects, topics, and relevant tags.
- **24/7 Academic Doubt Solver**: Context-aware assistant providing step-by-step explanations for engineering & science concepts.
- **Resilient Fallback Mode**: Functions offline or without an API key using an internal educational knowledge base.

</td>
</tr>
<tr>
<td width="50%" valign="top">

### 🎨 Live Collaborative Whiteboard
- **Multi-User Canvas**: Real-time pen stroke, shape, and eraser broadcasting over STOMP WebSockets (`/ws-studyhub`).
- **Canvas State Snapshots**: Room hosts can save high-resolution canvas states to MySQL and restore them when rejoining.
- **Session Codes**: 6-character room codes for rapid peer study group coordination.

</td>
<td width="50%" valign="top">

### ⏱️ Focus Sessions & Doubt Forum
- **Pomodoro Timer**: Tracks focused study minutes categorized by subject.
- **Department Leaderboard**: Weekly rankings based on verified focus intervals.
- **Discussion Forum**: Threaded student Q&A with peer upvoting and author-accepted solution mechanics.
- **In-App Notifications**: Real-time STOMP alerts for review updates and accepted answers.

</td>
</tr>
</table>

---

## 🚀 Quickstart & Installation

### Option 1: Docker Compose (Fastest Setup)

Runs the complete application stack (MySQL 8 + Spring Boot 3 + React/Nginx) in isolated containers:

```bash
# 1. Clone the repository
git clone https://github.com/VenuMadhavi-77/studyhub.git
cd studyhub

# 2. Copy the environment configuration
cp .env.example .env

# 3. Build and launch all services
docker compose up --build -d

# 4. Check status
docker compose ps
```

| Service | Address | Notes |
|:---|:---|:---|
| **Frontend Web App** | [http://localhost](http://localhost) | Reverse-proxied via Nginx on port 80 |
| **Backend REST API** | [http://localhost:8080/api/v1](http://localhost:8080/api/v1) | Spring Boot application port |
| **Swagger OpenAPI Docs** | [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) | Interactive API exploration |
| **Health Check** | [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health) | System health probe |

---

### Option 2: Local Developer Setup

#### Prerequisites
- **Java JDK 21+** (`java -version`)
- **Apache Maven 3.9+** (`mvn -v`)
- **Node.js 20+ & npm** (`node -v`)
- **MySQL 8.0** running locally on port `3306`

#### 1. Configure Database
Create the database in MySQL:
```sql
CREATE DATABASE IF NOT EXISTS studyhub_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 2. Run the Backend (Spring Boot)
```bash
cd backend

# Execute test suite (41/41 tests)
mvn test

# Start backend (connects to local MySQL on port 3306)
mvn spring-boot:run
```
> The backend boots on **`http://localhost:8080`**.

#### 3. Run the Frontend (React + Vite)
```bash
cd frontend

# Install dependencies
npm install

# Start Vite development server
npm run dev
```
> The frontend runs on **`http://localhost:3000`** *(port is explicitly configured in `vite.config.ts`)*.

---

## 🤖 AI Subsystem: Live vs. Fallback

StudyHub integrates Google Gemini with an automatic fallback architecture:

```
                          ┌──────────────────────────┐
                          │   AI Request Initiated   │
                          └─────────────┬────────────┘
                                        │
                         Is GEMINI_API_KEY configured?
                                       / \
                                Yes   /   \   No / Offline
                                     v     v
                  ┌────────────────────┐ ┌────────────────────┐
                  │ Google Gemini API  │ │ Built-in Fallback  │
                  │ (gemini-1.5-flash) │ │ Educational Engine │
                  └────────────────────┘ └────────────────────┘
```

- **Live Gemini Mode**: When `GEMINI_API_KEY` is set in `.env`, StudyHub queries Google's `gemini-1.5-flash` model for dynamic document extraction, personalized revision notes, and conversational explanations.
- **Offline Educational Fallback**: If no API key is provided, the application routes requests to `GeminiFallbackProvider.java`, utilizing structured educational patterns. The platform remains completely usable without third-party API dependencies.

**Enabling Live AI:**
1. Get a free API key at [Google AI Studio](https://aistudio.google.com/app/apikey).
2. Add your key to `.env`:
   ```env
   GEMINI_API_KEY=your_actual_gemini_api_key_here
   ```
3. Restart the backend.

---

## 🏗️ System Architecture

StudyHub utilizes a modular architecture with clear domain boundaries:

```
┌────────────────────────────────────────────────────────────────────────┐
│                       React 18 Single-Page App                         │
│       TypeScript 5.6 │ Tailwind CSS │ Axios JWT │ STOMP Client         │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ HTTP REST / WebSocket
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                   Spring Security 6 Web Filter Chain                   │
│   Bucket4j RateLimitingFilter ──► JwtAuthenticationFilter ──► RBAC     │
└───────────────────┬─────────────────┬─────────────────┬────────────────┘
                    │                 │                 │
                    ▼                 ▼                 ▼
             ┌─────────────┐   ┌─────────────┐   ┌─────────────┐
             │  Resource   │   │ Gemini AI   │   │ Collaborative│
             │  & Search   │   │ Subsystem   │   │ Whiteboard  │
             └──────┬──────┘   └──────┬──────┘   └──────┬──────┘
                    │                 │                 │
                    └────────────┬────┴─────────────────┘
                                 │
                                 ▼
┌────────────────────────────────────────────────────────────────────────┐
│                 Spring Data JPA / Hibernate ORM Layer                  │
└────────────────────────────────┬───────────────────────────────────────┘
                                 │
                                 ▼
┌────────────────────────────────────────────────────────────────────────┐
│                  MySQL 8.0 Relational Persistence                      │
└────────────────────────────────────────────────────────────────────────┘
```

### Bayesian Reliability Rating Formula

To eliminate unfair ratings on new uploads and prevent manipulation, reliability is calculated mathematically in `ReliabilityService.java`:

$$R = \frac{v \cdot \bar{r} + m \cdot C}{v + m}$$

Where:
- $v$: Number of student reviews for this resource.
- $\bar{r}$: Average star rating of those reviews ($1.0 - 5.0$).
- $m$: Confidence threshold parameter ($m = 5$).
- $C$: Prior baseline rating ($C = 3.5$).

A resource with only a single 5-star review will not falsely display 100% reliability; it smoothly scales toward the true score as more peer reviews accumulate.

---

## 🔒 Security Implementation Details

Rather than relying on abstract buzzwords, StudyHub's security is backed by concrete implementations:

1. **Dual-Token JWT Lifecycle**:
   - Access tokens expire after 15 minutes (`JWT_ACCESS_EXPIRATION_MS=900000`).
   - Refresh tokens persist in MySQL as cryptographic SHA-256 hashes (`RefreshToken.java`).
2. **Refresh Token Rotation (RTR) & Breach Detection**:
   - Every refresh request generates a new token pair and revokes the previous token.
   - If an invalidated refresh token is ever re-sent, `RefreshTokenService.java` detects a replay breach and immediately deletes all active tokens for that user account.
3. **Bucket4j Rate Limiting**:
   - In-memory token bucket algorithm enforces a 20 request/minute ceiling on authentication and AI endpoints to protect against brute-force attacks (`RateLimitingFilter.java`).
4. **File Upload Hardening**:
   - Files are validated by MIME type and genuine `%PDF-` magic-byte inspection.
   - Files are assigned UUID names and stored outside the public web root to prevent direct execution and path-traversal attacks.

---

## 🧪 Verification & Testing Suites

StudyHub features three distinct, verifiable test layers:

### 1. Backend Unit & Integration Tests (41 Tests)
Run using Maven in `/backend`:
```bash
cd backend
mvn test
```
**Results: 41 Tests Run, 0 Failures, 0 Errors**
- Covers `AuthServiceTest`, `RefreshTokenServiceTest`, `ResourceServiceTest`, `SearchServiceTest`, `RatingServiceTest`, `ReviewServiceTest`, `DoubtServiceTest`, `AIServiceTest`, `NotificationServiceTest`, `AdminServiceTest`, `WhiteboardServiceTest`, `SecurityIntegrationTest`, `ReliabilityFormulaTest`, and `RecommendationServiceTest`.

### 2. Full-System Diagnostic Suite (39 Assertions)
Run using Node.js against an active instance:
```bash
node debug_suite.js
```
**Results: 39 / 39 Endpoints & Workflows Passed**
- Tests end-to-end user registration, invalid credential rejection, token rotation, PDF multipart uploads, streaming downloads, peer review submission, self-review blocks (400), doubt question creation, answer voting, whiteboard snapshots, Pomodoro timer completion, department leaderboard, and role-based access control (403).

### 3. Quick Deployment Smoke Test (16 Sanity Checks)
Run using Node.js post-deployment:
```bash
node verify_deployment.js
```
**Results: 16 / 16 Verification Checkpoints Passed**
- Validates that server health is UP, database seeding initialized correctly, and essential public & authenticated endpoints respond as expected.

---

## ⚙️ Environment Configuration

Copy `.env.example` to `.env` to configure your instance:

| Variable | Default Value | Description |
|:---|:---|:---|
| `DB_HOST` | `localhost` | MySQL host (`mysql` when using Docker Compose) |
| `DB_PORT` | `3306` | MySQL port |
| `DB_NAME` | `studyhub_db` | Database schema name |
| `DB_USERNAME` | `root` | Database username |
| `DB_PASSWORD` | *(empty)* | Database password |
| `JWT_SECRET` | *(base64 secret)* | 256-bit HMAC-SHA256 secret key |
| `JWT_ACCESS_EXPIRATION_MS` | `900000` | Access token lifespan (15 minutes) |
| `JWT_REFRESH_EXPIRATION_MS`| `604800000` | Refresh token lifespan (7 days) |
| `GEMINI_API_KEY` | *(optional)* | Google AI Studio key for live AI features |
| `STORAGE_LOCAL_PATH` | `./uploads` | Local directory for uploaded files |
| `ADMIN_BOOTSTRAP_EMAIL` | `admin@studyhub.edu` | Email for initial administrator account |
| `ADMIN_BOOTSTRAP_PASSWORD`| *(set in .env)* | Password for initial administrator account |

> ⚠️ **Security Notice:** Change `ADMIN_BOOTSTRAP_PASSWORD` in your `.env` file before launching in any shared or production environment.

---

## 👥 Seeded Development Accounts

When running in development mode (`application-dev.yml`), the database seeds sample accounts for testing:

| Account Type | Email Address | Default Password | Scope / Permissions |
|:---|:---|:---|:---|
| **System Admin** | `admin@studyhub.edu` | *Configured via `.env`* | Platform statistics, moderation queue, user management |
| **Student (CSE)** | `rohan.v@anurag.edu.in` | `Password123#` | Upload resources, ask doubts, whiteboard collaboration |
| **Student (ECE)** | `meera.p@anurag.edu.in` | `Password123#` | Review peer resources, answer doubts, focus sessions |

---

## 📡 API Endpoints Overview

| Method | Endpoint | Access Level | Description |
|:---|:---|:---|:---|
| `POST` | `/api/v1/auth/register` | Public | Register new student account |
| `POST` | `/api/v1/auth/login` | Public | Authenticate credentials and issue JWT tokens |
| `POST` | `/api/v1/auth/refresh` | Public | Atomic refresh token rotation |
| `GET` | `/api/v1/users/me` | Authenticated | Retrieve profile for authenticated user |
| `GET` | `/api/v1/taxonomy/departments` | Public | List all engineering departments |
| `GET` | `/api/v1/search/resources` | Public | Dynamic multi-criteria resource search |
| `POST` | `/api/v1/resources` | Authenticated | Multipart upload with file and JSON metadata |
| `GET` | `/api/v1/resources/{id}/download` | Authenticated | Stream and download file content |
| `POST` | `/api/v1/resources/{id}/reviews` | Authenticated | Submit peer review (Self-reviews rejected) |
| `POST` | `/api/v1/doubts` | Authenticated | Post an academic doubt question |
| `POST` | `/api/v1/doubts/{id}/answers` | Authenticated | Submit answer to a doubt question |
| `POST` | `/api/v1/whiteboard/sessions` | Authenticated | Create a collaborative whiteboard session |
| `POST` | `/api/v1/focus/sessions/start` | Student | Begin Pomodoro focus interval |
| `GET` | `/api/v1/focus/leaderboard` | Authenticated | View department weekly focus leaderboard |
| `POST` | `/api/v1/ai/doubt-assistant` | Authenticated | Ask AI doubt assistant for explanation |
| `POST` | `/api/v1/ai/summarize/{id}` | Authenticated | Generate AI executive summary & revision notes |
| `GET` | `/api/v1/admin/stats` | Admin Only | Platform statistics and metrics |
| `PATCH`| `/api/v1/admin/resources/{id}/moderation`| Admin Only | Approve or reject uploaded resource |

Full interactive API documentation is accessible via **Swagger UI** at `http://localhost:8080/swagger-ui.html`.

---

## 🚢 Production Deployment

An automated deployment script [`deploy.sh`](deploy.sh) is provided for Ubuntu VPS hosts (AWS EC2, DigitalOcean, Hetzner, Oracle Cloud):

```bash
# SSH into your clean Ubuntu server and run:
git clone https://github.com/VenuMadhavi-77/studyhub.git
cd studyhub
chmod +x deploy.sh
./deploy.sh
```

**What [`deploy.sh`](deploy.sh) automates:**
1. Installs Docker Engine & Compose plugin if not already present.
2. Auto-detects the host public IP address and configures CORS origin headers.
3. Generates high-entropy cryptographic keys for JWT secrets and database credentials.
4. Builds multi-stage production Docker containers.
5. Runs healthchecks and validates container status.

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

<div align="center">

**Developed by [VenuMadhavi-77](https://github.com/VenuMadhavi-77)**

⭐ **Star this repository** if you find it helpful for your studies or engineering projects!

</div>
