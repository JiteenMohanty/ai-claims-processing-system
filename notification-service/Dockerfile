# ============================================================================
# Multi-Stage Build: Notification Service
# Stage 1 — Build and package with Maven
# Stage 2 — Extract layered JAR
# Stage 3 — Minimal production runtime image
# ============================================================================

FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml ./

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp dependency:go-offline

COPY src ./src

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp clean package -DskipTests


FROM eclipse-temurin:21-jre-alpine AS layers

WORKDIR /workspace

COPY --from=build /workspace/target/*.jar app.jar

RUN java -Djarmode=layertools -jar app.jar extract


FROM eclipse-temurin:21-jre-alpine

LABEL org.opencontainers.image.title="notification-service" \
      org.opencontainers.image.description="Spring Boot Notification Service" \
      org.opencontainers.image.vendor="Jiteen Claims" \
      org.opencontainers.image.version="0.0.1"

RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

COPY --from=layers --chown=spring:spring /workspace/dependencies/ ./
COPY --from=layers --chown=spring:spring /workspace/spring-boot-loader/ ./
COPY --from=layers --chown=spring:spring /workspace/snapshot-dependencies/ ./
COPY --from=layers --chown=spring:spring /workspace/application/ ./

USER spring:spring

EXPOSE 8084

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8084/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
