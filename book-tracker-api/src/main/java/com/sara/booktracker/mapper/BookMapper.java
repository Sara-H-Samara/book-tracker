package com.sara.booktracker.mapper;

import com.sara.booktracker.dto.AuthorSummary;
import com.sara.booktracker.dto.BookResponse;
import com.sara.booktracker.dto.BookSummary;
import com.sara.booktracker.dto.GenreSummary;
import com.sara.booktracker.entity.Author;
import com.sara.booktracker.entity.Book;
import com.sara.booktracker.entity.Genre;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class BookMapper {

    public BookResponse toResponse(Book book) {
        List<GenreSummary> genres = book.getGenres().stream()
                .sorted(Comparator.comparing(Genre::getName))
                .map(genre -> new GenreSummary(genre.getId(), genre.getName()))
                .toList();

        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getIsbn(),
                book.getPages(),
                book.getPublishedYear(),
                toAuthorSummary(book.getAuthor()),
                genres
        );
    }

    /** The short version of a book, used inside other responses (e.g. shelf entries). */
    public BookSummary toSummary(Book book) {
        return new BookSummary(
                book.getId(),
                book.getTitle(),
                book.getPages(),
                toAuthorSummary(book.getAuthor())
        );
    }

    private AuthorSummary toAuthorSummary(Author author) {
        return author != null ? new AuthorSummary(author.getId(), author.getName()) : null;
    }
}
