# Book Catalog Refactoring Instructions

## Overview
This refactoring changes your book catalog from a simple list/add interface to a more sophisticated search-based interface with book-specific review pages.

## Changes Summary

### Frontend Changes
1. **Main page** → Search books (with filters)
2. **New page** → Add Book (separate page)
3. **New page** → Book Detail (shows book info + reviews)
4. **Removed** → Old Reviews page (now integrated into Book Detail)

### Backend Changes
1. **book-service**: Add search endpoint
2. **review-service**: Add endpoint to get reviews by bookId

---

## Step 1: Backend Changes

### A. Update book-service

Replace your `BookResource.java` with the provided version that includes:
- New `/books/search` endpoint with optional query params: `title`, `author`, `year`

**Location**: `book-service/src/main/java/.../resources/BookResource.java`

### B. Update review-service

Replace your `ReviewResource.java` with the provided version that includes:
- New `/reviews/book/{bookId}` endpoint to get all reviews for a specific book

**Location**: `review-service/src/main/java/.../resources/ReviewResource.java`

### C. Deploy Backend Changes

```bash
# Commit and push your changes
git add .
git commit -m "Add search and book-specific review endpoints"
git push

# Render will auto-deploy
```

---

## Step 2: Frontend Changes

### A. Install React Router

```bash
cd book-catalog-frontend
npm install react-router-dom
```

### B. Replace Files

1. Replace `src/App.jsx` with the new version
2. Replace `src/App.css` with the new version
3. Delete `src/components/Books.jsx`
4. Delete `src/components/Reviews.jsx`
5. Create `src/components/SearchBooks.jsx` (new file)
6. Create `src/components/AddBook.jsx` (new file)
7. Create `src/components/BookDetail.jsx` (new file)

### C. File Structure

```
src/
├── App.jsx (replaced)
├── App.css (replaced)
├── components/
│   ├── SearchBooks.jsx (new)
│   ├── AddBook.jsx (new)
│   └── BookDetail.jsx (new)
└── main.jsx (unchanged)
```

### D. Test Locally

```bash
npm run dev
```

Open http://localhost:5173 and verify:
- Search books by title/author/year
- Click "Add Book" to add a new book
- Click on a book title to see its details
- Add reviews for specific books
- Delete reviews

### E. Deploy to Vercel

```bash
git add .
git commit -m "Refactor to search-based interface with book-specific reviews"
git push

# Vercel will auto-deploy
```

---

## Testing Checklist

### Backend Tests
- [ ] GET /books/search?title=gatsby → returns matching books
- [ ] GET /books/search?author=fitzgerald → returns matching books
- [ ] GET /books/search?year=1925 → returns matching books
- [ ] GET /reviews/book/1 → returns reviews for book ID 1

### Frontend Tests
- [ ] Main page shows search form
- [ ] Search works with title filter
- [ ] Search works with author filter
- [ ] Search works with year filter
- [ ] Clear button resets search
- [ ] "Add Book" navigation works
- [ ] Can add a new book
- [ ] After adding book, redirects to book detail
- [ ] Book detail shows book information
- [ ] Book detail shows reviews for that book
- [ ] Can add review from book detail page
- [ ] Can delete review from book detail page
- [ ] Back button returns to search

---

## New User Flow

1. **User signs in** → Sees search page
2. **User searches** → Filters books by title/author/year
3. **User clicks book title** → Goes to book detail page
4. **User sees reviews** → Only reviews for that specific book
5. **User adds review** → Review is associated with that book
6. **User clicks "Add Book"** → Goes to add book page
7. **After adding book** → Redirects to new book's detail page

---

## API Endpoints Reference

### book-service
- GET /books → All books
- GET /books/search?title=X&author=Y&year=Z → Filtered books
- GET /books/{id} → Single book
- POST /books → Create book
- DELETE /books/{id} → Delete book

### review-service
- GET /reviews → All reviews
- GET /reviews/book/{bookId} → Reviews for specific book
- GET /reviews/{id} → Single review
- POST /reviews → Create review (requires bookId in body)
- DELETE /reviews/{id} → Delete review

---

## Troubleshooting

### Backend not returning search results
- Check that BookResource.java was updated correctly
- Redeploy on Render
- Test endpoint directly: `curl https://book-service-innn.onrender.com/books/search?title=test`

### Reviews not showing for book
- Check that ReviewResource.java was updated correctly
- Verify reviews table has bookId column (it should from previous setup)
- Test endpoint: `curl https://review-service-x2si.onrender.com/reviews/book/1`

### React Router not working
- Verify `react-router-dom` is installed: `npm list react-router-dom`
- Check that App.jsx properly wraps with `<Router>`
- Clear browser cache and hard refresh

### 404 errors on page refresh in Vercel
Add `vercel.json` to your project root:
```json
{
  "rewrites": [
    { "source": "/(.*)", "destination": "/" }
  ]
}
```

---

## Questions?

Let me know if you run into any issues with:
- Backend deployment
- Frontend implementation
- Testing the new features
