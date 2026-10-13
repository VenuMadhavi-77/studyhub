# StudyHub — AI-Powered Collaborative Academic Resource Sharing Platform

[![Spring Boot 3.3.4](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java 21 LTS](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://www.oracle.com/java/)
[![React 18](https://img.shields.io/badge/React-18.3.1-blue.svg)](https://react.dev/)
[![Gemini AI](https://img.shields.io/badge/AI-Google%20Gemini%202.5%20Flash-purple.svg)](https://ai.google.dev/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

**StudyHub** is a production-grade, enterprise-ready full-stack academic resource platform engineered for university departments, faculty, and students. It integrates Spring Boot 3.3.4, React 18 / TypeScript, Aiven MySQL / JPA, STOMP WebSockets, and Google Gemini 2.5 Flash.

---

## 🌟 Key Architecture & Capabilities

```
+-----------------------------------------------------------------------------------+
|                                React 18 + TypeScript SPA                          |
|  (Outfit/Inter Typography, Glassmorphism, Axios JWT Interceptor, STOMP WebSockets)|
+------------------------------------------+----------------------------------------+
                                           | HTTP REST / WS Handshake
                                           v
+-----------------------------------------------------------------------------------+
|                          Spring Boot 3.3.4 Security & Filter Chain                |
|  (Bucket4j Rate Limiting -> JwtAuthenticationFilter -> SecurityFilterChain)      |
+------------------------------------------+----------------------------------------+
                                           |
    +--------------------------------------+----------------------------------+
    |                                      |                                  |
    v                                      v                                  v
+-----------------------+     +------------------------+     +----------------------+
|  Resource & Search    |     |  AI Subsystem          |     |  Collaborative Room  |
|  JPA Specifications   |     |  Apache PDFBox Text    |     |  STOMP Broadcast     |
|  Counter Deduplication|     |  Gemini 2.5 Flash JSON |     |  Snapshot Sequence   |
+-----------+-----------+     +-----------+------------+     +----------+-----------+
            |                             |                             |
            +----------------------+------+-----------------------------+
                                   |
                                   v
+-----------------------------------------------------------------------------------+
|                        Spring Data JPA / Hibernate Layer                          |
|    (@EntityGraph, Composite Indexes, Optimistic Locking, Flyway Migrations)       |
+------------------------------------------+----------------------------------------+
                                           |
                                           v
+-----------------------------------------------------------------------------------+
|                             MySQL Database / Aiven Cloud                          |
+-----------------------------------------------------------------------------------+
```

### 1. Robust Security Subsystem
- **JWT & Refresh Tokens**: Dual-token architecture with SHA-256 hashed refresh token storage in MySQL, automatic rotation on every refresh call, and replay-attack breach detection.
- **Strict Role-Based Authorization**: `ADMIN`, `FACULTY`, and `STUDENT` role hierarchies enforced at Spring Security layer (`/api/v1/admin/**`) and `@PreAuthorize` method annotations.
- **Bucket4j Rate Limiting**: Token-bucket algorithm (5 auth attempts/min, 60 global requests/min) guarding against brute-force and DDoS.
- **Defense in Depth**: PDF magic-byte header validation (`%PDF-`), secure UUID file renaming, and path-traversal sanitation (`..` stripped).

### 2. Google Gemini 2.5 Flash AI Subsystem
- **PDFBox Text Extraction**: Extracts structured text streams from uploaded course documents up to 50MB.
- **AI Document Summarizer**: Generates 2-3 paragraph executive overviews with formula/topic extraction.
- **AI Auto-Categorization**: Analyzes syllabus context to auto-fill title, department, semester, and tags during upload.
- **AI Doubt Assistant**: Produces educational step-by-step reasoning with key conceptual takeaways.
- **AI Natural Language Search**: Translates conversational queries into precise JPA Specification filters.

### 3. Synchronous Collaborative Whiteboard
- **Multi-Peer Vector Canvas**: HTML5 Canvas supporting freehand drawing, rectangles, circles, lines, erasers, and color/thickness selection.
- **STOMP Over WebSocket**: Live state broadcasting to `/topic/whiteboard.{sessionCode}`.
- **Snapshot Persistence**: Saves canvas state snapshots to MySQL for instant state restoration upon rejoining.

---

## 🚀 Quickstart Guide

### Prerequisites
- Java 21 LTS (or JDK 21+)
- Maven 3.9+
- Node.js 20+ and npm
- Docker & Docker Compose (Optional for containerized run)

### Running with Docker Compose
```bash
# 1. Clone the repository and configure environment variables
cp .env.example .env

# 2. Build and launch MySQL, Spring Boot Backend, and React Frontend
docker-compose up --build
```
- **Frontend App**: `http://localhost`
- **Backend REST API**: `http://localhost:8080/api/v1`
- **Swagger OpenAPI Docs**: `http://localhost:8080/swagger-ui/index.html`

---

### Local Development Setup

#### 1. Backend (Spring Boot 3.3.4)
```bash
cd backend
mvn clean test
mvn spring-boot:run
```
*Backend runs on `http://localhost:8080`*. Default bootstrapped administrator: `admin@studyhub.edu` / `Admin@123456`.

#### 2. Frontend (React 18 + Vite)
```bash
cd frontend
npm install
npm run dev
```
*Frontend runs on `http://localhost:3000`*.

---

## 🧪 Test Suite Verification
All backend test suites run cleanly with zero skips:
```bash
cd backend
mvn clean test
```
**Results**:
- `AuthServiceTest` (Registration, login, password validation)
- `RefreshTokenServiceTest` (Token hashing, rotation, breach defense)
- `ResourceServiceTest` (File upload, PDF validation, taxonomy mapping)
- `SearchServiceTest` (JPA Specification dynamic queries)
- `RatingServiceTest` (Atomic average calculation, anti-self-rating)
- `DoubtServiceTest` (Accepted answer workflow, reputation points)
- `AIServiceTest` (Gemini response parsing and fallback resilience)
- `NotificationServiceTest` (Real-time STOMP routing and DB persistence)
- `AdminServiceTest` (Content moderation, user account suspension)
- `WhiteboardServiceTest` (Room generation, sequence snapshot management)
- `SecurityIntegrationTest` (MockMvc 401 unauthenticated, 403 student access restrictions)

---

## 🛡️ Technical Interview Defense Guide

1. **Why use DTO records instead of JPA Entities at controller boundaries?**
   *Exposing JPA entities creates security risks (over-posting / Mass Assignment vulnerabilities), leaks database schema internals, causes LazyInitializationExceptions when serializing un-proxied relations outside transactions, and binds frontend contracts to database tables.*

2. **How does the Refresh Token Replay / Breach Defense work?**
   *Refresh tokens are single-use. When a refresh token is presented, we verify its SHA-256 hash. If a token is reused after already being revoked, the system detects a potential token theft, invalidates the entire refresh token family for that user, and forces immediate re-authentication.*

3. **How are race conditions prevented when computing resource ratings and view counters?**
   *Ratings use database transactions with atomic aggregate calculations (`AVG(score)`, `COUNT(id)`). View and download counters use an in-memory sliding de-duplication cache (1-hour window per IP/User) to prevent artificial counter inflation.*
