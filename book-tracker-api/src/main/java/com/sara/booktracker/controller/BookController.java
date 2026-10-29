package com.sara.booktracker.controller;

import com.sara.booktracker.dto.ApiResponse;
import com.sara.booktracker.dto.BookFilter;
import com.sara.booktracker.dto.BookRequest;
import com.sara.booktracker.dto.BookResponse;
import com.sara.booktracker.dto.PageResponse;
import com.sara.booktracker.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Tag(name = "Books", description = "Browse the book catalog and add new books")
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @Operation(
            summary = "Search books (paginated)",
            description = """
            Returns books one page at a time, not the whole catalog.
            All filters are optional and can be combined.
            Defaults: page 0, 10 books per page, sorted by title (A→Z). Maximum page size: 50.
            Use page.totalPages in the response to know how many pages exist.
            """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "A page of books, with page info (number, size, totalElements, totalPages)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                    description = "Invalid filter (e.g. minPages > maxPages) or unknown sort field",
                    content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
                    description = "Missing, invalid or expired token", content = @Content)
    })
    @GetMapping
    public ApiResponse<PageResponse<BookResponse>> getAll(
            @ParameterObject @Valid BookFilter filter,
            @ParameterObject @PageableDefault(size = 10, sort = "title") Pageable pageable) {
        return ApiResponse.ok("Books retrieved", bookService.search(filter, pageable));
    }

    @Operation(summary = "Get one book by id")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "The book"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
                    description = "Missing, invalid or expired token", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "No book with this id", content = @Content)
    })
    @GetMapping("/{id}")
    public ApiResponse<BookResponse> getById(
            @Parameter(description = "The book id", example = "1") @PathVariable Long id) {
        return ApiResponse.ok("Book retrieved", bookService.findById(id));
    }

    @Operation(
            summary = "Add a new book (ADMIN only)",
            description = "Creates a book. The ISBN must be unique. Returns the new book and a Location header.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Book created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                    description = "Invalid body (e.g. empty title, negative pages)", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
                    description = "Missing, invalid or expired token", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403",
                    description = "Logged in, but not an ADMIN", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "Author or genre not found", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409",
                    description = "A book with this ISBN already exists", content = @Content)
    })
    @PostMapping
    public ResponseEntity<ApiResponse<BookResponse>> create(@Valid @RequestBody BookRequest request) {
        BookResponse created = bookService.create(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity.created(location).body(ApiResponse.created("Book created", created));
    }
}