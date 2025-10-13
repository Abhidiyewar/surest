package com.surest.service.impl;

import com.surest.model.User;
import com.surest.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository repo;
    private final BCryptPasswordEncoder encoder;

    public UserDetailsServiceImpl(UserRepository repo, BCryptPasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = repo.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new org.springframework.security.core.userdetails.User(u.getUsername(), u.getPasswordHash(),
                List.of(new SimpleGrantedAuthority(u.getRole().getName())));
    }

    public org.springframework.security.core.userdetails.User authenticate(String username, String rawPassword) {
        User u = repo.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (!encoder.matches(rawPassword, u.getPasswordHash())) {
            throw new BadCredentialsException("Invalid username or password");
        }
        return new org.springframework.security.core.userdetails.User(u.getUsername(), u.getPasswordHash(),
                List.of(new SimpleGrantedAuthority(u.getRole().getName())));
    }
}
