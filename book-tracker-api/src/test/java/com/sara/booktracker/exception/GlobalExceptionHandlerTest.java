package com.sara.booktracker.exception;

import com.sara.booktracker.MockSecurityBeans;
import com.sara.booktracker.controller.BookController;
import com.sara.booktracker.controller.ShelfController;
import com.sara.booktracker.dto.BookResponse;
import com.sara.booktracker.service.BookService;
import com.sara.booktracker.service.ShelfService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Every kind of error must come back in the same ApiResponse envelope as successes
 * (status, message, data = null, errors, errorCode, timestamp) and must never leak internals.
 */
@WebMvcTest(controllers = {BookController.class, ShelfController.class})
@MockSecurityBeans
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private ShelfService shelfService;

    // ----- our own exceptions -----

    @Test
    void resourceNotFound_returns404InFullEnvelope() throws Exception {
        when(bookService.findById(99L)).thenThrow(new ResourceNotFoundException("Book", 99L));

        mockMvc.perform(get("/api/books/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(6))   // exactly the six envelope fields
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Book with id 99 not found"))
                .andExpect(jsonPath("$.data").value(nullValue()))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors").isEmpty())
                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.timestamp", endsWith("Z")));
    }

    @Test
    void fieldError_hasNullDataAndStatusInBodyMatchingHttpStatus() throws Exception {
        mockMvc.perform(get("/api/books/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.data").value(nullValue()))
                .andExpect(jsonPath("$.errors.length()").value(1));
    }

    // ----- bad requests (400) -----

    @Test
    void invalidPathVariableType_returns400WithFieldError() throws Exception {
        mockMvc.perform(get("/api/books/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_PARAMETER"))
                .andExpect(jsonPath("$.message").value("'id' must be of type Long"))
                .andExpect(jsonPath("$.errors[0].field").value("id"))
                .andExpect(jsonPath("$.errors[0].message").value("must be of type Long"));

        verifyNoInteractions(bookService);
    }

    @Test
    void missingRequiredParameter_returns400WithFieldError() throws Exception {
        mockMvc.perform(get("/api/shelf"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("MISSING_PARAMETER"))
                .andExpect(jsonPath("$.message").value("Required parameter 'userId' is missing"))
                .andExpect(jsonPath("$.errors[0].field").value("userId"))
                .andExpect(jsonPath("$.errors[0].message").value("is required"));

        verifyNoInteractions(shelfService);
    }

    @Test
    void invalidFilterValue_returns400WithoutJavaTypeNames() throws Exception {
        mockMvc.perform(get("/api/books").param("minPages", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[0].field").value("minPages"))
                .andExpect(jsonPath("$.errors[0].message").value("has an invalid value"))
                .andExpect(content().string(not(containsString("java.lang"))));

        verifyNoInteractions(bookService);
    }

    @Test
    void invalidRequestBody_listsEveryBrokenField() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"title\": \"\", \"isbn\": \"123\" }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.length()").value(2))
                .andExpect(jsonPath("$.errors[*].field", containsInAnyOrder("title", "isbn")))
                .andExpect(jsonPath("$.errors[*].message",
                        containsInAnyOrder("title is required", "isbn must be 10 or 13 digits")));

        verifyNoInteractions(bookService);
    }

    @Test
    void malformedJson_returns400() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"title\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("MALFORMED_REQUEST"))
                .andExpect(jsonPath("$.message").value("Request body is missing or is not valid JSON"));
    }

    // ----- wrong URL / method / content type -----

    @Test
    void unknownEndpoint_returns404EndpointNotFound() throws Exception {
        mockMvc.perform(get("/api/nothing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("ENDPOINT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("No endpoint GET /api/nothing"));
    }

    @Test
    void unsupportedMethod_returns405WithAllowHeader() throws Exception {
        mockMvc.perform(delete("/api/books"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().exists("Allow"))
                .andExpect(jsonPath("$.errorCode").value("METHOD_NOT_ALLOWED"))
                .andExpect(jsonPath("$.message").value("Method DELETE is not supported here"));
    }

    @Test
    void wrongContentType_returns415() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Dune"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.errorCode").value("UNSUPPORTED_MEDIA_TYPE"));
    }

    /** Before ResponseEntityExceptionHandler, this fell into the catch-all and became a 500. */
    @Test
    void unsupportedAcceptHeader_returns406NotA500() throws Exception {
        when(bookService.findById(1L))
                .thenReturn(new BookResponse(1L, "Dune", null, 688, 1965, null, List.of()));

        mockMvc.perform(get("/api/books/1").accept(MediaType.APPLICATION_XML))
                .andExpect(status().isNotAcceptable())
                .andExpect(jsonPath("$.errorCode").value("NOT_ACCEPTABLE"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ----- database and unexpected errors: no internals may leak -----

    @Test
    void databaseConstraintViolation_returns409WithoutDatabaseMessage() throws Exception {
        when(bookService.create(any()))
                .thenThrow(new DataIntegrityViolationException("duplicate key violates constraint \"uk_books_isbn\""));

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"title\": \"Dune\" }"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("DATA_CONFLICT"))
                .andExpect(jsonPath("$.message").value("The request conflicts with existing data"))
                .andExpect(content().string(not(containsString("uk_books_isbn"))));
    }

    @Test
    void unexpectedException_returns500WithGenericMessageOnly() throws Exception {
        when(bookService.findById(1L))
                .thenThrow(new IllegalStateException("connection to jdbc:postgresql://db:5432 refused"));

        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("Something went wrong. Please try again later."))
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.exception").doesNotExist())
                .andExpect(content().string(not(containsString("jdbc"))))
                .andExpect(content().string(not(containsString("IllegalStateException"))));
    }
}
