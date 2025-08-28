# Use OpenJDK 24 JDK Slim Bookworm as the base image
FROM openjdk:24-jdk-slim-bookworm

# Set working directory
WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradlew ./
COPY gradle gradle/
COPY build.gradle ./
COPY settings.gradle ./

# Make gradlew executable
RUN chmod +x ./gradlew

# Copy source code
COPY src src/

# Build the application
RUN ./gradlew build -x test

# Expose the default Spring Boot port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "build/libs/*.jar"]