package com.sara.booktracker.service;

import com.sara.booktracker.dto.AddToShelfRequest;
import com.sara.booktracker.dto.PageResponse;
import com.sara.booktracker.dto.ShelfEntryResponse;
import org.springframework.data.domain.Pageable;

public interface ShelfService {

    ShelfEntryResponse addToShelf(AddToShelfRequest request);

    PageResponse<ShelfEntryResponse> getShelf(Long userId, Pageable pageable);
}
