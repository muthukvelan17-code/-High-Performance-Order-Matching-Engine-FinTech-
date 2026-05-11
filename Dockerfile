# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
COPY . /app
WORKDIR /app
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre-jammy
COPY --from=build /app/trading-engine-app/target/*.jar /app/trading-engine.jar
WORKDIR /app

# JVM tuning flags for ultra-low latency
ENV JAVA_OPTS="-XX:+UseZGC -XX:+ZGenerational -XX:+UseLargePages -XX:+AlwaysPreTouch -Xms4g -Xmx4g -XX:MaxDirectMemorySize=2g"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar trading-engine.jar"]
