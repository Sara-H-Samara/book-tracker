package com.sara.booktracker.mapper;

import com.sara.booktracker.dto.ShelfEntryResponse;
import com.sara.booktracker.entity.ShelfEntry;
import org.springframework.stereotype.Component;

@Component
public class ShelfEntryMapper {

    private final BookMapper bookMapper;

    public ShelfEntryMapper(BookMapper bookMapper) {
        this.bookMapper = bookMapper;
    }

    public ShelfEntryResponse toResponse(ShelfEntry entry) {
        return new ShelfEntryResponse(
                entry.getId(),
                entry.getStatus(),
                entry.getCurrentPage(),
                entry.getStartedAt(),
                entry.getFinishedAt(),
                bookMapper.toSummary(entry.getBook())
        );
    }
}
