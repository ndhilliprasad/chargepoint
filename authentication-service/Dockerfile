# Start from a base Java image
FROM alpine/java:21-jre

# Set the working directory inside the container
WORKDIR /app

# Copy the jar file to the container
COPY build/libs/authentication-service-1.0-SNAPSHOT.jar authentication-service.jar

# Expose the port your app runs on (adjust if needed)
EXPOSE 7002

# Command to run the application
ENTRYPOINT ["java", "-jar", "authentication-service.jar"]