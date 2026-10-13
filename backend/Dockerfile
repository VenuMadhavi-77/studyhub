# Stage 1: Build the application using Java 21
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

RUN apk add --no-cache maven

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Stage 2: Run the application using Java 21 JRE
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S studyhub && \
    adduser -S studyhub -G studyhub && \
    mkdir -p /app/uploads && \
    chown -R studyhub:studyhub /app

COPY --from=builder --chown=studyhub:studyhub /build/target/*.jar app.jar

USER studyhub:studyhub

ENV SPRING_PROFILES_ACTIVE=prod \
    PORT=8080 \
    JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget -qO- "http://localhost:${PORT:-8080}/actuator/health" || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]