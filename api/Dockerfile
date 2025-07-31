# Build stage
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy poms and download dependencies to leverage Docker cache
COPY pom.xml .
COPY api/pom.xml ./api/
COPY worker/pom.xml ./worker/
# Using a specific version of the plugin for reproducibility
RUN mvn -B de.qaware.maven:go-offline-maven-plugin:1.2.9:go-offline -f pom.xml

# Copy source and build the application
COPY . .
RUN mvn -f pom.xml -pl api -am clean package -DskipTests

# Final stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copy the built jar from the build stage
COPY --from=build /app/api/target/*.jar app.jar

# Expose the port internally for the container
EXPOSE 8080

# Run the application
ENTRYPOINT ["java","-jar","app.jar"]
