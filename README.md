<div align="center">

# 📚 StudyHub
### AI-Powered Collaborative Academic Resource Sharing Platform

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21%20LTS-ED8B00?style=for-the-badge&logo=java&logoColor=white)](https://www.oracle.com/java/)
[![React](https://img.shields.io/badge/React-18.3-61DAFB?style=for-the-badge&logo=react&logoColor=black)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.6-3178C6?style=for-the-badge&logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Google Gemini](https://img.shields.io/badge/AI-Google%20Gemini-8E44AD?style=for-the-badge&logo=google&logoColor=white)](https://ai.google.dev/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](LICENSE)

> A full-stack, production-grade collaborative academic platform where students upload and peer-review study resources, ask AI-powered academic doubts, and collaborate in real-time whiteboards.

</div>

---

## 🌟 Features

<table>
<tr>
<td width="50%">

### 📁 Resource Sharing
- Upload PDF notes, external links, video resources
- Peer reliability ratings with Bayesian scoring
- Department & subject-based taxonomy
- Admin moderation workflow (Approve / Reject)
- Smart tag-based search with AI natural language queries
- Bookmark resources for later

</td>
<td width="50%">

### 🤖 AI Academic Assistant
- Powered by **Google Gemini 1.5 Flash**
- AI Doubt Solver with structured step-by-step answers
- AI Auto-Categorization for uploaded resources
- AI-generated summaries & exam revision notes
- Intelligent natural language search across resources
- Educational fallback engine when no API key is configured

</td>
</tr>
<tr>
<td>

### 🔒 Enterprise Security
- JWT access tokens + SHA-256 hashed refresh tokens
- Refresh token rotation with replay/breach detection
- Role-based access control (ADMIN / FACULTY / STUDENT)
- Bucket4j rate limiting (brute-force protection)
- PDF magic-byte header validation
- Path-traversal sanitization on file storage

</td>
<td>

### 🎨 Collaborative Whiteboard
- Real-time multi-peer canvas via STOMP WebSockets
- Freehand, rectangle, circle, line & eraser tools
- Color picker & stroke width controls
- Session snapshots persisted to MySQL
- Instant canvas state restoration on rejoin

</td>
</tr>
</table>

---

## 🏗️ Architecture Overview

```
┌──────────────────────────────────────────────────────────────────┐
│                    React 18 + TypeScript SPA                     │
│  Vite │ TailwindCSS │ Axios JWT Interceptor │ STOMP WebSockets   │
└───────────────────────────┬──────────────────────────────────────┘
                            │  HTTP REST + WebSocket
                            ▼
┌──────────────────────────────────────────────────────────────────┐
│              Spring Boot 3.3.4 Security Filter Chain             │
│     Bucket4j Rate Limiter → JWT Auth Filter → Spring Security    │
└──────────┬────────────────┬────────────────────┬────────────────┘
           │                │                    │
           ▼                ▼                    ▼
  ┌─────────────┐  ┌──────────────┐  ┌──────────────────┐
  │  Resource   │  │ AI Subsystem │  │  Collaborative   │
  │  & Search   │  │ Gemini Flash │  │  Whiteboard STOMP│
  │  JPA Specs  │  │ PDFBox Text  │  │  Broadcast       │
  └──────┬──────┘  └──────┬───────┘  └────────┬─────────┘
         └────────────────┴──────────────────┘
                          │
                          ▼
┌──────────────────────────────────────────────────────────────────┐
│           Spring Data JPA / Hibernate / Flyway Migrations        │
└──────────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌──────────────────────────────────────────────────────────────────┐
│                 MySQL 8.0 Database (Local / Aiven Cloud)         │
└──────────────────────────────────────────────────────────────────┘
```

---

## 🛠️ Tech Stack

| Layer | Technology |
|:---|:---|
| **Backend Framework** | Spring Boot 3.3.4, Spring Security 6, Spring Data JPA |
| **Language** | Java 21 LTS |
| **Frontend** | React 18.3, TypeScript 5.6, Vite 5 |
| **Styling** | Tailwind CSS 3, Custom Glassmorphism Design |
| **AI Engine** | Google Gemini 1.5 Flash REST API |
| **PDF Processing** | Apache PDFBox 3.0 |
| **Database** | MySQL 8.0 (Local / Aiven Cloud MySQL) |
| **ORM & Migrations** | Hibernate, Spring Data JPA, Flyway |
| **Authentication** | JWT (JJWT 0.12), Refresh Token Rotation |
| **WebSockets** | Spring STOMP, SockJS |
| **Rate Limiting** | Bucket4j 8.10 (Token Bucket Algorithm) |
| **Containerization** | Docker, Docker Compose |
| **Testing** | JUnit 5, Mockito, MockMvc (41 tests, 100% pass rate) |

---

## 🚀 Quick Start

### Option 1: Docker Compose (Recommended)

```bash
# 1. Clone the repository
git clone https://github.com/VenuMadhavi-77/studyhub.git
cd studyhub

# 2. Copy environment file and configure credentials
cp .env.example .env
# (Edit .env to set DB passwords, JWT secret, and optional GEMINI_API_KEY)

# 3. Build and launch all 3 services: MySQL + Spring Boot + React/Nginx
docker compose up --build

# 4. Verify everything is running
docker compose ps
```

| Service | URL |
|:---|:---|
| **Frontend Web App** | `http://localhost` |
| **Backend REST API** | `http://localhost:8080/api/v1` |
| **Swagger OpenAPI Docs** | `http://localhost:8080/swagger-ui/index.html` |
| **Actuator Health** | `http://localhost:8080/actuator/health` |

---

### Option 2: Local Development

#### Backend (Spring Boot)
```bash
cd backend
# Run tests first (41/41 should pass)
mvn clean test

# Start the backend (dev profile connects to local MySQL)
mvn spring-boot:run
```
> Backend starts on **http://localhost:8080**

#### Frontend (React + Vite)
```bash
cd frontend
npm install
npm run dev
```
> Frontend starts on **http://localhost:3000**

---

### Prerequisites

| Tool | Minimum Version |
|:---|:---|
| Java JDK | 21 LTS |
| Apache Maven | 3.9+ |
| Node.js | 20+ |
| MySQL | 8.0 |
| Docker _(optional)_ | 24+ |

---

## ⚙️ Environment Variables

Copy `.env.example` to `.env` and configure:

```env
# Database
DB_HOST=localhost
DB_PORT=3306
DB_NAME=studyhub_db
DB_USERNAME=root
DB_PASSWORD=your_password

# Security
JWT_SECRET=your-256-bit-secret-key-minimum-32-characters-long

# AI (Optional — uses educational fallback if empty)
GEMINI_API_KEY=
```

> **To enable live Google Gemini AI:** Get a free API key from [Google AI Studio](https://aistudio.google.com/app/apikey) and paste it into `GEMINI_API_KEY`.

---

## 👥 Demo Accounts

| Role | Email | Password |
|:---|:---|:---|
| **Admin** | `admin@studyhub.edu` | `AdminStudyHub#2026` |
| **Student** | `rohan.v@anurag.edu.in` | `Password123#` |
| **Student** | `meera.p@anurag.edu.in` | `Password123#` |

---

## 🧪 Test Suite

```bash
cd backend
mvn clean test
```

**41 tests, 0 failures, 0 errors:**

| Test Class | Coverage |
|:---|:---|
| `AuthServiceTest` | Registration, login, password hashing |
| `RefreshTokenServiceTest` | Token rotation, breach detection |
| `ResourceServiceTest` | File upload, PDF validation, taxonomy |
| `SearchServiceTest` | JPA Specification dynamic queries |
| `RatingServiceTest` | Bayesian scoring, anti-self-rating |
| `ReviewServiceTest` | Peer review workflow |
| `DoubtServiceTest` | Accepted answers, reputation points |
| `AIServiceTest` | Gemini parsing, fallback resilience |
| `NotificationServiceTest` | STOMP routing, DB persistence |
| `AdminServiceTest` | Moderation, user suspension |
| `WhiteboardServiceTest` | Room generation, snapshot management |
| `SecurityIntegrationTest` | 401/403 unauthorized access blocks |
| `ReliabilityFormulaTest` | Bayesian reliability recalculation |
| `RecommendationServiceTest` | AI-based resource recommendations |

---

## 📁 Project Structure

```
studyhub/
├── backend/                          # Spring Boot 3.3.4 Application
│   ├── src/main/java/
│   │   └── com/studyhub/platform/
│   │       ├── config/               # Security, CORS, JPA configs
│   │       ├── modules/
│   │       │   ├── auth/             # JWT auth, refresh tokens, password reset
│   │       │   ├── resource/         # Upload, search, ratings, bookmarks
│   │       │   ├── ai/               # Gemini API client, doubt assistant
│   │       │   ├── taxonomy/         # Departments & subjects
│   │       │   ├── doubt/            # Doubt forum Q&A
│   │       │   ├── whiteboard/       # Real-time collaborative canvas
│   │       │   ├── focus/            # Pomodoro focus timer
│   │       │   └── admin/            # Admin moderation
│   │       └── security/             # JWT filters, rate limiting
│   ├── src/main/resources/
│   │   ├── db/migration/             # Flyway SQL schema migrations (V1-V4)
│   │   ├── application.yml           # Main configuration
│   │   ├── application-dev.yml       # Local dev profile
│   │   └── application-prod.yml      # Production profile
│   └── Dockerfile
│
├── frontend/                         # React 18 + TypeScript + Vite SPA
│   ├── src/
│   │   ├── pages/                    # Route-level page components
│   │   ├── components/               # Reusable UI components
│   │   ├── context/                  # AuthContext, NotificationContext
│   │   ├── services/                 # Axios API service, WebSocket service
│   │   └── types/                    # Shared TypeScript interfaces
│   ├── nginx.conf                    # Production Nginx with API reverse-proxy
│   └── Dockerfile
│
├── docker-compose.yml                # Full stack orchestration
├── .env.example                      # Environment variable template
├── deploy.sh                         # Automated VPS deployment script
└── verify_deployment.js              # 16-step end-to-end integration tests
```

---

## 🚢 Production Deployment (VPS: AWS EC2 / DigitalOcean / Oracle Cloud)

A one-command automated deployment script is included:

```bash
# SSH into your Ubuntu 22.04 / 24.04 server and run:
git clone https://github.com/VenuMadhavi-77/studyhub.git
cd studyhub
chmod +x deploy.sh
./deploy.sh
```

`deploy.sh` automatically:
- ✅ Installs Docker & Docker Compose (if not present)
- ✅ Generates cryptographically secure passwords & JWT secrets
- ✅ Auto-detects your server's public IP and configures CORS
- ✅ Builds & launches all 3 Docker containers
- ✅ Verifies all container health checks

---

## 📊 End-to-End Deployment Verification

Run the 16-step integration test suite to verify the entire platform:

```bash
node verify_deployment.js
```

**Expected output: 16/16 PASSED** ✅
```
1.  Backend /actuator/health              ✅ UP
2.  Departments loaded (5 depts)          ✅ PASS
3.  CSE Subjects (9 subjects)             ✅ PASS
4.  Student A Registration                ✅ PASS
5.  Student A Auth (JWT)                  ✅ PASS
6.  Student B (Reviewer) Auth (JWT)       ✅ PASS
7.  Admin JWT Auth                        ✅ PASS
8.  Multipart Resource Upload             ✅ PASS
9.  Storage File Persisted on Disk        ✅ PASS
10. Public Search API                     ✅ PASS
11. Peer Rating & Bayesian Reliability    ✅ PASS
12. Integrity Rule: Self-Review Blocked   ✅ PASS
13. Bookmark Action                       ✅ PASS
14. RBAC: Student blocked from Admin API  ✅ PASS
15. Admin Moderation Execution            ✅ PASS
16. AI Academic Assistant Response        ✅ PASS
=== OVERALL: ALL PASSED (16/16) ===
```

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit your changes: `git commit -m 'feat: add amazing feature'`
4. Push to the branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

---

## 📄 License

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for details.

---

<div align="center">

Built with ❤️ for the academic community

**[⭐ Star this repo](https://github.com/VenuMadhavi-77/studyhub)** if you found it helpful!

</div>
