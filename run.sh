#!/bin/bash

echo "======================================================="
echo "  Ultra-Low-Latency Trading Engine - Quick Start"
echo "======================================================="
echo ""

MAVEN_CMD="mvn"

if ! command -v mvn &> /dev/null; then
    if [ ! -f ".maven/bin/mvn" ]; then
        echo "[INFO] Maven not found globally. Downloading Apache Maven locally (this is a one-time process)..."
        curl -fsSL -o maven.zip "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.zip"
        if [ ! -f "maven.zip" ]; then
            echo "[ERROR] Failed to download Maven."
            exit 1
        fi
        echo "[INFO] Extracting Maven..."
        unzip -q maven.zip -d .maven_temp
        mv .maven_temp/apache-maven-3.9.6 .maven
        rm -rf .maven_temp maven.zip
    fi
    MAVEN_CMD=".maven/bin/mvn"
fi

echo ""
echo "Step 1: Building the project with Maven..."
$MAVEN_CMD clean package -DskipTests
if [ $? -ne 0 ]; then
    echo "[ERROR] Maven build failed!"
    exit 1
fi

echo ""
echo "Step 2: Starting the Trading Engine..."
echo "(Press Ctrl+C to stop the engine)"
echo ""
$MAVEN_CMD spring-boot:run -pl trading-engine-app
