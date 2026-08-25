#!/usr/bin/env bash
set -e

mkdir -p build lib
rm -rf build/org/oorexx/handlers/jdorfx

javac \
  -cp "$BSF4OOREXX_HOME/lib/*:." \
  -d build \
  src/JavaFXDrawingHandler.java

jar cf lib/jdorfx-dev.jar -C build org

echo "Created lib/jdorfx-dev.jar"
cp lib/jdorfx-dev.jar "$BSF4OOREXX_HOME/lib/jdorfx-dev.jar"
echo "Copied lib/jdorfx-dev.jar to $BSF4OOREXX_HOME/lib/jdorfx-dev.jar"