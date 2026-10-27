package com.sara.booktracker.service.impl;

import com.sara.booktracker.dto.GenreCount;
import com.sara.booktracker.dto.ReadingStatsResponse;
import com.sara.booktracker.entity.ReadingStatus;
import com.sara.booktracker.exception.ResourceNotFoundException;
import com.sara.booktracker.repository.ShelfEntryRepository;
import com.sara.booktracker.repository.UserRepository;
import com.sara.booktracker.service.ReadingStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReadingStatsServiceImpl implements ReadingStatsService {

    private final ShelfEntryRepository shelfEntryRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    @Override
    public ReadingStatsResponse getStats(Long userId, Integer year) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }

        // No year sent → use the current year
        int targetYear = (year != null) ? year : LocalDate.now(clock).getYear();
        LocalDate from = LocalDate.of(targetYear, 1, 1);
        LocalDate to = LocalDate.of(targetYear, 12, 31);
        ReadingStatus finished = ReadingStatus.FINISHED;

        long booksFinished = shelfEntryRepository
                .countByUserIdAndStatusAndFinishedAtBetween(userId, finished, from, to);
        long pagesRead = shelfEntryRepository
                .sumPagesFinished(userId, finished, from, to);
        List<GenreCount> byGenre = shelfEntryRepository
                .countFinishedPerGenre(userId, finished, from, to);

        return new ReadingStatsResponse(userId, targetYear, booksFinished, pagesRead, byGenre);
    }
}