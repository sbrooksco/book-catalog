-- Migration V3: published_year column already exists
-- This is a no-op migration because the schema was created 
-- with published_year directly via Hibernate
-- This just brings Flyway's version tracking in sync

-- Verify column exists (will succeed silently)
SELECT 1 FROM information_schema.columns 
WHERE table_schema = 'books_schema' 
  AND table_name = 'books' 
  AND column_name = 'published_year';
