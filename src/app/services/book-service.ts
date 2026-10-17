import { Injectable, signal } from '@angular/core';
import { Book, BookStatus } from '../models/book';

@Injectable({ providedIn: 'root' })
export class BookService {
  private booksSignal = signal<Book[]>([
    { id: 1, title: 'Atomic Habits', author: 'James Clear', status: 'finished' },
    { id: 2, title: 'Clean Code', author: 'Robert C. Martin', status: 'reading' },
    { id: 3, title: 'The Alchemist', author: 'Paulo Coelho', status: 'to-read' },
  ]);

  books = this.booksSignal.asReadonly();

  addBook(title: string, author: string, coverUrl?: string, year?: number) {
    const newBook: Book = { id: Date.now(), title, author, status: 'to-read', coverUrl, year };
    this.booksSignal.update((books) => [...books, newBook]);
  }

  setStatus(id: number, status: BookStatus) {
    this.booksSignal.update((books) =>
      books.map((book) => (book.id === id ? { ...book, status } : book)),
    );
  }

  updateBook(id: number, changes: Partial<Omit<Book, 'id'>>) {
    this.booksSignal.update((books) =>
      books.map((book) => (book.id === id ? { ...book, ...changes } : book)),
    );
  }

  deleteBook(id: number) {
    this.booksSignal.update((books) => books.filter((book) => book.id !== id));
  }
}
