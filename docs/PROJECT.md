# Book Tracker — Project Definition

## Problem

Readers lose track of what they want to read, what they're reading, and what they thought of it.

## Users

- Reader (USER): manages their own shelf and reviews
- Admin (ADMIN): manages the shared book catalog (books, authors, genres)

## MVP features

1. Register / log in (JWT)
2. Browse and search the book catalog
3. Admin: CRUD books, authors, genres
4. Add a book to my shelf with a status: WANT_TO_READ, READING, FINISHED
5. Update reading progress (current page)
6. Rate (1–5) and review a book — one review per user per book
7. Stats: books finished this year, count per genre

## Out of scope (for now)

Social features (followers), recommendations, book covers upload, external book APIs

## Business rules

- ISBN is unique
- A user can have a given book on their shelf only once
- currentPage cannot exceed the book's pages
- FINISHED sets finishedAt; READING sets startedAt if empty
