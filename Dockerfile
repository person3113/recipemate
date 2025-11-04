# Stage 1: Build stage
FROM gradle:8.5-jdk21-alpine AS builder

WORKDIR /app

# Copy Gradle files first for better caching
COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle/

# Download dependencies (cached if build.gradle hasn't changed)
RUN gradle dependencies --no-daemon || true

# Copy source code
COPY src src/

# Build the application (skip tests for faster builds, run tests in CI/CD)
RUN gradle bootJar --no-daemon -x test

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Create directory for file uploads
RUN mkdir -p /app/uploads && chown -R spring:spring /app

# Copy the built JAR from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Change to non-root user
USER spring:spring

# Expose application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Set JVM options for containerized environment
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
