FROM maven:3.9.6-amazoncorretto-21 AS maven_build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN --mount=type=cache,target=/root/.m2 mvn clean package  -Dmaven.test.skip

FROM amazoncorretto:21-alpine
WORKDIR /app

COPY --from=maven_build /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
