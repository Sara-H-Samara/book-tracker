package com.sara.booktracker.service.impl;

import com.sara.booktracker.dto.AuthResponse;
import com.sara.booktracker.dto.LoginRequest;
import com.sara.booktracker.dto.RegisterRequest;
import com.sara.booktracker.dto.UserResponse;
import com.sara.booktracker.entity.Role;
import com.sara.booktracker.entity.User;
import com.sara.booktracker.exception.DuplicateResourceException;
import com.sara.booktracker.exception.InvalidCredentialsException;
import com.sara.booktracker.repository.UserRepository;
import com.sara.booktracker.service.AuthService;
import com.sara.booktracker.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        // 1. Clean the input
        String username = request.username().trim();
        String email = request.email().trim().toLowerCase();

        // 2. Check for duplicates
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("Username is already taken");
        }
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email is already registered");
        }

        // 3. Build the entity
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);

        // 4. Save
        User saved = userRepository.save(user);

        // 5. Return the DTO
        return new UserResponse(saved.getId(), saved.getUsername(), saved.getEmail(), saved.getRole());
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // 1. Find the user
        User user = userRepository.findByUsername(request.username().trim())
                .orElseThrow(InvalidCredentialsException::new);

        // 2. Check the password
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        // 3. Create the token
        String token = jwtService.generateToken(user.getUsername(), user.getRole().name());

        // 4. Send it back
        return new AuthResponse(token);
    }
}