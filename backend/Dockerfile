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

# Runtime configuration via env vars
ENV SPRING_PROFILES_ACTIVE=default \
    SPRING_DATASOURCE_URL= \
    SPRING_DATASOURCE_USERNAME= \
    SPRING_DATASOURCE_PASSWORD= \
    GROQ_API_KEY=

COPY --from=build /app/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]

