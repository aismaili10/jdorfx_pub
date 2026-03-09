#!/bin/bash

# JDORFX Development Test Script
# Tests the compiled JDORFX library with a sample

# Set environment variables
export BSF4OOREXX_HOME="/opt/BSF4ooRexx850"
export _JAVA_OPTIONS="-Dprism.order=es2 -Dprism.forceGPU=true"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}JDORFX Development Test${NC}"
echo "=========================="

# Check if development JAR exists
if [ ! -f "lib/jdorfx-dev.jar" ]; then
    echo -e "${RED}Error: Development JAR not found. Run ./build.sh first${NC}"
    exit 1
fi

# Copy development JAR to BSF4ooRexx lib directory (backup original ONCE)
if [ ! -f "$BSF4OOREXX_HOME/lib/jdorfx_20240527.jar.backup" ]; then
    if [ -f "$BSF4OOREXX_HOME/lib/jdorfx_20240527.jar" ]; then
        echo "Creating backup of original JAR (one-time)..."
        sudo mv "$BSF4OOREXX_HOME/lib/jdorfx_20240527.jar" "$BSF4OOREXX_HOME/lib/jdorfx_20240527.jar.backup"
    fi
else
    echo "Backup exists (preserving original stable version)"
fi

echo "Installing development JAR..."
sudo cp "lib/jdorfx-dev.jar" "$BSF4OOREXX_HOME/lib/jdorfx-dev.jar"

echo -e "${GREEN}Development JAR installed!${NC}"

# Test with a sample
if [ "$1" ]; then
    SAMPLE="$1"
else
    SAMPLE="samples/01_jdorfx_drawing2d.rxj"
fi

if [ -f "$SAMPLE" ]; then
    echo "Testing with sample: $SAMPLE"
    echo "Running: rexx $SAMPLE"
    cd samples
    rexx "$(basename "$SAMPLE")"
else
    echo -e "${YELLOW}Sample not found: $SAMPLE${NC}"
    echo "Available samples:"
    ls samples/*.rxj
fi
