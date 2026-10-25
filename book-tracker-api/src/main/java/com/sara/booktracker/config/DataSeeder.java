package com.sara.booktracker.config;

import com.sara.booktracker.entity.Author;
import com.sara.booktracker.entity.Book;
import com.sara.booktracker.entity.Genre;
import com.sara.booktracker.entity.User;
import com.sara.booktracker.repository.AuthorRepository;
import com.sara.booktracker.repository.BookRepository;
import com.sara.booktracker.repository.GenreRepository;
import com.sara.booktracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (bookRepository.count() > 0) {
            log.info("🌱 Data already exists, skipping seed");
            return;
        }

        Genre sciFi = genreRepository.save(genre("Science Fiction"));
        Genre fantasy = genreRepository.save(genre("Fantasy"));
        Genre classic = genreRepository.save(genre("Classic"));

        Author herbert = authorRepository.save(author("Frank Herbert"));
        Author leGuin = authorRepository.save(author("Ursula K. Le Guin"));
        Author austen = authorRepository.save(author("Jane Austen"));

        bookRepository.save(book("Dune", "9780441172719", 688, 1965, herbert, Set.of(sciFi, classic)));
        bookRepository.save(book("A Wizard of Earthsea", "9780547773742", 183, 1968, leGuin, Set.of(fantasy)));
        bookRepository.save(book("Pride and Prejudice", "9780141439518", 480, 1813, austen, Set.of(classic)));

        if (userRepository.count() == 0) {
            User demo = new User();
            demo.setUsername("sara");
            demo.setEmail("sara@example.com");
            demo.setPasswordHash("not-a-real-hash"); // replaced when we add BCrypt + JWT
            userRepository.save(demo);
        }

        log.info("🌱 Seeded 3 books, 3 authors, 3 genres and the demo user");
    }

    private static Genre genre(String name) {
        Genre genre = new Genre();
        genre.setName(name);
        return genre;
    }

    private static Author author(String name) {
        Author author = new Author();
        author.setName(name);
        return author;
    }

    private static Book book(String title, String isbn, int pages, int year, Author author, Set<Genre> genres) {
        Book book = new Book();
        book.setTitle(title);
        book.setIsbn(isbn);
        book.setPages(pages);
        book.setPublishedYear(year);
        book.setAuthor(author);
        book.getGenres().addAll(genres);
        return book;
    }
}