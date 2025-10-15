package com.surest.service;

import com.surest.model.Role;
import com.surest.model.User;
import com.surest.repository.UserRepository;
import com.surest.service.impl.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock private UserRepository repo;
    @Mock private BCryptPasswordEncoder encoder;

    @InjectMocks private UserDetailsServiceImpl service;

    private User buildUser(String username, String hash, String roleName) {
        Role role = new Role();
        role.setName(roleName);
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(hash);
        user.setRole(role);
        return user;
    }

    private static boolean hasAuthority(Collection<? extends GrantedAuthority> auths, String role) {
        return auths.stream().anyMatch(a -> a.getAuthority().equals(role));
    }

    @Test
    void loadUserByUsername_found_mapsToSpringUserWithAuthorities() {
        User user = buildUser("rahul", "$2a$10$abcdef", "ROLE_USER");
        when(repo.findByUsername("rahul")).thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername("rahul");

        assertEquals("rahul", details.getUsername());
        assertEquals("$2a$10$abcdef", details.getPassword());
        assertTrue(hasAuthority(details.getAuthorities(), "ROLE_USER"));
        verify(repo).findByUsername("rahul");
        verifyNoInteractions(encoder);
    }

    @Test
    void loadUserByUsername_notFound_throwsUsernameNotFound() {
        when(repo.findByUsername("missing")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("missing"));
        verify(repo).findByUsername("missing");
        verifyNoInteractions(encoder);
    }

    @Test
    void authenticate_success_returnsSpringUser() {
        User user = buildUser("mike", "$2a$10$cbcdef", "ROLE_ADMIN");
        when(repo.findByUsername("mike")).thenReturn(Optional.of(user));
        when(encoder.matches("mike123", "$2a$10$cbcdef")).thenReturn(true);

        org.springframework.security.core.userdetails.User out = service.authenticate("mike", "mike123");

        assertEquals("mike", out.getUsername());
        assertEquals("$2a$10$cbcdef", out.getPassword());
        assertTrue(hasAuthority(out.getAuthorities(), "ROLE_ADMIN"));
        verify(repo).findByUsername("mike");
        verify(encoder).matches("mike123", "$2a$10$cbcdef");
    }

    @Test
    void authenticate_badPassword_throwsBadCredentials() {
        User user = buildUser("rahul", "$2a$10$abcdef", "ROLE_USER");
        when(repo.findByUsername("rahul")).thenReturn(Optional.of(user));
        when(encoder.matches("wrong", "$2a$10$abcdef")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> service.authenticate("rahul", "wrong"));
        verify(repo).findByUsername("rahul");
        verify(encoder).matches("wrong", "$2a$10$abcdef");
    }

    @Test
    void authenticate_userNotFound_throwsUsernameNotFound() {
        when(repo.findByUsername("abhi")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.authenticate("abhi", "abhi123"));
        verify(repo).findByUsername("abhi");
        verifyNoInteractions(encoder);
    }
}
