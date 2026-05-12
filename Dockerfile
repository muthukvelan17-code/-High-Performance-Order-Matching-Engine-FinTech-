# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests -pl trading-engine-app -am

# Run stage
FROM eclipse-temurin:21-jre-jammy
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
WORKDIR /app

# Copy the fat jar from the build stage
COPY --from=build /app/trading-engine-app/target/trading-engine-app-1.0.0-SNAPSHOT.jar /app/trading-engine.jar

# JVM tuning flags for ultra-low latency and container awareness
ENV JAVA_OPTS="-XX:+UseZGC \
               -XX:+ZGenerational \
               -XX:+UseLargePages \
               -XX:+AlwaysPreTouch \
               -Xms4g -Xmx4g \
               -XX:MaxDirectMemorySize=2g \
               -XX:+ExitOnOutOfMemoryError"

# Expose gRPC and HTTP ports
EXPOSE 9090 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar trading-engine.jar"]
