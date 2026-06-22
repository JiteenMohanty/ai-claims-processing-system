# ============================================================================
# Stage 1: Build the application using Maven and BuildKit caching
# ============================================================================
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Pre-cache dependencies by isolating pom.xml definition changes
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B -ntp dependency:go-offline

# Copy application code layers and compile localized code packages
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -ntp clean package -DskipTests

# ============================================================================
# Stage 2: Extract execution layers using Spring Boot layertools module
# ============================================================================
FROM eclipse-temurin:21-jre-alpine AS layers
WORKDIR /workspace

COPY --from=build /workspace/target/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# ============================================================================
# Stage 3: Construct production-grade hardened micro-runtime image container
# ============================================================================
FROM eclipse-temurin:21-jre-alpine

LABEL title="claim-service" \
      description="Spring Boot Claim Service" \
      source="com.jiteen.claims.claim" \
      vendor="Jiteen Claims" \
      version="0.0.1"

# Establish non-root process ownership context boundaries for security compliance
RUN addgroup -S spring && adduser -S spring -G spring
WORKDIR /app

# Stream out granular isolated image layer caches for dependencies and application code
COPY --from=layers --chown=spring:spring /workspace/dependencies/ ./
COPY --from=layers --chown=spring:spring /workspace/spring-boot-loader/ ./
COPY --from=layers --chown=spring:spring /workspace/snapshot-dependencies/ ./
COPY --from=layers --chown=spring:spring /workspace/application/ ./

# Enforce secure least-privilege non-root execution permissions matrix

RUN mkdir -p /app/uploads && \
    chown -R spring:spring /app/uploads

USER spring:spring

EXPOSE 8082

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]