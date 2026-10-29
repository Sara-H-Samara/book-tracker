package com.sara.booktracker.config;

import com.sara.booktracker.entity.Role;
import com.sara.booktracker.entity.User;
import com.sara.booktracker.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates the first ADMIN user when the app starts, if it doesn't exist yet.
 * Credentials come from env variables (ADMIN_USERNAME, ADMIN_EMAIL, ADMIN_PASSWORD), never from code.
 * Safe to run on every start: if the admin already exists, it does nothing.
 */
@Slf4j
@Component
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String email;
    private final String password;

    public AdminSeeder(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       @Value("${app.admin.username}") String username,
                       @Value("${app.admin.email}") String email,
                       @Value("${app.admin.password}") String password) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // 1. Nothing configured → skip
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            log.info("No admin configured (ADMIN_USERNAME / ADMIN_EMAIL / ADMIN_PASSWORD): skipping");
            return;
        }

        // 2. Already exists → skip (this is what makes it idempotent)
        if (userRepository.existsByUsername(username)) {
            log.info("Admin user '{}' already exists", username);
            return;
        }

        // 3. Create it
        User admin = new User();
        admin.setUsername(username.trim());
        admin.setEmail(email.trim().toLowerCase());
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);

        log.info("Created admin user '{}'", username);
    }
}