#!/bin/bash

cd "$(dirname "$0")"

CLASSPATH=".:./lib/*"

echo "Running the application..."
java -cp "$CLASSPATH" com.quentity.Application
