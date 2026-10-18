import { Component, computed, inject, input, numberAttribute, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { BookService } from '../../services/book-service';
import { BookStatus, STATUS_LABELS } from '../../models/book';

@Component({
  selector: 'app-book-details',
  imports: [RouterLink],
  templateUrl: './book-details.html',
  styleUrl: './book-details.css',
})
export class BookDetails {
  private bookService = inject(BookService);
  private router = inject(Router);

  /** Comes from the URL: /books/:id */
  id = input.required({ transform: numberAttribute });

  book = computed(() => this.bookService.books().find((b) => b.id === this.id()));

  labels = STATUS_LABELS;
  statuses: BookStatus[] = ['to-read', 'reading', 'finished'];

  confirming = signal(false);

  setStatus(status: BookStatus) {
    const book = this.book();
    if (book) this.bookService.setStatus(book.id, status);
  }

  remove() {
    const book = this.book();
    if (!book) return;
    this.bookService.deleteBook(book.id);
    this.router.navigate(['/']);
  }
}
