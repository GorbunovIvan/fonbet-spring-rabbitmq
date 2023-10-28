package com.example.service;

import com.example.model.Role;
import com.example.model.User;
import com.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    private List<User> users;
    private User currentUser;

    @BeforeEach
    void setUp() {

        users = List.of(
                new User(1, "admin", "admin", Role.ADMIN, true),
                new User(2, "user", "user", Role.USER, true)
        );

        currentUser = users.get(0);

        try (var ignored = MockitoAnnotations.openMocks(this)) {

            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getPrincipal()).thenReturn(currentUser);
            SecurityContextHolder.setContext(securityContext);

            when(userRepository.findByUsername("")).thenReturn(Optional.empty());
            for (var user : users) {
                when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        userService = new UserService(userRepository);
    }

    @Test
    void testGetByUsername() {

        for (var user : users) {
            assertEquals(user, userService.getByUsername(user.getUsername()));
            verify(userRepository, times(1)).findByUsername(user.getUsername());
        }

        assertNull(userService.getByUsername(""));
        verify(userRepository, times(1)).findByUsername("");
    }

    @Test
    void testLoadUserByUsername() {

        for (var user : users) {
            assertEquals(user, userService.loadUserByUsername(user.getUsername()));
            verify(userRepository, times(1)).findByUsername(user.getUsername());
        }

        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername(""));
        verify(userRepository, times(1)).findByUsername("");
    }

    @Test
    void testGetCurrentUser() {

        // is authenticated
        assertEquals(currentUser, userService.getCurrentUser());
        verify(authentication, times(1)).isAuthenticated();
        verify(authentication, times(1)).getPrincipal();

        // not authenticated
        Mockito.reset(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(authentication.getPrincipal()).thenReturn(null);

        assertNull(userService.getCurrentUser());
        verify(authentication, times(1)).isAuthenticated();
        verify(authentication, never()).getPrincipal();
    }
}