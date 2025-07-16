FROM maven:3.9.6-amazoncorretto-21 AS maven_build

WORKDIR /app

COPY pom.xml .
COPY src ./src

#DEV profile ON
RUN --mount=type=cache,target=/root/.m2 mvn clean package -P dev  -Dmaven.test.skip
#DEV profile OFF
#RUN --mount=type=cache,target=/root/.m2 mvn clean package -P dev  -Dmaven.test.skip

FROM amazoncorretto:21-alpine
WORKDIR /app

COPY --from=maven_build /app/target/*.jar app.jar

ENTRYPOINT ["java","-agentlib:jdwp=transport=dt_socket,address=*:5005,server=y,suspend=n","-Djava.security.egd=file:/dev/./urandom","-jar","app.jar"]
