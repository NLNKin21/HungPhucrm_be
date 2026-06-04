# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM gradle:8.5-jdk21 AS build
WORKDIR /app

# Copy gradle files trước để cache dependencies
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon -q || true

# Copy source và build
COPY src ./src
RUN gradle bootJar --no-daemon -q

# ── Stage 2: Run ──────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN mkdir -p /app/uploads

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]