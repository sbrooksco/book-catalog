-- Change published_date column to published_year integer
ALTER TABLE books ADD COLUMN published_year INTEGER;

-- Copy year from existing dates (if any data exists)
UPDATE books 
SET published_year = EXTRACT(YEAR FROM published_date) 
WHERE published_date IS NOT NULL;

-- Drop old column
ALTER TABLE books DROP COLUMN published_date;
