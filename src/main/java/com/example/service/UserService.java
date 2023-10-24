package com.example.service;

import com.example.model.User;
import com.example.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElse(null);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = getByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("Invalid username: '" + username + "'");
        }
        return user;
    }

    public User getCurrentUser() {

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        if (authentication.getPrincipal() instanceof User user) {
            return user;
        }
        if (authentication.getPrincipal() instanceof UserDetails userDetails) {
            return getByUsername(userDetails.getUsername());
        }
        if (authentication.getPrincipal() instanceof Principal principal) {
            return getByUsername(principal.getName());
        }

        return null;
    }
}
