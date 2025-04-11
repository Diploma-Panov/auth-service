FROM maven:3.9.9-amazoncorretto-17

WORKDIR /app

COPY pom.xml /app
COPY settings.xml /app/settings.xml
COPY src /app/src

RUN mvn clean package -s settings.xml -Dmaven.test.skip=true

CMD ["java", "-jar", "target/app.jar"]

