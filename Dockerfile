FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

COPY gradlew build.gradle settings.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S crm && adduser -S crm -G crm
COPY --from=builder /app/build/libs/*.jar app.jar
RUN mkdir -p /app/uploads && chown crm:crm /app/uploads

USER crm
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
