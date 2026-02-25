# Use official OpenJDK 17 image
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy project files
COPY . .

# Build application
RUN ./mvnw clean package -DskipTests

# Expose port
EXPOSE 8080

# Run app
CMD ["java", "-jar", "target/chatapp-0.0.1-SNAPSHOT.jar"]