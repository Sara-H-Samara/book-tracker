package com.sara.booktracker.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
  name = "reviews",
  uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "book_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class Review {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "book_id")
  private Book book;

  @Min(1)
  @Max(5)
  @Column(nullable = false)
  private Integer rating;

  @Column(length = 2000)
  private String comment;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  void onCreate() {
    createdAt = LocalDateTime.now();
  }
}
