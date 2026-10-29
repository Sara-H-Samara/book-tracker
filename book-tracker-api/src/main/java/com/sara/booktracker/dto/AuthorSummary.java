package com.sara.booktracker.dto;

/** A short author inside another response: "author": { "id": 1, "name": "Frank Herbert" } */
public record AuthorSummary(Long id, String name) {
}
