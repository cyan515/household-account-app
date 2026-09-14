FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /workspace

COPY gradle ./gradle
COPY gradlew build.gradle.kts settings.gradle.kts gradle.properties ./
COPY src ./src

RUN ./gradlew installDist --no-daemon

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY --from=build --chown=10001:10001 /workspace/build/install/household-account-app/ ./

USER 10001:10001
EXPOSE 8080

ENTRYPOINT ["/app/bin/household-account-app"]
