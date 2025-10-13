package com.surest.service.impl;

import com.surest.model.User;
import com.surest.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    private final UserRepository repo;
    private final BCryptPasswordEncoder encoder;

    public UserDetailsServiceImpl(UserRepository repo, BCryptPasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = repo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new org.springframework.security.core.userdetails.User(
                u.getUsername(),
                u.getPasswordHash(),
                List.of(new SimpleGrantedAuthority(u.getRole().getName()))
        );
    }

    // used by AuthController to authenticate on login
    public org.springframework.security.core.userdetails.User authenticate(String username, String rawPassword) {
        User u = repo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        log.debug("Authenticating user={}, dbHashPresent={}", username, u.getPasswordHash() != null);
        boolean matches = encoder.matches(rawPassword, u.getPasswordHash());
        log.debug("Password matches? {}", matches);

        if (!matches) {
            // Use BadCredentialsException so Spring semantics map it to 401 (via handler below)
            throw new BadCredentialsException("Invalid username or password");
        }

        return new org.springframework.security.core.userdetails.User(
                u.getUsername(),
                u.getPasswordHash(),
                List.of(new SimpleGrantedAuthority(u.getRole().getName()))
        );
    }
}
