# ---------- Build stage ----------
FROM maven:3.8.5-openjdk-8 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# ---------- Runtime stage ----------
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy the JAR file
COPY --from=builder /app/target/my-finances-bff.jar ./my-finances-bff.jar

# Create logs directory with proper permissions
RUN mkdir -p /app/logs && chmod 755 /app/logs

# Create uploads directory as well
RUN mkdir -p /app/uploads && chmod 755 /app/uploads

# Declare volumes for persistent data
VOLUME ["/app/logs", "/app/uploads"]

EXPOSE 9090
CMD ["java", "-jar", "my-finances-bff.jar"]