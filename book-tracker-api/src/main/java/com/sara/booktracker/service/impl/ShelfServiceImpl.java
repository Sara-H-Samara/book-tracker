package com.sara.booktracker.service.impl;

import com.sara.booktracker.dto.AddToShelfRequest;
import com.sara.booktracker.dto.ShelfEntryResponse;
import com.sara.booktracker.entity.Book;
import com.sara.booktracker.entity.ShelfEntry;
import com.sara.booktracker.entity.User;
import com.sara.booktracker.exception.DuplicateResourceException;
import com.sara.booktracker.exception.ResourceNotFoundException;
import com.sara.booktracker.factory.ShelfEntryFactory;
import com.sara.booktracker.mapper.ShelfEntryMapper;
import com.sara.booktracker.repository.BookRepository;
import com.sara.booktracker.repository.ShelfEntryRepository;
import com.sara.booktracker.repository.UserRepository;
import com.sara.booktracker.service.ShelfService;
import com.sara.booktracker.singleton.ActivityLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShelfServiceImpl implements ShelfService {

    private final ShelfEntryRepository shelfEntryRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final ShelfEntryFactory shelfEntryFactory;
    private final ShelfEntryMapper shelfEntryMapper;

    @Override
    @Transactional
    public ShelfEntryResponse addToShelf(AddToShelfRequest request) {
        // Business rule: a book can be on a user's shelf only once
        if (shelfEntryRepository.existsByUserIdAndBookId(request.userId(), request.bookId())) {
            throw new DuplicateResourceException("Book " + request.bookId() + " is already on this user's shelf");
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.userId()));
        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book", request.bookId()));

        // Factory: the service doesn't know HOW to build an entry, it asks the factory
        ShelfEntry entry = shelfEntryFactory.create(user, book, request.status());

        ShelfEntry saved = shelfEntryRepository.save(entry);

        // Singleton: the same ActivityLog instance that BookServiceImpl writes to
        ActivityLog.getInstance().record(
                user.getUsername() + " added '" + book.getTitle() + "' to the shelf as " + saved.getStatus()
        );

        return shelfEntryMapper.toResponse(saved);
    }

    @Override
    public List<ShelfEntryResponse> getShelf(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }
        return shelfEntryRepository.findByUserId(userId).stream()
                .map(shelfEntryMapper::toResponse)
                .toList();
    }
}