#!/bin/bash

# Convert Render's postgres:// URL to JDBC format
if [ -n "$DATABASE_URL" ]; then
  export DATABASE_URL=$(echo $DATABASE_URL | sed 's|^postgres://|jdbc:postgresql://|' | sed 's|^postgresql://|jdbc:postgresql://|')
fi

# Start the application
exec java -jar review-service.jar server config.yaml
