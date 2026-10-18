import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { BookService } from '../../services/book-service';
import { Book, BookStatus, NEXT_ACTION, STATUS_LABELS } from '../../models/book';

type Filter = 'all' | BookStatus;

@Component({
  selector: 'app-book-list',
  imports: [RouterLink],
  templateUrl: './book-list.html',
  styleUrl: './book-list.css',
})
export class BookList {
  private bookService = inject(BookService);
  books = this.bookService.books;

  labels = STATUS_LABELS;
  nextAction = NEXT_ACTION;

  finishedCount = computed(() => this.books().filter((b) => b.status === 'finished').length);

  // ---------- Filtering ----------
  filters: { value: Filter; label: string }[] = [
    { value: 'all', label: 'All' },
    { value: 'reading', label: 'Reading' },
    { value: 'to-read', label: 'To read' },
    { value: 'finished', label: 'Finished' },
  ];

  filter = signal<Filter>('all');

  counts = computed(() => {
    const counts: Record<Filter, number> = { all: 0, 'to-read': 0, reading: 0, finished: 0 };
    for (const book of this.books()) {
      counts.all++;
      counts[book.status]++;
    }
    return counts;
  });

  visibleBooks = computed(() => {
    const filter = this.filter();
    const books = this.books();
    return filter === 'all' ? books : books.filter((b) => b.status === filter);
  });

  // ---------- Status ----------
  advance(book: Book) {
    this.bookService.setStatus(book.id, this.nextAction[book.status].to);
  }

  // ---------- Delete with confirm ----------
  confirmingId = signal<number | null>(null);

  askDelete(id: number) {
    this.confirmingId.set(id);
  }

  cancelDelete() {
    this.confirmingId.set(null);
  }

  confirmDelete(id: number) {
    this.bookService.deleteBook(id);
    this.confirmingId.set(null);
  }
}
