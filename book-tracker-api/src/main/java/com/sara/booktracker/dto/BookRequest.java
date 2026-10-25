package com.sara.booktracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record BookRequest(

        @NotBlank(message = "title is required")
        @Size(max = 255)
        String title,

        @Pattern(regexp = "\\d{10}|\\d{13}", message = "isbn must be 10 or 13 digits")
        String isbn,

        @Positive(message = "pages must be positive")
        Integer pages,

        Integer publishedYear,

        Long authorId,

        Set<Long> genreIds
) {
}