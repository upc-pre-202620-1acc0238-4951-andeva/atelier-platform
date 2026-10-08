# ==============================================================================
# ATELIER PLATFORM - PRODUCTION MULTI-STAGE DOCKERFILE
# ==============================================================================
# Java Version: Eclipse Temurin OpenJDK 25 (Ubuntu Noble LTS)
# Target Cloud: Render Web Service (Free Tier / Containerized Runtime)
# ==============================================================================

# ------------------------------------------------------------------------------
# STAGE 1: Build & Dependency Resolution
# ------------------------------------------------------------------------------
FROM eclipse-temurin:25-jdk-noble AS builder
WORKDIR /app

# Copy Maven wrapper configuration and descriptor
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Sanitize line endings and make wrapper executable
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# Warm up dependency cache (optimizes repeated cloud builds)
RUN ./mvnw dependency:go-offline -B || true

# Copy complete source code
COPY src ./src

# Build production Spring Boot executable JAR skipping unit tests
RUN ./mvnw clean package -DskipTests -B

# Copy the generated executable JAR to a predictable location
RUN cp $(find target -maxdepth 1 -name "*.jar" ! -name "*original*") /app/app.jar

# ------------------------------------------------------------------------------
# STAGE 2: Lightweight Production Runtime
# ------------------------------------------------------------------------------
FROM eclipse-temurin:25-jre-noble AS runner
WORKDIR /app

# Run as dedicated unprivileged user for container security
RUN groupadd -r spring && useradd -r -g spring spring

# Copy pre-packaged artifact from builder stage
COPY --from=builder --chown=spring:spring /app/app.jar /app/app.jar

# Switch to non-root user
USER spring:spring

# Default environment configuration
ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=prod
# Tune JVM memory ergonomics strictly within 512MB RAM cloud containers (Render Free Tier)
ENV JAVA_OPTS="-Xms64m -Xmx160m -XX:MaxMetaspaceSize=220m -XX:ReservedCodeCacheSize=48m -Xss256k -XX:+UseSerialGC -XX:+ExitOnOutOfMemoryError"

EXPOSE 8080

# Use exec in shell form to support dynamic JAVA_OPTS & PORT, and pass UNIX signals
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Dserver.port=${PORT} -jar /app/app.jar"]
