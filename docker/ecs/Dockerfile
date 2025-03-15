FROM maven:3.9.9-amazoncorretto-17

WORKDIR /app

COPY pom.xml /app
COPY src /app/src

RUN mvn clean package -Dmaven.test.skip=true

CMD ["java", "-jar", "target/app.jar"]

