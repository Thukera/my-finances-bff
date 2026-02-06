FROM eclipse-temurin:17-jdk-jammy
WORKDIR /

# Copy JAR
COPY my-finances-bff.jar ./my-finances-bff.jar

# ✅ Copy your config folder into the image
#COPY config ./config

EXPOSE 9090
CMD ["java", "-jar", "my-finances-bff.jar"]
