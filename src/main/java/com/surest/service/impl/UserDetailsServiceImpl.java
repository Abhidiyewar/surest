package com.surest.service.impl;

import com.surest.model.User;
import com.surest.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserDetailsServiceImpl(UserRepository repo, BCryptPasswordEncoder encoder) {
        this.userRepository = repo;
        this.passwordEncoder = encoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new org.springframework.security.core.userdetails.User(
                u.getUsername(),
                u.getPasswordHash(),
                List.of(new SimpleGrantedAuthority(u.getRole().getName()))
        );
    }

    public org.springframework.security.core.userdetails.User authenticate(String username, String rawPassword) {

        User u = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        log.debug("Authenticating user='{}', dbHashPresent={}", username, u.getPasswordHash() != null);
        boolean matches = passwordEncoder.matches(rawPassword, u.getPasswordHash());
        log.debug("Password matches? {}", matches);

        if (!matches) {
            throw new BadCredentialsException("Invalid username or password");
        }

        log.info("Authentication successful for username='{}'", username);
        return new org.springframework.security.core.userdetails.User(
                u.getUsername(),
                u.getPasswordHash(),
                List.of(new SimpleGrantedAuthority(u.getRole().getName()))
        );
    }
}
