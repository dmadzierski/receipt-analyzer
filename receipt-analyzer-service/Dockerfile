FROM maven:3.9.6-amazoncorretto-21 AS maven_build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN --mount=type=cache,target=/root/.m2 mvn clean package -P dev  -Dmaven.test.skip -T 1.5C

FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache tesseract-ocr

WORKDIR /app

COPY --from=maven_build /app/target/*.jar app.jar

COPY tesseract-data /app/tesseract-data

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","app.jar"]
