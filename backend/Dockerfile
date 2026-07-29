# ==========================================
# STAGE 1: Build Stage
# ==========================================
FROM maven:3.9.8-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copy Maven POM and download dependencies for layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build application JAR
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# STAGE 2: Production Runtime Stage
# ==========================================
FROM eclipse-temurin:21-jre-alpine AS runner

LABEL maintainer="HireMate Cloud Engineering Team <engineering@hiremate.com>"
LABEL description="HireMate Enterprise Recruitment Platform Backend"

# Create non-root system user and group for security hardening
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy built JAR from builder stage
COPY --from=builder /build/target/hiremate-backend-1.0.0.jar /app/hiremate-backend.jar

# Set directory permissions
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose Spring Boot default HTTP port
EXPOSE 8080

# Environment defaults
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

# Container Healthcheck via Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Entrypoint execution
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/hiremate-backend.jar"]
