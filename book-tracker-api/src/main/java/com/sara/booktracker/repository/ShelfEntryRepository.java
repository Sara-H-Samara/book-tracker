package com.sara.booktracker.repository;

import com.sara.booktracker.entity.ShelfEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShelfEntryRepository extends JpaRepository<ShelfEntry, Long> {

    // "UserId" walks the relation: shelfEntry.user.id
    boolean existsByUserIdAndBookId(Long userId, Long bookId);

    List<ShelfEntry> findByUserId(Long userId);
}