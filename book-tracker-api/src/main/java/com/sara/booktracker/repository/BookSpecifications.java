package com.sara.booktracker.repository;

import com.sara.booktracker.dto.BookFilter;
import com.sara.booktracker.entity.Book;
import com.sara.booktracker.entity.Genre;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Reusable query conditions for Book. Each method returns ONE condition;
 * fromFilter() combines only the ones the client actually sent.
 */
public final class BookSpecifications {

    private BookSpecifications() {
    }

    /** title LIKE %text% (case-insensitive) */
    public static Specification<Book> titleContains(String text) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("title")), "%" + text.toLowerCase() + "%");
    }

    /** author.id = authorId */
    public static Specification<Book> hasAuthor(Long authorId) {
        return (root, query, cb) ->
                cb.equal(root.get("author").get("id"), authorId);
    }

    /** JOIN genres, genre.name = name (case-insensitive) */
    public static Specification<Book> hasGenre(String genreName) {
        return (root, query, cb) -> {
            query.distinct(true);                                  // a book with 2 genres must appear once
            Join<Book, Genre> genres = root.join("genres");
            return cb.equal(cb.lower(genres.get("name")), genreName.toLowerCase());
        };
    }

    /** pages >= min */
    public static Specification<Book> minPages(int min) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("pages"), min);
    }

    /** pages <= max */
    public static Specification<Book> maxPages(int max) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("pages"), max);
    }

    /** Builds ONE combined condition from only the filters that were sent. */
    public static Specification<Book> fromFilter(BookFilter filter) {
        List<Specification<Book>> specs = new ArrayList<>();

        if (filter.title() != null && !filter.title().isBlank()) {
            specs.add(titleContains(filter.title().trim()));
        }
        if (filter.authorId() != null) {
            specs.add(hasAuthor(filter.authorId()));
        }
        if (filter.genre() != null && !filter.genre().isBlank()) {
            specs.add(hasGenre(filter.genre().trim()));
        }
        if (filter.minPages() != null) {
            specs.add(minPages(filter.minPages()));
        }
        if (filter.maxPages() != null) {
            specs.add(maxPages(filter.maxPages()));
        }

        return Specification.allOf(specs);   // spec1 AND spec2 AND ... (no specs = no WHERE)
    }
}