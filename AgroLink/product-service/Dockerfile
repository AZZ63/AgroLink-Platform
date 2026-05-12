FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY common/ common/
COPY product-service/ product-service/
RUN mkdir -p user-service gateway order-service notify-service
RUN mvn -pl common,product-service -am clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /build/product-service/target/product-service-*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
