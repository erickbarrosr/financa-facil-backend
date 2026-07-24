# ---- Stage 1: Build ----
FROM maven:3-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml ./
RUN mvn dependency:go-offline --no-transfer-progress 2>/dev/null || true
COPY src/ src/
RUN mvn package -DskipTests --no-transfer-progress

# ---- Stage 2: Runtime ----
FROM eclipse-temurin:21-jre-alpine
RUN apk upgrade --no-cache
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
RUN chown appuser:appgroup app.jar
USER appuser
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
  CMD wget -qO- http://localhost:${SERVER_PORT:-8080}/api/v1/actuator/health | grep -q '"status":"UP"' || exit 1
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]
