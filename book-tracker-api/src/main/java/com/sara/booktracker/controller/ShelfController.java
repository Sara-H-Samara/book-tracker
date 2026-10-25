package com.sara.booktracker.controller;

import com.sara.booktracker.dto.AddToShelfRequest;
import com.sara.booktracker.dto.ShelfEntryResponse;
import com.sara.booktracker.service.ShelfService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shelf")
public class ShelfController {

    private final ShelfService shelfService;

    public ShelfController(ShelfService shelfService) {
        this.shelfService = shelfService;
    }

    /** POST /api/shelf → 201, or 400 / 404 / 409 */
    @PostMapping
    public ResponseEntity<ShelfEntryResponse> addToShelf(@Valid @RequestBody AddToShelfRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shelfService.addToShelf(request));
    }

    /** GET /api/shelf?userId=1 → 200 + that user's shelf */
    @GetMapping
    public List<ShelfEntryResponse> getShelf(@RequestParam Long userId) {
        return shelfService.getShelf(userId);
    }
}