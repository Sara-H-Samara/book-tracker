package com.sara.booktracker;

import com.sara.booktracker.security.JwtService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * For @WebMvcTest classes: the JWT filter is loaded in web slices, but its dependencies
 * (JwtService, UserDetailsService) are not, so we replace them with mocks.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@MockitoBean(types = {JwtService.class, UserDetailsService.class})
public @interface MockSecurityBeans {
}