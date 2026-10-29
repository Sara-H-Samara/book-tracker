package com.sara.booktracker.mapper;

import com.sara.booktracker.dto.AuthorSummary;
import com.sara.booktracker.dto.BookResponse;
import com.sara.booktracker.dto.BookSummary;
import com.sara.booktracker.dto.GenreSummary;
import com.sara.booktracker.entity.Author;
import com.sara.booktracker.entity.Book;
import com.sara.booktracker.entity.Genre;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookMapperTest {

    private final BookMapper mapper = new BookMapper();

    @Test
    void toResponse_nestsAuthorAndSortsGenresByName() {
        Book dune = book(1L, "Dune", 688, author(1L, "Frank Herbert"));
        dune.getGenres().add(genre(1L, "Science Fiction"));
        dune.getGenres().add(genre(3L, "Classic"));

        BookResponse response = mapper.toResponse(dune);

        assertEquals(new AuthorSummary(1L, "Frank Herbert"), response.author());
        assertEquals(List.of(new GenreSummary(3L, "Classic"), new GenreSummary(1L, "Science Fiction")),
                response.genres());
    }

    @Test
    void toResponse_withoutAuthorOrGenres_hasNullAuthorAndEmptyGenres() {
        BookResponse response = mapper.toResponse(book(2L, "Untitled", null, null));

        assertNull(response.author());
        assertTrue(response.genres().isEmpty());
    }

    @Test
    void toSummary_keepsOnlyIdTitlePagesAndAuthor() {
        BookSummary summary = mapper.toSummary(book(1L, "Dune", 688, author(1L, "Frank Herbert")));

        assertEquals(new BookSummary(1L, "Dune", 688, new AuthorSummary(1L, "Frank Herbert")), summary);
    }

    private static Book book(Long id, String title, Integer pages, Author author) {
        Book book = new Book();
        book.setId(id);
        book.setTitle(title);
        book.setPages(pages);
        book.setAuthor(author);
        return book;
    }

    private static Author author(Long id, String name) {
        Author author = new Author();
        author.setId(id);
        author.setName(name);
        return author;
    }

    private static Genre genre(Long id, String name) {
        Genre genre = new Genre();
        genre.setId(id);
        genre.setName(name);
        return genre;
    }
}
