# Use the official gradle image to create a build artifact.
FROM gradle:7.6-jdk17 AS build

# Copy local code to the container image.
COPY build.gradle.kts .
COPY gradle.properties .
COPY src ./src

# Build a release artifact.
RUN gradle installDist

FROM openjdk:17-jdk
RUN microdnf install findutils
EXPOSE 8080:8080
RUN mkdir /app
COPY --from=build /home/gradle/build/install/gradle /app/
WORKDIR /app/bin

COPY wait-for-it.sh /usr/local/bin/wait-for-it.sh
RUN chmod +x /usr/local/bin/wait-for-it.sh

CMD ["sh", "-c", "/usr/local/bin/wait-for-it.sh db:5432 -- ./gradle"]
