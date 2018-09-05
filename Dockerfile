FROM maven:3-jdk-8

LABEL maintainer "Alexander Malic <alexander.malic@maastrichtuniversity.nl>"

ENV APP_DIR /app
ENV TMP_DIR /tmp/build

WORKDIR $TMP_DIR

COPY . .

RUN mvn clean install -Dmaven.test.skip=true

RUN mkdir $APP_DIR && \
    mv target/autor2rml-0.0.1-SNAPSHOT-jar-with-dependencies.jar $APP_DIR/autorml.jar && \
    rm -rf $TMP_DIR
    
WORKDIR $APP_DIR

ENTRYPOINT ["java","-jar","autorml.jar"]
