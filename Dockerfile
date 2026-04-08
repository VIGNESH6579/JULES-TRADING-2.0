# Stage 1: Build the backend with Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
# We only copy the backend folder contents since this Dockerfile is at the root
COPY backend/pom.xml .
COPY backend/src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the Spring Boot application
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/trading-0.0.1-SNAPSHOT.jar app.jar
# Expose the dynamic Render port
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java", "-Xmx300m", "-jar", "app.jar"]
