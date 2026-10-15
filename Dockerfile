# ==============================================================================
# ATELIER PLATFORM - PRODUCTION MULTI-STAGE DOCKERFILE
# ==============================================================================
# Java Version: Eclipse Temurin OpenJDK 25 (Ubuntu Noble LTS)
# Target Cloud: Render Web Service (512MB container)
# Runtime configuration comes exclusively from Render environment variables
# (see .env.example); the Firebase service account is a Render Secret File.
# Render health check path: /actuator/health
# ==============================================================================

# ------------------------------------------------------------------------------
# STAGE 1: Build
# ------------------------------------------------------------------------------
FROM eclipse-temurin:25-jdk-noble AS builder
WORKDIR /app

# Maven wrapper and descriptor first so the dependency layer is cached
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
RUN ./mvnw dependency:go-offline -B || true

# Build the executable JAR (tests are neither compiled nor run here; CI runs them)
COPY src/main ./src/main
RUN ./mvnw clean package -Dmaven.test.skip=true -B \
    && cp "$(find target -maxdepth 1 -name '*.jar' ! -name '*original*')" /app/app.jar

# ------------------------------------------------------------------------------
# STAGE 2: Runtime
# ------------------------------------------------------------------------------
FROM eclipse-temurin:25-jre-noble AS runner
WORKDIR /app

# Unprivileged user
RUN groupadd -r spring && useradd -r -g spring spring
COPY --from=builder --chown=spring:spring /app/app.jar /app/app.jar
USER spring:spring

ENV SPRING_PROFILES_ACTIVE=prod
# JVM memory tuned for a 512MB container
ENV JAVA_OPTS="-Xms64m -Xmx160m -XX:MaxMetaspaceSize=220m -XX:ReservedCodeCacheSize=48m -Xss256k -XX:+UseSerialGC -XX:+ExitOnOutOfMemoryError"

# Render injects PORT at runtime (server.port=${PORT:8080})
EXPOSE 8080

# Shell form expands JAVA_OPTS; exec keeps java as PID 1 to receive SIGTERM
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
