#!/bin/bash
# Build script for RealEstateHub project

echo "🏗️  Building RealEstateHub..."
echo "================================"

cd "$(dirname "$0")"

# Check if mvn is available
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven not found!"
    echo ""
    echo "Please install Maven first:"
    echo "  brew install maven"
    exit 1
fi

# Clean and compile
echo "📦 Cleaning and compiling..."
mvn clean compile

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Build successful!"
    echo ""
    echo "🧪 Running tests..."
    mvn test
    
    if [ $? -eq 0 ]; then
        echo ""
        echo "✅ All tests passed!"
    else
        echo ""
        echo "⚠️  Some tests failed. Check output above."
    fi
else
    echo ""
    echo "❌ Build failed! Check errors above."
    exit 1
fi
