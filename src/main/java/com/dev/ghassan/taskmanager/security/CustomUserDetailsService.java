package com.dev.ghassan.taskmanager.security;

import com.dev.ghassan.taskmanager.model.User;
import com.dev.ghassan.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);
        
        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> {
                    log.warn("Authentication attempt with non-existent email: {}", email);
                    return new UsernameNotFoundException("Invalid email or password");
                });

        log.debug("User found: {}, enabled: {}, locked: {}", user.getEmail(), user.getEnabled(), user.getAccountLocked());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .disabled(!user.getEnabled())
                .accountLocked(user.getAccountLocked())
                .authorities(new ArrayList<>()) // Add roles/authorities here if needed
                .build();
    }
}