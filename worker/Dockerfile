# Stage 1: Build the entire application using Maven
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy all pom.xml files to leverage Docker's layer caching
COPY pom.xml .
COPY common/pom.xml ./common/
COPY api/pom.xml ./api/
COPY worker/pom.xml ./worker/
RUN mvn -B dependency:go-offline

# Copy the rest of your source code
COPY . .

# Build the entire project. Maven will handle the module order automatically.
RUN mvn clean package -DskipTests

# Stage 2: Create the final, lightweight image for the Worker
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install Docker CLI (necessary for your DockerRunner service)
RUN apk add --no-cache docker-cli

# Copy ONLY the worker JAR from the build stage
COPY --from=build /app/worker/target/*.jar app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
