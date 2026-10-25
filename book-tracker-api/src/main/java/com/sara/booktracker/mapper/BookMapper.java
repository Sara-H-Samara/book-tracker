package com.sara.booktracker.mapper;

import com.sara.booktracker.dto.BookResponse;
import com.sara.booktracker.entity.Author;
import com.sara.booktracker.entity.Book;
import com.sara.booktracker.entity.Genre;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Component
public class BookMapper {

    public BookResponse toResponse(Book book) {
        Author author = book.getAuthor();
        Set<String> genreNames = book.getGenres().stream()
                .map(Genre::getName)
                .collect(Collectors.toCollection(TreeSet::new));

        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getIsbn(),
                book.getPages(),
                book.getPublishedYear(),
                author != null ? author.getId() : null,
                author != null ? author.getName() : null,
                genreNames
        );
    }
}