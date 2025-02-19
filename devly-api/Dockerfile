FROM openjdk:21-jdk
WORKDIR /app
COPY devly-api/build/libs/*.jar app.jar
COPY devly-domain/build/libs/*.jar domain.jar
COPY devly-external/build/libs/*.jar external.jar
ENTRYPOINT exec java -jar -Dspring.profiles.active=prod app.jar
EXPOSE 8080
