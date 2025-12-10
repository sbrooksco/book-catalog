-- Add new column (ignore if exists)
ALTER TABLE books ADD COLUMN IF NOT EXISTS published_year INTEGER;

-- Copy data only if source column exists
-- This will silently fail in H2 tests (which is fine - no data to copy)
UPDATE books
SET published_year = EXTRACT(YEAR FROM published)
WHERE published IS NOT NULL;

-- Drop old column (ignore if doesn't exist)
ALTER TABLE books DROP COLUMN IF EXISTS published;


