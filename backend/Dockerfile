# Multi-stage build for CalculatorIot backend
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Prepare wrapper and dependencies
COPY mvnw pom.xml .
COPY .mvn .mvn
RUN chmod +x mvnw

# Copy source and build
COPY src src
RUN ./mvnw -B -DskipTests package

# Runtime image
FROM eclipse-temurin:21-jre
WORKDIR /app

# The application reads configuration from environment variables at runtime.
# Do NOT set sensitive values (passwords, API keys) in the Dockerfile.
# Pass them via docker run -e or docker-compose environment section.

COPY --from=build /app/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]

