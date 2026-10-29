package com.sara.booktracker.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    // Fake key, only for tests (32 bytes in Base64). Protects nothing real.
    private static final String SECRET =
            "dGVzdC1zZWNyZXQta2V5LWZvci10ZXN0cy1vbmx5LTMyYnl0ZXM=";

    // 1 hour lifetime. We create it with "new": no Spring needed.
    private final JwtService jwtService = new JwtService(SECRET, 3_600_000);

    @Test
    void generatedToken_containsTheUsername() {
        String token = jwtService.generateToken("eman", "USER");

        assertEquals("eman", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void changedToken_isNotValid() {
        String token = jwtService.generateToken("eman", "USER");

        // Damage the signature: replace the last 2 characters
        String changed = token.substring(0, token.length() - 2) + "xx";

        assertFalse(jwtService.isTokenValid(changed));
    }

    @Test
    void expiredToken_isNotValid() {
        // Negative lifetime: the token is already expired when created
        JwtService shortLived = new JwtService(SECRET, -1000);
        String token = shortLived.generateToken("eman", "USER");

        assertFalse(shortLived.isTokenValid(token));
    }

    @Test
    void randomText_isNotValid() {
        assertFalse(jwtService.isTokenValid("hello"));
        assertFalse(jwtService.isTokenValid(""));
    }
}