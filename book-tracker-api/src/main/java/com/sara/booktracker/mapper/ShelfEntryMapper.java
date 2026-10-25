package com.sara.booktracker.mapper;

import com.sara.booktracker.dto.ShelfEntryResponse;
import com.sara.booktracker.entity.Book;
import com.sara.booktracker.entity.ShelfEntry;
import org.springframework.stereotype.Component;

@Component
public class ShelfEntryMapper {

    public ShelfEntryResponse toResponse(ShelfEntry entry) {
        Book book = entry.getBook();
        return new ShelfEntryResponse(
                entry.getId(),
                book.getId(),
                book.getTitle(),
                entry.getStatus(),
                entry.getCurrentPage(),
                book.getPages(),
                entry.getStartedAt(),
                entry.getFinishedAt()
        );
    }
}