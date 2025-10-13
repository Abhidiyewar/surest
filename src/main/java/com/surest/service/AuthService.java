package com.surest.service;

import com.surest.model.Role;
import com.surest.model.User;
import com.surest.repository.RoleRepository;
import com.surest.repository.UserRepository;
import com.surest.security.jwt.LoginRequest;
import com.surest.security.jwt.RegisterRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
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
        // enforce required fields (validation already ensures non-null/blank)
        if (userRepository.existsByUsername(req.username())) {
            throw new IllegalArgumentException("username already exists");
        }

        // find role by id (required)
        Role role = roleRepository.findById(req.roleId())
                .orElseThrow(() -> new IllegalArgumentException("role not found"));

        User u = new User();
        u.setId(req.id());
        u.setUsername(req.username());
        u.setPasswordHash(passwordEncoder.encode(req.password()));
        u.setRole(role);

        return userRepository.save(u);
    }

    public UserDetails authenticate(LoginRequest req) {
        var user = userRepository.findByUsername(req.username())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        boolean matches = passwordEncoder.matches(req.password(), user.getPasswordHash());
        if (!matches) {
            throw new BadCredentialsException("Invalid username or password");
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(user.getRole().getName()))
        );
    }
}
