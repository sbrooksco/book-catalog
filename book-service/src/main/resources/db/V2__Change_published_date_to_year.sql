-- Change published_date column to published_year integer (fully idempotent)

DO $$
BEGIN
    -- Only proceed if published_year doesn't exist yet
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'books' AND column_name = 'published_year'
    ) THEN
        -- Add the new column
        ALTER TABLE books ADD COLUMN published_year INTEGER;

        -- Copy data if published_date exists
        IF EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_name = 'books' AND column_name = 'published_date'
        ) THEN
            UPDATE books
            SET published_year = EXTRACT(YEAR FROM published_date)
            WHERE published_date IS NOT NULL;

            ALTER TABLE books DROP COLUMN published_date;
        END IF;
    END IF;
END $$;
