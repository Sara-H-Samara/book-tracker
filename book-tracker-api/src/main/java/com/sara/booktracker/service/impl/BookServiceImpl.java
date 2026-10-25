package com.sara.booktracker.service.impl;

import com.sara.booktracker.dto.BookRequest;
import com.sara.booktracker.dto.BookResponse;
import com.sara.booktracker.entity.Author;
import com.sara.booktracker.entity.Book;
import com.sara.booktracker.entity.Genre;
import com.sara.booktracker.exception.DuplicateResourceException;
import com.sara.booktracker.exception.ResourceNotFoundException;
import com.sara.booktracker.mapper.BookMapper;
import com.sara.booktracker.repository.AuthorRepository;
import com.sara.booktracker.repository.BookRepository;
import com.sara.booktracker.repository.GenreRepository;
import com.sara.booktracker.service.BookService;
import com.sara.booktracker.singleton.ActivityLog;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;
    private final BookMapper bookMapper;

    public BookServiceImpl(BookRepository bookRepository,
                           AuthorRepository authorRepository,
                           GenreRepository genreRepository,
                           BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.genreRepository = genreRepository;
        this.bookMapper = bookMapper;
    }

    @Override
    public List<BookResponse> findAll(String search) {
        List<Book> books = (search == null || search.isBlank())
                ? bookRepository.findAll()
                : bookRepository.findByTitleContainingIgnoreCase(search.trim());

        return books.stream().map(bookMapper::toResponse).toList();
    }

    @Override
    public BookResponse findById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", id));
        return bookMapper.toResponse(book);
    }

    @Override
    @Transactional
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
        ActivityLog.getInstance().record("Book created: " + saved.getTitle());
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