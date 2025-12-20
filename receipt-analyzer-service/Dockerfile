FROM maven:3.9.6-amazoncorretto-21 AS maven_build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN --mount=type=cache,target=/root/.m2 mvn clean package -P dev  -Dmaven.test.skip -T 1C

FROM eclipse-temurin:21-jdk

RUN apt-get update -y && apt-get install -y tesseract-ocr libtesseract-dev libleptonica-dev pkg-config

WORKDIR /app

COPY --from=maven_build /app/target/*.jar app.jar

COPY tesseract-data /app/tesseract-data

ENTRYPOINT ["java","-agentlib:jdwp=transport=dt_socket,address=*:5005,server=y,suspend=n","-Djava.security.egd=file:/dev/./urandom","-jar","app.jar"]
