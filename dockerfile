FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY target/my-finances-bff.jar ./my-finances-bff.jar

EXPOSE 9090

CMD ["java", "-jar", "my-finances-bff.jar"]
