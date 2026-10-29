package com.sara.booktracker.controller;

import com.sara.booktracker.MockSecurityBeans;
import com.sara.booktracker.dto.AuthorSummary;
import com.sara.booktracker.dto.BookFilter;
import com.sara.booktracker.dto.BookRequest;
import com.sara.booktracker.dto.BookResponse;
import com.sara.booktracker.dto.GenreSummary;
import com.sara.booktracker.dto.PageResponse;
import com.sara.booktracker.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer tests for /api/books: only the controller, validation and
 * GlobalExceptionHandler are started. BookService is a mock.
 */
@WebMvcTest(BookController.class)
@MockSecurityBeans
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @BeforeEach
    void stubSearch() {
        PageResponse<BookResponse> emptyPage =
                new PageResponse<>(List.of(), new PageResponse.PageMetadata(0, 10, 0, 0));
        when(bookService.search(any(BookFilter.class), any(Pageable.class))).thenReturn(emptyPage);
    }

    @Test
    void minPagesGreaterThanMaxPages_returns400() throws Exception {
        mockMvc.perform(get("/api/books")
                        .param("minPages", "500")
                        .param("maxPages", "100"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0].field").value("pageRangeValid"))
                .andExpect(jsonPath("$.errors[0].message")
                        .value("minPages must be less than or equal to maxPages"));
    }

    @Test
    void minPagesGreaterThanMaxPages_neverCallsService() throws Exception {
        mockMvc.perform(get("/api/books")
                        .param("minPages", "500")
                        .param("maxPages", "100"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookService);
    }

    @Test
    void validRange_returns200AndPassesValuesToService() throws Exception {
        mockMvc.perform(get("/api/books")
                        .param("minPages", "100")
                        .param("maxPages", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());

        BookFilter filter = captureFilter();
        assertEquals(100, filter.minPages());
        assertEquals(500, filter.maxPages());
    }

    @Test
    void equalMinAndMaxPages_returns200() throws Exception {
        mockMvc.perform(get("/api/books")
                        .param("minPages", "300")
                        .param("maxPages", "300"))
                .andExpect(status().isOk());
    }

    @Test
    void onlyMinPages_returns200() throws Exception {
        mockMvc.perform(get("/api/books")
                        .param("minPages", "300"))
                .andExpect(status().isOk());

        BookFilter filter = captureFilter();
        assertEquals(300, filter.minPages());
        assertNull(filter.maxPages());
    }

    @Test
    void noFilters_returns200EnvelopeWithPageMetadataInsideData() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Books retrieved"))
                .andExpect(jsonPath("$.errors").isEmpty())
                .andExpect(jsonPath("$.errorCode").value(nullValue()))
                .andExpect(jsonPath("$.timestamp", endsWith("Z")))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.page.number").value(0))
                .andExpect(jsonPath("$.data.page.size").value(10))
                .andExpect(jsonPath("$.data.page.totalElements").value(0))
                .andExpect(jsonPath("$.data.page.totalPages").value(0))
                .andExpect(jsonPath("$.data.first").doesNotExist())
                .andExpect(jsonPath("$.data.last").doesNotExist());
    }

    @Test
    void bookInList_hasNestedAuthorAndGenresWithIds() throws Exception {
        BookResponse dune = new BookResponse(1L, "Dune", "9780441172719", 688, 1965,
                new AuthorSummary(1L, "Frank Herbert"),
                List.of(new GenreSummary(3L, "Classic"), new GenreSummary(1L, "Science Fiction")));
        when(bookService.search(any(BookFilter.class), any(Pageable.class)))
                .thenReturn(new PageResponse<>(List.of(dune), new PageResponse.PageMetadata(0, 10, 1, 1)));

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].author.id").value(1))
                .andExpect(jsonPath("$.data.content[0].author.name").value("Frank Herbert"))
                .andExpect(jsonPath("$.data.content[0].genres[0].id").value(3))
                .andExpect(jsonPath("$.data.content[0].genres[0].name").value("Classic"))
                .andExpect(jsonPath("$.data.content[0].genres[1].name").value("Science Fiction"))
                .andExpect(jsonPath("$.data.content[0].authorId").doesNotExist())
                .andExpect(jsonPath("$.data.content[0].authorName").doesNotExist())
                .andExpect(jsonPath("$.data.page.totalElements").value(1));
    }

    @Test
    void bookWithoutAuthor_hasNullAuthorAndEmptyGenres() throws Exception {
        when(bookService.findById(2L))
                .thenReturn(new BookResponse(2L, "Untitled", null, null, null, null, List.of()));

        mockMvc.perform(get("/api/books/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.author").value(nullValue()))   // key present, value null
                .andExpect(jsonPath("$.data.isbn").value(nullValue()))
                .andExpect(jsonPath("$.data.genres").isEmpty());
    }

    @Test
    void create_returns201EnvelopeAndKeepsLocationHeader() throws Exception {
        when(bookService.create(any(BookRequest.class)))
                .thenReturn(new BookResponse(5L, "Dune", null, null, null, null, List.of()));

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"title\": \"Dune\" }"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/books/5")))
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Book created"))
                .andExpect(jsonPath("$.data.id").value(5))
                .andExpect(jsonPath("$.errors").isEmpty())
                .andExpect(jsonPath("$.errorCode").value(nullValue()));
    }

    /** Returns the BookFilter the controller handed to the service. */
    private BookFilter captureFilter() {
        ArgumentCaptor<BookFilter> captor = ArgumentCaptor.forClass(BookFilter.class);
        verify(bookService).search(captor.capture(), any(Pageable.class));
        return captor.getValue();
    }
}
