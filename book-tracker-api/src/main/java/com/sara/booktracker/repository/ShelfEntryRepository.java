package com.sara.booktracker.repository;

import com.sara.booktracker.dto.GenreCount;
import com.sara.booktracker.entity.ReadingStatus;
import com.sara.booktracker.entity.ShelfEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ShelfEntryRepository extends JpaRepository<ShelfEntry, Long> {

    // "UserId" walks the relation: shelfEntry.user.id
    boolean existsByUserIdAndBookId(Long userId, Long bookId);

    // Loads each entry's book + author in the same SELECT, so mapping a page never triggers extra queries
    @EntityGraph(attributePaths = {"book", "book.author"})
    Page<ShelfEntry> findByUserId(Long userId, Pageable pageable);

    long countByUserIdAndStatusAndFinishedAtBetween(
            Long userId, ReadingStatus status, LocalDate from, LocalDate to);

    @Query("""
    SELECT COALESCE(SUM(b.pages), 0)
    FROM ShelfEntry s JOIN s.book b
    WHERE s.user.id = :userId
      AND s.status = :status
      AND s.finishedAt BETWEEN :from AND :to
    """)
    long sumPagesFinished(@Param("userId") Long userId,
                          @Param("status") ReadingStatus status,
                          @Param("from") LocalDate from,
                          @Param("to") LocalDate to);


    @Query("""
    SELECT new com.sara.booktracker.dto.GenreCount(g.name, COUNT(s))
    FROM ShelfEntry s JOIN s.book b JOIN b.genres g
    WHERE s.user.id = :userId
      AND s.status = :status
      AND s.finishedAt BETWEEN :from AND :to
    GROUP BY g.name
    ORDER BY COUNT(s) DESC
    """)
    List<GenreCount> countFinishedPerGenre(@Param("userId") Long userId,
                                           @Param("status") ReadingStatus status,
                                           @Param("from") LocalDate from,
                                           @Param("to") LocalDate to);

}