package com.sara.booktracker.service;

import com.sara.booktracker.dto.AddToShelfRequest;
import com.sara.booktracker.dto.ShelfEntryResponse;

import java.util.List;

public interface ShelfService {

    ShelfEntryResponse addToShelf(AddToShelfRequest request);

    List<ShelfEntryResponse> getShelf(Long userId);
}