#!/bin/bash
set -e

# Build JDBC URL from components if DATABASE_URL not provided
if [ -z "$DATABASE_URL" ]; then
  if [ -n "$DATABASE_HOST" ] && [ -n "$DATABASE_NAME" ]; then
    export DATABASE_URL="jdbc:postgresql://${DATABASE_HOST}:${DATABASE_PORT:-5432}/${DATABASE_NAME}"
    echo "Built DATABASE_URL from components: jdbc:postgresql://${DATABASE_HOST}:${DATABASE_PORT:-5432}/${DATABASE_NAME}"
  fi
elif [[ "$DATABASE_URL" == postgres://* ]]; then
  # Convert postgres:// to jdbc:postgresql://
  export DATABASE_URL="${DATABASE_URL/postgres:\/\//jdbc:postgresql://}"
  echo "Converted DATABASE_URL from postgres:// to jdbc:postgresql://"
elif [[ "$DATABASE_URL" == postgresql://* ]]; then
  # Convert postgresql:// to jdbc:postgresql://
  export DATABASE_URL="${DATABASE_URL/postgresql:\/\//jdbc:postgresql://}"
  echo "Converted DATABASE_URL from postgresql:// to jdbc:postgresql://"
fi

echo "Using DATABASE_URL: ${DATABASE_URL}"

# Start the application
exec java -jar book-service.jar server config.yaml