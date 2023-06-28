#FROM openjdk:8-jdk-alpine
#FROM adoptopenjdk/openjdk11:alpine
#FROM openjdk:14-alpine
#FROM openjdk:8-jdk-alpine
#FROM openjdk:17
FROM openjdk:17
COPY build/libs/batch-payments-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
