FROM gradle:8.7.0-jdk21 AS builder
WORKDIR /app

COPY . .

RUN ./gradlew bootJar -x test

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/identity_and_access_management-*.jar app.jar

EXPOSE 4200

ENTRYPOINT ["java", "-jar", "app.jar"]