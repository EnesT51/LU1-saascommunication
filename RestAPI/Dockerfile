FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw
COPY mvnw.cmd mvnw.cmd
COPY src src

RUN mvn -B -DskipTests package

# Download OpenTelemetry Java agent in the build stage (maven image has curl)
RUN curl -L -o /otel-agent.jar \
    https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.9.0/opentelemetry-javaagent.jar

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /otel-agent.jar /otel-agent.jar
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-javaagent:/otel-agent.jar", "-jar", "/app/app.jar"]
