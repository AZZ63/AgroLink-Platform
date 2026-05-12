FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY common/ common/
COPY order-service/ order-service/
RUN mkdir -p user-service product-service gateway notify-service
RUN mvn -pl common,order-service -am clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /build/order-service/target/order-service-*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
