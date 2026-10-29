package com.sara.booktracker.service.impl;

import com.sara.booktracker.aspect.LogActivity;
import com.sara.booktracker.dto.BookFilter;
import com.sara.booktracker.dto.BookRequest;
import com.sara.booktracker.dto.BookResponse;
import com.sara.booktracker.dto.PageResponse;
import com.sara.booktracker.entity.Author;
import com.sara.booktracker.entity.Book;
import com.sara.booktracker.entity.Genre;
import com.sara.booktracker.exception.DuplicateResourceException;
import com.sara.booktracker.exception.ResourceNotFoundException;
import com.sara.booktracker.mapper.BookMapper;
import com.sara.booktracker.repository.AuthorRepository;
import com.sara.booktracker.repository.BookRepository;
import com.sara.booktracker.repository.BookSpecifications;
import com.sara.booktracker.repository.GenreRepository;
import com.sara.booktracker.service.BookService;
import com.sara.booktracker.singleton.ActivityLog;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;
    private final BookMapper bookMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BookResponse> search(BookFilter filter, Pageable pageable) {
        // Query 1: the page of books (+ author). Paging is correct here.
        Page<Book> page = bookRepository.findAll(BookSpecifications.fromFilter(filter), pageable);

        if (page.isEmpty()) {
            return PageResponse.from(page.map(bookMapper::toResponse));
        }

        // Collect the ids of the books on this page
        List<Long> ids = page.getContent().stream()
                .map(Book::getId)
                .toList();

        // Query 2: the same books, now WITH their genres
        Map<Long, Book> booksWithGenres = bookRepository.findByIdIn(ids).stream()
                .collect(Collectors.toMap(Book::getId, Function.identity()));

        // Keep the page order, but use the books that have genres loaded
        Page<BookResponse> result = page.map(book ->
                bookMapper.toResponse(booksWithGenres.get(book.getId())));

        return PageResponse.from(result);
    }

    @Override
    public BookResponse findById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", id));
        return bookMapper.toResponse(book);
    }

    @Override
    @Transactional
    @LogActivity("Book created")
    public BookResponse create(BookRequest request) {
        if (request.isbn() != null && bookRepository.existsByIsbn(request.isbn())) {
            throw new DuplicateResourceException("A book with isbn " + request.isbn() + " already exists");
        }

        Book book = new Book();
        book.setTitle(request.title().trim());
        book.setIsbn(request.isbn());
        book.setPages(request.pages());
        book.setPublishedYear(request.publishedYear());
        book.setAuthor(resolveAuthor(request.authorId()));
        book.setGenres(resolveGenres(request.genreIds()));

        Book saved = bookRepository.save(book);
        return bookMapper.toResponse(saved);
    }

    private Author resolveAuthor(Long authorId) {
        if (authorId == null) {
            return null;
        }
        return authorRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Author", authorId));
    }

    private Set<Genre> resolveGenres(Set<Long> genreIds) {
        Set<Genre> genres = new HashSet<>();
        if (genreIds == null) {
            return genres;
        }
        for (Long genreId : genreIds) {
            genres.add(genreRepository.findById(genreId)
                    .orElseThrow(() -> new ResourceNotFoundException("Genre", genreId)));
        }
        return genres;
    }
}