#!/bin/bash

# JDORFX Build Script
# This script compiles the src

# Set environment variables
export BSF4OOREXX_HOME="/opt/BSF4ooRexx850"
export CLASSPATH="$BSF4OOREXX_HOME/lib/bsf4ooRexx-v850-20240707-bin.jar:."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}JDORFX Build Script${NC}"
echo "=========================="

# Check if BSF4ooRexx is available
if [ ! -f "$BSF4OOREXX_HOME/lib/bsf4ooRexx-v850-20240707-bin.jar" ]; then
    echo -e "${RED}Error: BSF4ooRexx not found at $BSF4OOREXX_HOME${NC}"
    exit 1
fi

# Create output directory
mkdir -p build/org/oorexx/handlers/jdorfx

# Clean previous build
echo "Cleaning previous build..."
rm -rf build/org/oorexx/handlers/jdorfx/*.class

echo "Compiling JavaFXDrawingHandler..."
echo "Classpath: $CLASSPATH"

# Compile the main class
javac -cp "$CLASSPATH" \
      -d build \
      -Xlint:unchecked \
      src/JavaFXDrawingHandler.java

if [ $? -eq 0 ]; then
    echo -e "${GREEN}Compilation successful!${NC}"
    
    # Check what was compiled
    echo "Compiled classes:"
    find build -name "*.class" -type f
    
    echo ""
    echo "Creating JAR file..."
    cd build
    jar cf ../lib/jdorfx-dev.jar org/
    cd ..
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}JAR file created: lib/jdorfx-dev.jar${NC}"
    else
        echo -e "${RED}Error creating JAR file${NC}"
        exit 1
    fi
else
    echo -e "${RED}Compilation failed!${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}Build completed successfully!${NC}"
echo "You can now run the samples with the new JAR file."
