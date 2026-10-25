package com.sara.booktracker.repository;

import com.sara.booktracker.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    boolean existsByIsbn(String isbn);

    List<Book> findByTitleContainingIgnoreCase(String title);
}