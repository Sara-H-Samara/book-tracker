import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { form, FormField, required, minLength } from '@angular/forms/signals';
import { BookService } from '../../services/book-service';
import { BookSearchResult, OpenLibraryService } from '../../services/open-library-service';

interface BookFormModel {
  title: string;
  author: string;
}

interface PickedExtras {
  coverUrl?: string;
  year?: number;
}

@Component({
  selector: 'app-book-form',
  imports: [FormField, RouterLink],
  templateUrl: './book-form.html',
  styleUrl: './book-form.css',
})
export class BookForm implements OnInit {
  private bookService = inject(BookService);
  private openLibrary = inject(OpenLibraryService);
  private router = inject(Router);

  // ---------- Mode: add or edit ----------
  /** Only set on /books/:id/edit */
  id = input<number | undefined, string | undefined>(undefined, {
    transform: (value) => (value ? Number(value) : undefined),
  });

  isEditing = computed(() => this.id() !== undefined);

  private editingBook = computed(() => {
    const id = this.id();
    return id === undefined ? undefined : this.bookService.books().find((b) => b.id === id);
  });

  ngOnInit() {
    if (!this.isEditing()) return;

    const book = this.editingBook();
    if (!book) {
      this.router.navigate(['/']);
      return;
    }

    this.model.set({ title: book.title, author: book.author });
    if (book.coverUrl || book.year) {
      this.picked.set({ coverUrl: book.coverUrl, year: book.year });
    }
  }

  // ---------- Search ----------
  searchModel = signal({ query: '' });
  searchForm = form(this.searchModel);

  results = signal<BookSearchResult[]>([]);
  searching = signal(false);
  searchError = signal<string | null>(null);

  search(event: Event) {
    event.preventDefault();

    const query = this.searchModel().query.trim();
    if (query.length < 2) return;

    this.searching.set(true);
    this.searchError.set(null);

    this.openLibrary.search(query).subscribe({
      next: (results) => {
        this.results.set(results);
        this.searching.set(false);
      },
      error: () => {
        this.searchError.set('Search failed. Check your connection and try again.');
        this.searching.set(false);
      },
    });
  }

  /** Fill the form from a search result */
  use(result: BookSearchResult) {
    this.model.set({ title: result.title, author: result.author });
    this.picked.set({ coverUrl: result.coverUrl, year: result.year });
    this.results.set([]);
  }

  clearPicked() {
    this.picked.set(null);
  }

  // ---------- The form ----------
  model = signal<BookFormModel>({ title: '', author: '' });
  picked = signal<PickedExtras | null>(null);

  bookForm = form(this.model, (path) => {
    required(path.title, { message: 'Title is required.' });
    minLength(path.title, 2, { message: 'At least 2 characters.' });
    required(path.author, { message: 'Author is required.' });
  });

  submit(event: Event) {
    event.preventDefault();

    if (this.bookForm().invalid()) {
      this.bookForm.title().markAsTouched();
      this.bookForm.author().markAsTouched();
      return;
    }

    const { title, author } = this.model();
    const extras = this.picked();
    const details = {
      title: title.trim(),
      author: author.trim(),
      coverUrl: extras?.coverUrl,
      year: extras?.year,
    };

    const id = this.id();
    if (id !== undefined) {
      this.bookService.updateBook(id, details);
      this.router.navigate(['/books', id]);
    } else {
      this.bookService.addBook(details.title, details.author, details.coverUrl, details.year);
      this.router.navigate(['/']);
    }
  }
}
