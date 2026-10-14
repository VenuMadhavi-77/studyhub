# StudyHub — Smart AI-Powered Collaborative Academic Resource Sharing Platform

StudyHub is a modern, full-stack collaborative academic resource sharing web application built for university engineering students and faculty to share verified study notes, prevent spam through Bayesian reliability scoring, resolve academic doubts with Gemini AI, and collaborate in real-time study sessions.

---

## 1. What StudyHub Does

- **Multi-Department Academic Resource Repository**: Manage, share, and discover verified notes across Computer Science (CSE), Information Technology (IT), Electronics & Communication (ECE), and Mechanical Engineering (MECH).
- **Automated Document Header Validation (Magic-Byte)**: Upload lecture notes (PDF) with automated verification of binary `%PDF-` file headers to validate genuine academic documents and block disguised malicious uploads.
- **Bayesian Reliability Scoring & Trust Badges**: Calculates mathematical weighted trust scores ($R = \frac{v \cdot \bar{r} + m \cdot C}{v + m}$) from peer reviews, eliminating review manipulation and assigning clear reliability indicators (*"Highly reliable"*, *"Good"*, *"New"*).
- **Integrity & Self-Review Prevention**: Server-enforced security constraints prevent resource authors from rating or reviewing their own uploaded notes.
- **Ask StudyHub AI Study Assistant**: Ask questions in natural language like *"Summarize these operating systems notes"* or *"Explain Dijkstra's shortest path algorithm step-by-step"*. Powered by Google Gemini 1.5 Flash with an internal educational fallback engine!
- **Live Collaborative Whiteboard**: Real-time multi-user study whiteboard canvas over STOMP WebSockets (`/ws-studyhub`) with pen, shapes, eraser, and persistent MySQL canvas state snapshots.
- **Pomodoro Focus Sessions & Leaderboard**: Track focused study minutes categorized by subject with real-time department study leaderboards.
- **Campus Doubt Forum & Notifications**: Threaded student Q&A forum with upvoting, author-accepted solutions, and live notifications.

---

## 2. Technologies Used

### Frontend
- **React.js 18 (with Vite)**: Fast modern single-page application framework.
- **TypeScript 5.6**: Strict compile-time type safety across state models, API clients, and UI components.
- **React Router**: Client-side page navigation and protected route layout.
- **Axios**: Centralized HTTP client with JWT Authorization headers and automatic CORS handling.
- **STOMP & SockJS**: Real-time WebSocket clients for multi-user whiteboard collaboration and push notifications.
- **Lucide React**: Modern icons for academic departments, categories, and navigation.
- **Tailwind CSS**: Clean, responsive styling system with dark mode and glassmorphism UI.

### Backend
- **Java 21 LTS**: LTS version of Java.
- **Spring Boot 3.3.4**: REST API backend with Spring Web, Spring Data JPA, and Spring Validation.
- **Spring Security 6 with JWT**: Stateless authentication, short-lived access tokens, Refresh Token Rotation (RTR), and replay attack detection.
- **Bucket4j**: In-memory token bucket rate limiting on authentication and AI endpoints.
- **Apache PDFBox 3.0**: High-fidelity text extraction and metadata analysis from PDF notes.
- **MySQL 8.0**: Relational database for persistent storage with automated Flyway database migrations (`V1` to `V5`).
- **AI Intelligence**: Gemini 1.5 Flash LLM API integration with a built-in rule-based educational analysis engine fallback.

---

## 3. Project Structure

