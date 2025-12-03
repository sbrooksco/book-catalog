#!/bin/bash

# If separate connection params are provided, build the DATABASE_URL
if [ -n "$DATABASE_HOST" ] && [ -n "$DATABASE_USER" ] && [ -n "$DATABASE_PASSWORD" ] && [ -n "$DATABASE_NAME" ]; then
  export DATABASE_URL="jdbc:postgresql://${DATABASE_HOST}:${DATABASE_PORT:-5432}/${DATABASE_NAME}"
  echo "Built DATABASE_URL from components"
elif [ -n "$DATABASE_URL" ]; then
  # Fallback: try to convert provided DATABASE_URL
  export DATABASE_URL=$(echo $DATABASE_URL | sed 's|^postgres://|jdbc:postgresql://|' | sed 's|^postgresql://|jdbc:postgresql://|')

  # Add port if missing
  if ! echo $DATABASE_URL | grep -q ':[0-9]\+/'; then
    export DATABASE_URL=$(echo $DATABASE_URL | sed 's|@\([^/]*\)/|@\1:5432/|')
  fi
  echo "Converted DATABASE_URL"
fi

echo "Using DATABASE_URL (host part): $(echo $DATABASE_URL | sed 's/.*@\([^/]*\).*/\1/')"

# Start the application
exec java -jar review-service.jar server config.yaml
