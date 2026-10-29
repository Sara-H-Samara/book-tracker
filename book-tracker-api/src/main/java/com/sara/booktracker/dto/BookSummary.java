package com.sara.booktracker.dto;

/** A short book inside another response (e.g. a shelf entry): just what a list or card needs. */
public record BookSummary(
        Long id,
        String title,
        Integer pages,
        AuthorSummary author
) {
}
