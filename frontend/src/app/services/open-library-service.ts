import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';

/** One search result, exactly as Open Library sends it */
interface OpenLibraryDoc {
  key: string;
  title: string;
  author_name?: string[];
  cover_i?: number;
  first_publish_year?: number;
}

interface OpenLibraryResponse {
  docs: OpenLibraryDoc[];
}

/** One search result, in the shape OUR app wants */
export interface BookSearchResult {
  key: string;
  title: string;
  author: string;
  year?: number;
  coverUrl?: string;
}

@Injectable({ providedIn: 'root' })
export class OpenLibraryService {
  private http = inject(HttpClient);
  private searchUrl = 'https://openlibrary.org/search.json';

  search(query: string): Observable<BookSearchResult[]> {
    const params = new HttpParams()
      .set('q', query)
      .set('limit', 8)
      .set('fields', 'key,title,author_name,cover_i,first_publish_year');

    return this.http.get<OpenLibraryResponse>(this.searchUrl, { params }).pipe(
      map((response) =>
        response.docs.map((doc) => ({
          key: doc.key,
          title: doc.title,
          author: doc.author_name?.[0] ?? 'Unknown author',
          year: doc.first_publish_year,
          coverUrl: doc.cover_i
            ? `https://covers.openlibrary.org/b/id/${doc.cover_i}-M.jpg`
            : undefined,
        })),
      ),
    );
  }
}
