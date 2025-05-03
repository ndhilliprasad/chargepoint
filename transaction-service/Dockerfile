# Start from a base Java image
FROM alpine/java:21-jre

# Set the working directory inside the container
WORKDIR /app

# Copy the jar file to the container
COPY build/libs/transaction-service-1.0-SNAPSHOT.jar transaction-service.jar

# Expose the port your app runs on (adjust if needed)
EXPOSE 7001

# Command to run the application
ENTRYPOINT ["java", "-jar", "transaction-service.jar"]