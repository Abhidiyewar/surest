package com.surest.service;

import com.surest.model.Role;
import com.surest.model.User;
import com.surest.repository.RoleRepository;
import com.surest.repository.UserRepository;
import com.surest.security.jwt.LoginRequest;
import com.surest.security.jwt.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private UUID roleId;
    private UUID userId;
    private Role role;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        roleId = UUID.randomUUID();
        userId = UUID.randomUUID();

        role = Role.builder().id(roleId).name("ROLE_USER").build();
        user = User.builder().id(userId).username("john").passwordHash("$2a$10$encodedPass").role(role).build();
    }


    @Test
    @DisplayName("register() -> creates and saves user when username not exists")
    void register_success() {
        RegisterRequest registerRequest = new RegisterRequest(userId, "john", "plainPass", roleId);

        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("plainPass")).thenReturn("$2a$10$encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = authService.register(registerRequest);

        assertThat(saved.getId()).isEqualTo(userId);
        assertThat(saved.getUsername()).isEqualTo("john");
        assertThat(saved.getRole().getName()).isEqualTo("ROLE_USER");
        assertThat(saved.getPasswordHash()).isEqualTo("$2a$10$encoded");

        verify(userRepository).existsByUsername("john");
        verify(roleRepository).findById(roleId);
        verify(passwordEncoder).encode("plainPass");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register() -> throws when username already exists")
    void register_usernameExists() {
        RegisterRequest registerRequest = new RegisterRequest(userId, "john", "john123", roleId);
        when(userRepository.existsByUsername("john")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("username already exists");

        verify(userRepository).existsByUsername("john");
        verifyNoMoreInteractions(roleRepository, passwordEncoder);
    }

    @Test
    @DisplayName("register() -> throws when role not found")
    void register_roleNotFound() {
        RegisterRequest registerRequest = new RegisterRequest(userId, "john", "john123", roleId);
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.register(registerRequest)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("role not found");

        verify(userRepository).existsByUsername("john");
        verify(roleRepository).findById(roleId);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("authenticate() -> returns UserDetails when password matches")
    void authenticate_success() {
        LoginRequest loginRequest = new LoginRequest("john", "plain");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("plain", "$2a$10$encodedPass")).thenReturn(true);

        UserDetails ud = authService.authenticate(loginRequest);

        assertThat(ud.getUsername()).isEqualTo("john");
        assertThat(ud.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");

        verify(userRepository).findByUsername("john");
        verify(passwordEncoder).matches("plain", "$2a$10$encodedPass");
    }

    @Test
    @DisplayName("authenticate() -> throws UsernameNotFoundException when user missing")
    void authenticate_userNotFound() {
        LoginRequest loginRequest = new LoginRequest("ghost", "pass");
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.authenticate(loginRequest)).isInstanceOf(UsernameNotFoundException.class).hasMessageContaining("User not found");

        verify(userRepository).findByUsername("ghost");
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("authenticate() -> throws BadCredentialsException when password mismatch")
    void authenticate_badPassword() {
        LoginRequest loginRequest = new LoginRequest("john", "wrong");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "$2a$10$encodedPass")).thenReturn(false);

        assertThatThrownBy(() -> authService.authenticate(loginRequest)).isInstanceOf(BadCredentialsException.class).hasMessageContaining("Invalid username or password");

        verify(userRepository).findByUsername("john");
        verify(passwordEncoder).matches("wrong", "$2a$10$encodedPass");
    }
}
