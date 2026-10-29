package com.sara.booktracker.dto;

/** A short genre inside a book. The id is what clients send back in BookRequest.genreIds. */
public record GenreSummary(Long id, String name) {
}
