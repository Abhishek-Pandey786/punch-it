#!/bin/bash
# Linux/Mac script to run Attendance Manager

echo "===================================="
echo "  Attendance Manager - Launcher"
echo "===================================="
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "ERROR: Maven is not installed or not in PATH"
    echo "Please install Maven from: https://maven.apache.org/download.cgi"
    exit 1
fi

echo "[1/3] Checking Java version..."
java -version
if [ $? -ne 0 ]; then
    echo "ERROR: Java is not installed or not in PATH"
    echo "Please install Java 17+ from: https://adoptium.net/"
    exit 1
fi

echo ""
echo "[2/3] Building project..."
mvn clean compile
if [ $? -ne 0 ]; then
    echo "ERROR: Build failed!"
    exit 1
fi

echo ""
echo "[3/3] Launching Attendance Manager..."
echo ""
mvn javafx:run
