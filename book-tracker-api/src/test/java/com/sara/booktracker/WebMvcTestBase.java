package com.sara.booktracker;

import com.sara.booktracker.security.JwtService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Base for @WebMvcTest classes: the JWT filter is loaded in web slices,
 * but its dependencies are not, so we replace them with mocks.
 */
public abstract class WebMvcTestBase {

    @MockitoBean
    protected JwtService jwtService;

    @MockitoBean
    protected UserDetailsService userDetailsService;
}