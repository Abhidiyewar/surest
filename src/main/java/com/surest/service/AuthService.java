package com.surest.service;

import com.surest.model.Role;
import com.surest.model.User;
import com.surest.repository.RoleRepository;
import com.surest.repository.UserRepository;
import com.surest.security.jwt.LoginRequest;
import com.surest.security.jwt.RegisterRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(RegisterRequest req) {
        log.info("Registration attempt for username='{}'", req.username());

        if (userRepository.existsByUsername(req.username())) {
            log.warn("Registration blocked: username '{}' already exists", req.username());
            throw new IllegalArgumentException("username already exists");
        }

        Role role = roleRepository.findById(req.roleId())
                .orElseThrow(() -> {
                    log.warn("Registration failed: role not found for roleId='{}'", req.roleId());
                    return new IllegalArgumentException("role not found");
                });

        User user = new User();
        user.setId(req.id());
        user.setUsername(req.username());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setRole(role);

        User saved = userRepository.save(user);
        log.info("User registered successfully: id='{}', username='{}', role='{}'",
                saved.getId(), saved.getUsername(), saved.getRole().getName());
        return saved;
    }

    public UserDetails authenticate(LoginRequest req) {

        var userOpt = userRepository.findByUsername(req.username());
        if (userOpt.isEmpty()) {
            log.warn("Login failed: user not found for username='{}'", req.username());
            throw new UsernameNotFoundException("User not found");
        }

        var user = userOpt.get();
        boolean matches = passwordEncoder.matches(req.password(), user.getPasswordHash());
        if (!matches) {
            log.warn("Login failed: bad credentials for username='{}'", req.username());
            throw new BadCredentialsException("Invalid username or password");
        }

        log.info("Login successful for username='{}'", req.username());
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(user.getRole().getName()))
        );
    }
}
