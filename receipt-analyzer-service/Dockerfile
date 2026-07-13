FROM maven:3.9.9-amazoncorretto-23 AS maven_build

WORKDIR /app

COPY pom.xml .
COPY monolith ./monolith
COPY app ./app
COPY adapters ./adapters
COPY domain ./domain

RUN --mount=type=cache,target=/root/.m2 mvn clean package -Dmaven.test.skip -T 1.5C

FROM eclipse-temurin:23-jre-alpine

RUN apk add --no-cache tesseract-ocr

WORKDIR /app

COPY --from=maven_build /app/monolith/target/*.jar app.jar

COPY tesseract-data /app/tesseract-data

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","app.jar"]