```
studyhub/
├── backend/
│   ├── src/main/java/com/studyhub/platform/
│   │   ├── common/              # Global exceptions, base responses, pagination, and enums
│   │   ├── config/              # SecurityConfig, ApplicationProperties, DevDataSeeder
│   │   ├── modules/
│   │   │   ├── admin/           # Admin moderation, reports, and platform statistics
│   │   │   ├── ai/              # Gemini AI integration, summarization, and educational fallback
│   │   │   ├── auth/            # Authentication, JWT tokens, user profiles, and password reset
│   │   │   ├── doubt/           # Student doubt forum, answers, and solution acceptance
│   │   │   ├── focus/           # Pomodoro focus timer sessions and department leaderboards
│   │   │   ├── notification/    # Live notifications and alerts
│   │   │   ├── resource/        # Resource uploads, search, reviews, and Bayesian reliability
│   │   │   ├── taxonomy/        # Departments, subjects, topics, and academic tags
│   │   │   └── whiteboard/      # Live collaborative whiteboard sessions and canvas snapshots
│   │   └── security/            # JwtAuthenticationFilter, JwtTokenProvider, RateLimitingFilter
│   ├── src/main/resources/
│   │   ├── application.yml      # Base Spring Boot configuration
│   │   ├── application-prod.yml # Production Render & cloud database configuration
│   │   └── db/migration/        # Flyway migrations (V1 schema, V2 seed, V3 reviews, V4 depts, V5 catalog)
│   ├── .env.example             # Backend environment variable template
│   └── pom.xml                  # Maven build configuration
│
└── frontend/
    ├── src/
    │   ├── components/          # Reusable components (Navbar, Sidebar, Badge, ReliabilityChip, ErrorBoundary)
    │   ├── context/             # AuthContext (login, register, token persistence), NotificationContext
    │   ├── pages/               # 15 Web Pages (Landing, Login, Register, Dashboard, Browse, ResourceDetails,
    │   │                        #  UploadResource, MyResources, AIAssistant, Whiteboard, Focus, Doubts, Admin)
    │   ├── services/            # Axios API client (api.ts), WebSocket STOMP client (websocket.ts)
    │   ├── types/               # TypeScript interfaces (Resource, Review, Department, User, Doubt)
    │   ├── App.tsx              # Main router & protected layout
    │   ├── index.css            # Complete Tailwind CSS styling system
    │   └── main.tsx             # React entry point
    ├── .env.example             # Frontend environment variable template
    ├── package.json             # NPM dependencies
    └── vite.config.ts           # Vite configuration
```

---

## 4. Required Software

Before running StudyHub, ensure you have:
1. **Java JDK 21+** installed (run `java -version` to verify).
2. **Node.js 18+ or 20+** installed (run `node -v` to verify).
3. **Apache Maven 3.9+** installed (run `mvn -v` to verify).
4. **MySQL Server 8.0** installed and running on port 3306 (default).

---

## 5. How to Set Up MySQL Database

1. Open your MySQL client (Command Prompt, Terminal, or MySQL Workbench):
```sql
CREATE DATABASE IF NOT EXISTS studyhub_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. Verify that your MySQL credentials match `backend/src/main/resources/application.yml` or your `.env` file:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/studyhub_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: root
    password: YOUR_MYSQL_PASSWORD
```
*(Flyway automatically executes migrations `V1` through `V5`, creating all tables, indexes, engineering departments, subjects, pre-seeded academic resources, and multi-user peer reviews upon boot).*

---

## 6. How to Start the Backend (Spring Boot)

Open a terminal in the `backend` directory and run:

### Windows (PowerShell / CMD):
```powershell
cd backend
mvn spring-boot:run
```

### Linux / macOS:
```bash
cd backend
./mvnw spring-boot:run
```

The Spring Boot backend will start on **`http://localhost:8080`**.
You can verify it is running by opening:
- `http://localhost:8080/actuator/health` — returns `{"status":"UP"}`.
- `http://localhost:8080/api/v1/taxonomy/departments` — returns the list of active engineering departments.

---

## 7. How to Start the Frontend (React + Vite)

Open a second terminal in the `frontend` directory and run:

```powershell
cd frontend
npm install
npm run dev
```

The frontend will start on **`http://localhost:3000`** *(or `http://localhost:5173`)*.
Open your browser and visit:
👉 **`http://localhost:3000`**

---

## 8. Quick Feature Walkthrough & Testing

1. **Landing Page (`/`)**:
   - Explore the educational hero banner, department highlights, and platform features.
   - Click **"Get Started"** to register or **"Sign In"**.

