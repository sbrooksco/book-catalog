-- Migrations are version controlled SQL scripts.
-- To modify it create V2__create_books_table.sql
-- Example:
-- ALTER TABLE books ADD COLUMN genre VARCHAR(100);
-- Each file gets applies once, in order to keep
-- everything consistent.
CREATE TABLE books (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    published_date DATE,
    isbn VARCHAR(20) UNIQUE
);