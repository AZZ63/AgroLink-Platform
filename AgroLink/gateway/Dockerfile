FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY common/ common/
COPY gateway/ gateway/
RUN mkdir -p user-service product-service order-service notify-service
RUN mvn -pl common,gateway -am clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /build/gateway/target/gateway-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
