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

# Stage 2: Create the final, lightweight image for the API
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy ONLY the api JAR from the build stage
RUN apk --no-cache add curl

# Copy ONLY the api JAR from the build stage
COPY --from=build /app/api/target/*.jar app.jar

# Expose the port for the API service
EXPOSE 8080

# Run the application
ENTRYPOINT ["java","-jar","app.jar"]
