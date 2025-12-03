#!/bin/bash

# Convert Render's postgres:// URL to JDBC format
if [ -n "$DATABASE_URL" ]; then
  export DATABASE_URL=$(echo $DATABASE_URL | sed 's|^postgres://|jdbc:postgresql://|' | sed 's|^postgresql://|jdbc:postgresql://|')

    # Check if port is missing (no :5432 before the /)
    # If URL doesn't have :port/ pattern, add :5432
    if ! echo $DATABASE_URL | grep -q ':[0-9]\+/'; then
      export DATABASE_URL=$(echo $DATABASE_URL | sed 's|@\([^/]*\)/|@\1:5432/|')
    fi
fi

# Start the application
exec java -jar review-service.jar server config.yaml
