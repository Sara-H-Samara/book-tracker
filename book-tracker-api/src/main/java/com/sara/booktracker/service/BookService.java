package com.sara.booktracker.service;

import com.sara.booktracker.dto.BookFilter;
import com.sara.booktracker.dto.BookRequest;
import com.sara.booktracker.dto.BookResponse;
import com.sara.booktracker.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookService {

    PageResponse<BookResponse> search(BookFilter filter, Pageable pageable);

    BookResponse findById(Long id);

    BookResponse create(BookRequest request);
}