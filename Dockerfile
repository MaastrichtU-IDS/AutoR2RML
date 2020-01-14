# Image to build the jar
FROM maven:3-jdk-8 as build

# Avoid to download dependencies if no change in pom.xml
COPY ./pom.xml ./pom.xml
RUN mvn verify clean --fail-never

COPY ./src ./src
RUN mvn package -Dmaven.test.skip=true

# Final image
FROM openjdk:8-jre-alpine

LABEL maintainer  "Vincent Emonet <vincent.emonet@maastrichtuniversity.nl>"

COPY --from=build target/autor2rml*-jar-with-dependencies.jar /app/autor2rml.jar

WORKDIR /app

ENTRYPOINT ["java","-jar","/app/autor2rml.jar"]