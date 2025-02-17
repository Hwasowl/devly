FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY devly-api/build/libs/*.jar app.jar
COPY devly-domain/build/libs/*.jar domain.jar
COPY devly-external/build/libs/*.jar external.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
EXPOSE 8080
