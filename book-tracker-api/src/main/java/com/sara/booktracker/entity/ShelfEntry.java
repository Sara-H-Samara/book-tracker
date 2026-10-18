package com.sara.booktracker.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(
  name = "shelf_entries",
  uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "book_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class ShelfEntry {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "book_id")
  private Book book;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ReadingStatus status = ReadingStatus.WANT_TO_READ;

  @Min(0)
  private Integer currentPage = 0;

  private LocalDate startedAt;
  private LocalDate finishedAt;
}
