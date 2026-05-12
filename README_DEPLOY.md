# Deployment Guide: Ultra-Low Latency Trading Engine

This project is optimized for deployment using **Docker** and **Spring Boot**.

## 🚀 Quick Start (Docker Compose)

The easiest way to deploy the engine is using Docker Compose. This will build the project from source and start the container with optimized JVM settings.

```bash
docker-compose up -d --build
```

### Expose Ports:
- `8080`: HTTP Dashboard & Actuator (Health, Metrics)
- `9090`: gRPC Server for order submission

---

## 🛠️ Build Artifacts Manually

If you prefer to run the project on a bare-metal server or a VM:

1. **Build the Fat JAR**:
   ```bash
   mvn clean package -DskipTests -pl trading-engine-app -am
   ```
2. **Run the JAR**:
   ```bash
   java -XX:+UseZGC -Xms4g -Xmx4g -jar trading-engine-app/target/trading-engine-app-1.0.0-SNAPSHOT.jar
   ```

---

## 📈 Monitoring & Health

The system includes **Spring Boot Actuator** for production monitoring:

- **Health Check**: `http://localhost:8080/actuator/health`
- **Prometheus Metrics**: `http://localhost:8080/actuator/prometheus`
- **Application Info**: `http://localhost:8080/actuator/info`

---

## ⚡ JVM Tuning for Production

For ultra-low latency, the following flags are pre-configured in the Dockerfile:

| Flag | Purpose |
|------|---------|
| `-XX:+UseZGC` | Low-pause garbage collector. |
| `-XX:+ZGenerational` | Optimized memory management for modern Java 21+. |
| `-XX:+AlwaysPreTouch` | Pre-allocates heap memory to avoid runtime page faults. |
| `-XX:+UseLargePages` | Improves memory access speed. |
| `-Xms4g -Xmx4g` | Fixed heap size to prevent dynamic resizing pauses. |

---

## 📂 Persistence

By default, the engine uses **Chronicle Map** for off-heap persistence. Data is stored in the `./data` directory relative to the project root. In Docker, this is mounted as a volume to ensure data persists across restarts.
