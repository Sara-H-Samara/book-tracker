package com.sara.booktracker.repository;

import com.sara.booktracker.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    boolean existsByIsbn(String isbn);

    //---------Derived Queries------------

    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    List<Book> findByPagesGreaterThan(int pages);

    List<Book> findByPublishedYearBetween(int from, int to);

    List<Book> findByAuthorNameIgnoreCase(String authorName);

    List<Book> findDistinctByGenresNameIgnoreCase(String genreName);

    List<Book> findTop2ByOrderByPagesDesc();

    long countByAuthorNameIgnoreCase(String authorName);

    //--------------Queries----------------

    @Query("""
            SELECT b FROM Book b
            WHERE LOWER(b.author.name) = LOWER(:authorName)
              AND b.pages >= :minPages
            ORDER BY b.publishedYear DESC
            """)
    List<Book> findByAuthorWithMinPages(@Param("authorName") String authorName, @Param("minPages") int minPages);

    //  JPQL
    @Query("SELECT DISTINCT b FROM Book b JOIN b.genres g WHERE LOWER(g.name) = LOWER(:genre)")
    List<Book> findByGenre(@Param("genre") String genre);

    //  JPQL
    @Query("SELECT AVG(b.pages) FROM Book b")
    Double averagePages();

    //  Native SQL
    @Query(value = "SELECT * FROM books WHERE published_year < :year ORDER BY published_year", nativeQuery = true)
    List<Book> findOlderThan(@Param("year") int year);
}