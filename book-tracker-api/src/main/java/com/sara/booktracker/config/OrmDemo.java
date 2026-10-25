package com.sara.booktracker.config;

import com.sara.booktracker.entity.Book;
import jakarta.persistence.EntityManager;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class OrmDemo {

    private final EntityManager entityManager;

    public OrmDemo(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void runDemo() {
        System.out.println("===== ORM DEMO =====");


        Book dune = entityManager.find(Book.class, 1L);
        System.out.println("🔎 find(1): " + (dune != null ? dune.getTitle() : "not found"));

        List<Book> books = entityManager
                .createQuery("SELECT b FROM Book b WHERE b.pages > :minPages ORDER BY b.pages DESC", Book.class)
                .setParameter("minPages", 300)
                .getResultList();

        for (Book book : books) {
            System.out.println("📖 " + book.getId() + " | " + book.getTitle() + " | " + book.getPages() + " pages");
        }

        System.out.println("====================");
    }
}