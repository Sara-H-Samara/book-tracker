package com.sara.booktracker.service;

import com.sara.booktracker.dto.AuthResponse;
import com.sara.booktracker.dto.LoginRequest;
import com.sara.booktracker.dto.RegisterRequest;
import com.sara.booktracker.dto.UserResponse;

public interface AuthService {

    UserResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}