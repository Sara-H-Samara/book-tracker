package com.sara.booktracker.config;

import com.sara.booktracker.entity.Book;
import com.sara.booktracker.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class QueryDemo {

    private final BookRepository bookRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void runDemo() {
        System.out.println("===== DERIVED QUERIES DEMO =====");

        print("1. More than 400 pages", bookRepository.findByPagesGreaterThan(400));
        print("2. Published 1900–2000", bookRepository.findByPublishedYearBetween(1900, 2000));
        print("3. By Jane Austen", bookRepository.findByAuthorNameIgnoreCase("jane austen"));
        print("4. Genre: Classic", bookRepository.findDistinctByGenresNameIgnoreCase("classic"));
        print("5. Top 2 longest", bookRepository.findTop2ByOrderByPagesDesc());
        System.out.println("6. Books by Frank Herbert: " + bookRepository.countByAuthorNameIgnoreCase("frank herbert"));

        System.out.println("===== QUERIES DEMO =====");


        print("1. Herbert, 300+ pages", bookRepository.findByAuthorWithMinPages("frank herbert", 300));
        print("2. Genre: classic", bookRepository.findByGenre("classic"));
        System.out.println("3. Average pages: " + bookRepository.averagePages());
        print("4. Older than 1900 (native)", bookRepository.findOlderThan(1900));

        System.out.println("================================");


    }

    private void print(String label, List<Book> books) {
        List<String> titles = books.stream().map(Book::getTitle).toList();
        System.out.println(label + " → " + titles);
    }
}