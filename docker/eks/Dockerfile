FROM maven:3.9.9-amazoncorretto-17

ARG AWS_ACCESS_KEY_ID
ARG AWS_SECRET_ACCESS_KEY
ARG AWS_DEFAULT_REGION

ENV AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID
ENV AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY
ENV AWS_DEFAULT_REGION=$AWS_DEFAULT_REGION

WORKDIR /app

COPY pom.xml /app
COPY settings.xml /app/settings.xml
COPY src /app/src

RUN mvn clean package -s settings.xml -Dmaven.test.skip=true && \
    unset AWS_ACCESS_KEY_ID AWS_SECRET_ACCESS_KEY AWS_DEFAULT_REGION

CMD ["java", "-jar", "target/app.jar"]

