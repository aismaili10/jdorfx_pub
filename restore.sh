#!/bin/bash

# JDORFX Development Restore Script
# Restores the original JDORFX JAR file

# Set environment variables
export BSF4OOREXX_HOME="/opt/BSF4ooRexx850"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}JDORFX Restore Original${NC}"
echo "=========================="

# Check if backup exists
if [ -f "$BSF4OOREXX_HOME/lib/jdorfx_20240527.jar.backup" ]; then
    echo "Restoring original JAR..."
    sudo mv "$BSF4OOREXX_HOME/lib/jdorfx_20240527.jar.backup" "$BSF4OOREXX_HOME/lib/jdorfx_20240527.jar"
    if [ -f "$BSF4OOREXX_HOME/lib/jdorfx-dev.jar" ]; then
        echo "Removing development JAR..."
        sudo rm "$BSF4OOREXX_HOME/lib/jdorfx-dev.jar"
    fi
    echo -e "${GREEN}Original JAR restored!${NC}"
else
    echo -e "${RED}Error: No backup found${NC}"
    exit 1
fi
