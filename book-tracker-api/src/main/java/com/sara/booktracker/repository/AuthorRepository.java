package com.sara.booktracker.repository;

import com.sara.booktracker.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author, Long> {
}