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

RUN java -Djarmode=tools -jar monolith/build/libs/*.jar extract --destination /app/extracted --application-filename app.jar \
 && mkdir -p /app/layers/project /app/layers/spring /app/layers/data \
 && find /app/extracted/lib -maxdepth 1 -name '*-SNAPSHOT.jar' -exec mv -t /app/layers/project {} + \
 && find /app/extracted/lib -maxdepth 1 -name 'spring-*' -exec mv -t /app/layers/spring {} + \
 && find /app/extracted/lib -maxdepth 1 \( -name 'hibernate-*' -o -name 'postgresql-*' -o -name 'mongodb-*' -o -name 'bson*' \
    -o -name 'kafka-*' -o -name 'zstd-*' -o -name 'snappy-*' -o -name 'lz4-*' -o -name 'flyway-*' -o -name 'HikariCP-*' \
    -o -name 'jakarta.*' -o -name 'byte-buddy-*' -o -name 'antlr4-*' \) -exec mv -t /app/layers/data {} +

FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache tesseract-ocr

WORKDIR /app

COPY tesseract-data /app/tesseract-data

COPY --from=gradle_build /app/extracted/lib/ ./lib/
COPY --from=gradle_build /app/layers/data/ ./lib/
COPY --from=gradle_build /app/layers/spring/ ./lib/
COPY --from=gradle_build /app/layers/project/ ./lib/
COPY --from=gradle_build /app/extracted/app.jar ./app.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","app.jar"]
