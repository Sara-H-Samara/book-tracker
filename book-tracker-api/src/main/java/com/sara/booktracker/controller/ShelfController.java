package com.sara.booktracker.controller;

import com.sara.booktracker.dto.AddToShelfRequest;
import com.sara.booktracker.dto.ApiResponse;
import com.sara.booktracker.dto.PageResponse;
import com.sara.booktracker.dto.ShelfEntryResponse;
import com.sara.booktracker.service.ShelfService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shelf")
public class ShelfController {

    private final ShelfService shelfService;

    public ShelfController(ShelfService shelfService) {
        this.shelfService = shelfService;
    }

    /** POST /api/shelf → 201, or 400 / 404 / 409 (no Location header: there is no GET /api/shelf/{id} yet) */
    @PostMapping
    public ResponseEntity<ApiResponse<ShelfEntryResponse>> addToShelf(@Valid @RequestBody AddToShelfRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Book added to shelf", shelfService.addToShelf(request)));
    }

    /** GET /api/shelf?userId=1&page=0&size=20&sort=id → 200 + one page of that user's shelf (oldest entry first) */
    @GetMapping
    public ApiResponse<PageResponse<ShelfEntryResponse>> getShelf(
            @RequestParam Long userId,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ApiResponse.ok("Shelf retrieved", shelfService.getShelf(userId, pageable));
    }
}
