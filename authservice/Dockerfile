
FROM amazoncorretto:21

WORKDIR /app

# Copy the JAR file from the host to the container
COPY app/build/libs/app.jar /app/app.jar


EXPOSE 9811

ENTRYPOINT ["java", "-jar", "/app/app.jar"]