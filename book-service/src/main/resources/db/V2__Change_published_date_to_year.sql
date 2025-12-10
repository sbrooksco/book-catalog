-- Change published_date column to published_year integer (idempotent version)

-- Add published_year column only if it doesn't exist
ALTER TABLE books ADD COLUMN IF NOT EXISTS published_year INTEGER;

-- Copy data only if published_date column still exists
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'books' AND column_name = 'published_date'
    ) THEN
        UPDATE books
        SET published_year = EXTRACT(YEAR FROM published_date)
        WHERE published_date IS NOT NULL;

        ALTER TABLE books DROP COLUMN published_date;
    END IF;
END $$;
