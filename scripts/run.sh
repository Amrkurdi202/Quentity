#!/bin/bash

cd "$(dirname "$0")"

CLASSPATH=".:./lib/*"

echo "Running the application..."
java -Dspring.sql.init.data-locations=file:./data.sql -cp "$CLASSPATH" com.quentity.Application
