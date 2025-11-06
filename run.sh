#!/bin/bash

# Personal Finance Manager - Run Script
# This script builds and runs the application

set -e  # Exit on error

echo "╔════════════════════════════════════════════════════════╗"
echo "║   Personal Finance Manager - Build & Run              ║"
echo "╚════════════════════════════════════════════════════════╝"
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven first."
    echo "   Visit: https://maven.apache.org/install.html"
    exit 1
fi

# Check Java version
echo "🔍 Checking Java version..."
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ Java 17 or higher is required. Current version: $JAVA_VERSION"
    exit 1
fi
echo "✓ Java version: $(java -version 2>&1 | head -n 1)"
echo ""

# Clean and compile
echo "🔨 Building project..."
mvn clean compile -q

if [ $? -ne 0 ]; then
    echo "❌ Build failed!"
    exit 1
fi

echo "✓ Build successful!"
echo ""

# Check if JAR exists, if not - package it
if [ ! -f "target/finance-manager.jar" ]; then
    echo "📦 Creating JAR package..."
    mvn package -DskipTests -q
    echo "✓ Package created!"
    echo ""
fi

# Run the application
echo "🚀 Starting Personal Finance Manager..."
echo "════════════════════════════════════════════════════════"
echo ""

java -jar target/finance-manager.jar

echo ""
echo "════════════════════════════════════════════════════════"
echo "✓ Application terminated"

