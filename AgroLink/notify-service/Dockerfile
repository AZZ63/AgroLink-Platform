FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY common/ common/
COPY notify-service/ notify-service/
RUN mkdir -p user-service product-service gateway order-service
RUN mvn -pl common,notify-service -am clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /build/notify-service/target/notify-service-*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