2. **Register & Login (`/register`, `/login`)**:
   - Register a student account with your Full Name, Institutional Email, Department, Semester, and Password.
   - Built-in academic department fallbacks and validation ensure immediate, smooth onboarding.

3. **Dashboard (`/dashboard`)**:
   - View your personalized academic feed based on your registered department and semester.
   - Inspect quick-access study links, recent uploads, active doubt questions, and focus session stats.

4. **Resource Catalog & Bayesian Reliability Badges (`/browse`)**:
   - Search notes by keyword or filter by Department (CSE, IT, ECE, MECH), Subject, Semester, and tags.
   - Notice the **Reliability Badges** (*"Highly reliable"*, *"Good"*): computed from peer review star ratings and review volume via the Bayesian formula.
   - Direct one-click download or open reader preview for genuine PDF notes.

5. **Resource Details & Peer Reviews (`/resources/:id`)**:
   - Inspect complete document metadata, uploader attribution, and embedded PDF document preview reader.
   - Read peer reviews and helpfulness votes left by other students and faculty.
   - Test the self-review safeguard: authors cannot rate their own uploads!

6. **Upload Academic Resource (`/upload`)**:
   - Share a lecture PDF note, syllabus cheatsheet, or video tutorial.
   - Select Department, Subject, and Semester, and add custom tags.
   - Automated server-side validation verifies the binary `%PDF-` file header before publishing.

7. **Ask StudyHub AI Study Assistant (`/ai-assistant`)**:
   - Ask academic doubts in natural language (e.g., *"Explain process scheduling algorithms in Operating Systems"*).
   - Generate instant executive summaries and key revision takeaways for uploaded lecture materials.

8. **Live Collaborative Whiteboard (`/whiteboard`)**:
   - Create or join a collaborative study session with a 6-character room code.
   - Draw, annotate, use geometric shapes, and erase in real time with classmates over WebSockets.
   - Save canvas snapshots directly to the cloud database and restore them anytime.

9. **Focus Sessions & Leaderboard (`/focus`)**:
   - Start a Pomodoro focus interval (25m / 50m) categorized by subject.
   - View your study streak and see where your department ranks on the campus focus leaderboard.

10. **Academic Doubt Forum (`/doubts`)**:
    - Post syllabus-related questions for peer and faculty review.
    - Upvote helpful answers and mark author-accepted solutions.

---

## 9. Configuring Gemini AI API (Optional)

StudyHub works **100% out of the box** even without external API keys:
- **Offline Educational Engine**: Includes a built-in rule-based knowledge engine that generates structured explanations and concept summaries.
- **Document Text Parsing**: Uses Apache PDFBox locally to extract text and analyze metadata without cloud dependencies.

### To enable live Google Gemini 1.5 Flash:
1. Obtain a free API key from [Google AI Studio](https://aistudio.google.com/).
2. Add your key to your `.env` file or environment variables:
```env
GEMINI_API_KEY=YOUR_GEMINI_API_KEY
```
3. Restart Spring Boot. The AI study assistant will automatically utilize Gemini with live conversational reasoning!

---

## 10. Common Beginner Troubleshooting Tips

- **Backend port 8080 already in use**:
  If port 8080 is in use, modify `PORT=8081` in your environment or `server.port: 8081` in `application.yml`, and set `VITE_API_URL=http://localhost:8081/api/v1` in the frontend `.env`.
- **MySQL Access Denied**:
  Ensure the database username and password in `application.yml` match your local MySQL credentials.
- **CORS Preflight (OPTIONS) Errors**:
  The backend includes `SecurityConfig.java` and `JwtAuthenticationFilter.java` explicitly permitting HTTP `OPTIONS` preflight requests from frontend domains (`localhost:3000`, `localhost:5173`, and Render URLs).
- **Node / Vite Port Conflict**:
  If port 3000 is occupied, Vite will automatically prompt or select the next open port (e.g., 5173). Check the terminal output when running `npm run dev`.
