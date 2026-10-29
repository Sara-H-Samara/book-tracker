package com.sara.booktracker.repository;

import com.sara.booktracker.dto.ShelfEntryResponse;
import com.sara.booktracker.entity.Author;
import com.sara.booktracker.entity.Book;
import com.sara.booktracker.entity.ShelfEntry;
import com.sara.booktracker.entity.User;
import com.sara.booktracker.mapper.BookMapper;
import com.sara.booktracker.mapper.ShelfEntryMapper;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Runs the real shelf query against in-memory H2: pagination boundaries,
 * filtering by user, and the number of SQL statements (no N+1).
 */
@DataJpaTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
class ShelfEntryRepositoryTest {

    @Autowired
    private ShelfEntryRepository shelfEntryRepository;

    @Autowired
    private TestEntityManager em;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private final ShelfEntryMapper mapper = new ShelfEntryMapper(new BookMapper());

    private User sara;

    @BeforeEach
    void seed() {
        sara = em.persist(user("sara"));
        User other = em.persist(user("other"));

        Book dune = em.persist(book("Dune", 688, em.persist(author("Frank Herbert"))));
        Book earthsea = em.persist(book("A Wizard of Earthsea", 183, em.persist(author("Ursula K. Le Guin"))));
        Book pride = em.persist(book("Pride and Prejudice", 480, em.persist(author("Jane Austen"))));

        em.persist(entry(sara, dune));
        em.persist(entry(sara, earthsea));
        em.persist(entry(sara, pride));
        em.persist(entry(other, dune));   // must never appear on Sara's shelf

        em.flush();
        em.clear();   // start every test with nothing cached, like a fresh request
    }

    @Test
    void firstPage_containsOnlyThatUsersEntriesAndCorrectTotals() {
        Page<ShelfEntry> page = shelfEntryRepository.findByUserId(sara.getId(), PageRequest.of(0, 2, Sort.by("id")));

        assertEquals(2, page.getContent().size());
        assertEquals(3, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
        assertTrue(page.getContent().stream().allMatch(e -> e.getUser().getId().equals(sara.getId())));
    }

    @Test
    void lastPage_containsTheRemainingEntry() {
        Page<ShelfEntry> page = shelfEntryRepository.findByUserId(sara.getId(), PageRequest.of(1, 2, Sort.by("id")));

        assertEquals(1, page.getContent().size());
        assertEquals("Pride and Prejudice", page.getContent().get(0).getBook().getTitle());
    }

    @Test
    void pageAfterTheLast_isEmptyButKeepsTotals() {
        Page<ShelfEntry> page = shelfEntryRepository.findByUserId(sara.getId(), PageRequest.of(5, 2, Sort.by("id")));

        assertTrue(page.getContent().isEmpty());
        assertEquals(3, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
    }

    @Test
    void mappingAPage_needsOnlyTheSelectAndTheCount_noNPlusOne() {
        Statistics stats = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        stats.clear();

        Page<ShelfEntry> page = shelfEntryRepository.findByUserId(sara.getId(), PageRequest.of(0, 2, Sort.by("id")));
        List<ShelfEntryResponse> responses = page.map(mapper::toResponse).getContent();   // touches book + author

        assertEquals("Frank Herbert", responses.get(0).book().author().name());
        assertEquals("Ursula K. Le Guin", responses.get(1).book().author().name());
        // 1 SELECT (entries JOIN books JOIN authors) + 1 COUNT, however many entries are on the page
        assertEquals(2, stats.getPrepareStatementCount());
    }

    private static User user(String username) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPasswordHash("not-a-real-hash");
        return user;
    }

    private static Author author(String name) {
        Author author = new Author();
        author.setName(name);
        return author;
    }

    private static Book book(String title, int pages, Author author) {
        Book book = new Book();
        book.setTitle(title);
        book.setPages(pages);
        book.setAuthor(author);
        return book;
    }

    private static ShelfEntry entry(User user, Book book) {
        ShelfEntry entry = new ShelfEntry();
        entry.setUser(user);
        entry.setBook(book);
        return entry;
    }
}
