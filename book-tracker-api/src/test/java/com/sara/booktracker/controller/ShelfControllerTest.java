package com.sara.booktracker.controller;

import com.sara.booktracker.MockSecurityBeans;
import com.sara.booktracker.dto.AddToShelfRequest;
import com.sara.booktracker.dto.AuthorSummary;
import com.sara.booktracker.dto.BookSummary;
import com.sara.booktracker.dto.PageResponse;
import com.sara.booktracker.dto.ShelfEntryResponse;
import com.sara.booktracker.entity.ReadingStatus;
import com.sara.booktracker.exception.ResourceNotFoundException;
import com.sara.booktracker.service.ShelfService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer tests for /api/shelf: pagination parameters, the page envelope
 * and the nested book summary. ShelfService is a mock.
 */
@WebMvcTest(ShelfController.class)
@MockSecurityBeans
class ShelfControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShelfService shelfService;

    private final ShelfEntryResponse duneEntry = new ShelfEntryResponse(
            1L, ReadingStatus.READING, 0, LocalDate.of(2026, 10, 28), null,
            new BookSummary(1L, "Dune", 688, new AuthorSummary(1L, "Frank Herbert")));

    // ----- GET: pagination parameters -----

    @Test
    void getShelf_withoutPagingParams_usesFirstPageOf20SortedById() throws Exception {
        stubShelf(List.of(), 0, 20, 0);

        mockMvc.perform(get("/api/shelf").param("userId", "1"))
                .andExpect(status().isOk());

        Pageable pageable = capturePageable();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(20, pageable.getPageSize());
        assertEquals(Sort.by("id"), pageable.getSort());
    }

    @Test
    void getShelf_passesPageSizeAndSortToService() throws Exception {
        stubShelf(List.of(), 1, 5, 0);

        mockMvc.perform(get("/api/shelf")
                        .param("userId", "1")
                        .param("page", "1")
                        .param("size", "5")
                        .param("sort", "book.title,desc"))
                .andExpect(status().isOk());

        Pageable pageable = capturePageable();
        assertEquals(1, pageable.getPageNumber());
        assertEquals(5, pageable.getPageSize());
        assertEquals(Sort.by(Sort.Direction.DESC, "book.title"), pageable.getSort());
    }

    @Test
    void getShelf_sizeAboveMaximum_isCappedAt50() throws Exception {
        stubShelf(List.of(), 0, 50, 0);

        mockMvc.perform(get("/api/shelf").param("userId", "1").param("size", "500"))
                .andExpect(status().isOk());

        assertEquals(50, capturePageable().getPageSize());   // spring.data.web.pageable.max-page-size
    }

    @Test
    void getShelf_negativePage_isTreatedAsFirstPage() throws Exception {
        stubShelf(List.of(), 0, 20, 0);

        mockMvc.perform(get("/api/shelf").param("userId", "1").param("page", "-1"))
                .andExpect(status().isOk());

        assertEquals(0, capturePageable().getPageNumber());
    }

    // ----- GET: response shape -----

    @Test
    void getShelf_returnsPageEnvelopeWithNestedBookSummary() throws Exception {
        stubShelf(List.of(duneEntry), 1, 2, 3);

        mockMvc.perform(get("/api/shelf").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].status").value("READING"))
                .andExpect(jsonPath("$.data.content[0].currentPage").value(0))
                .andExpect(jsonPath("$.data.content[0].startedAt").value("2026-10-28"))
                .andExpect(jsonPath("$.data.content[0].book.id").value(1))
                .andExpect(jsonPath("$.data.content[0].book.title").value("Dune"))
                .andExpect(jsonPath("$.data.content[0].book.pages").value(688))
                .andExpect(jsonPath("$.data.content[0].book.author.id").value(1))
                .andExpect(jsonPath("$.data.content[0].book.author.name").value("Frank Herbert"))
                .andExpect(jsonPath("$.data.content[0].bookId").doesNotExist())
                .andExpect(jsonPath("$.data.content[0].bookTitle").doesNotExist())
                .andExpect(jsonPath("$.data.content[0].totalPages").doesNotExist())
                .andExpect(jsonPath("$.data.page.number").value(1))
                .andExpect(jsonPath("$.data.page.size").value(2))
                .andExpect(jsonPath("$.data.page.totalElements").value(3))
                .andExpect(jsonPath("$.data.page.totalPages").value(2));
    }

    @Test
    void getShelf_unknownUser_returns404() throws Exception {
        when(shelfService.getShelf(eq(99L), any(Pageable.class)))
                .thenThrow(new ResourceNotFoundException("User", 99L));

        mockMvc.perform(get("/api/shelf").param("userId", "99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
    }

    // ----- POST -----

    @Test
    void addToShelf_returns201WithNestedBookAndNoLocationHeader() throws Exception {
        when(shelfService.addToShelf(any(AddToShelfRequest.class))).thenReturn(duneEntry);

        mockMvc.perform(post("/api/shelf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"userId\": 1, \"bookId\": 1, \"status\": \"READING\" }"))
                .andExpect(status().isCreated())
                .andExpect(header().doesNotExist("Location"))   // no GET /api/shelf/{id} to point to
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Book added to shelf"))
                .andExpect(jsonPath("$.errors").isEmpty())
                .andExpect(jsonPath("$.data.book.title").value("Dune"))
                .andExpect(jsonPath("$.data.book.author.name").value("Frank Herbert"))
                .andExpect(jsonPath("$.data.finishedAt").value(nullValue()));
    }

    // ----- helpers -----

    private void stubShelf(List<ShelfEntryResponse> content, int number, int size, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        when(shelfService.getShelf(eq(1L), any(Pageable.class)))
                .thenReturn(new PageResponse<>(content,
                        new PageResponse.PageMetadata(number, size, totalElements, totalPages)));
    }

    private Pageable capturePageable() {
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(shelfService).getShelf(eq(1L), captor.capture());
        return captor.getValue();
    }
}
