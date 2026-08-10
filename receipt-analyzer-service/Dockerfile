FROM gradle:9.3.0-jdk21 AS gradle_build

WORKDIR /app

COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY monolith ./monolith
COPY app ./app
COPY adapters ./adapters
COPY domain ./domain
COPY gradlew gradlew.bat ./

RUN --mount=type=cache,target=/home/gradle/.gradle gradle :monolith:bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache tesseract-ocr

WORKDIR /app

COPY --from=gradle_build /app/monolith/build/libs/*.jar app.jar

COPY tesseract-data /app/tesseract-data

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","app.jar"]
