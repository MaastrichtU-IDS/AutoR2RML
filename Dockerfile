FROM maven:3-jdk-8

LABEL maintainer "Alexander Malic <alexander.malic@maastrichtuniversity.nl>"

ENV APP_DIR /app
ENV TMP_DIR /tmp/build

WORKDIR $TMP_DIR

# Only runs if pom.xml changes. To avoid downloading dependencies everytime.
COPY pom.xml .
RUN mvn verify clean --fail-never

COPY src/ ./src/
RUN mvn package -Dmaven.test.skip=true && \
    mkdir $APP_DIR && \
    mv target/autor2rml-0.0.1-SNAPSHOT-jar-with-dependencies.jar $APP_DIR/autor2rml.jar && \
    rm -rf $TMP_DIR

WORKDIR $APP_DIR

ENTRYPOINT ["java","-jar","/app/autor2rml.jar"]
