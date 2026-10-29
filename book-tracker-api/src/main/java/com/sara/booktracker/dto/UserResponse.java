package com.sara.booktracker.dto;

import com.sara.booktracker.entity.Role;

public record UserResponse(
        Long id,
        String username,
        String email,
        Role role
) {}