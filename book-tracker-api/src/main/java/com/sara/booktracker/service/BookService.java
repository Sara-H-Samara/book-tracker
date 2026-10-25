package com.sara.booktracker.service;

import com.sara.booktracker.dto.BookRequest;
import com.sara.booktracker.dto.BookResponse;

import java.util.List;

public interface BookService {

    List<BookResponse> findAll(String search);

    BookResponse findById(Long id);

    BookResponse create(BookRequest request);
}