# ---------- Build stage ----------
FROM maven:3.8.5-openjdk-8 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# ---------- Runtime stage ----------
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/target/my-finances-bff.jar ./my-finances-bff.jar
EXPOSE 9090
CMD ["java", "-jar", "my-finances-bff.jar"]