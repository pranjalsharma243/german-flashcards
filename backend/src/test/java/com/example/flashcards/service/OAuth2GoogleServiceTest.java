package com.example.flashcards.service;

import com.example.flashcards.entity.AppUser;
import com.example.flashcards.model.AuthResponse;
import com.example.flashcards.repository.UserRepository;
import com.example.flashcards.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2GoogleServiceTest {

    @Mock UserRepository userRepository;
    @Mock JwtService jwtService;
    @InjectMocks OAuth2GoogleService service;

    @Test
    void createsNewUserOnFirstGoogleSignIn() {
        when(userRepository.findByProviderId("sub-123")).thenReturn(Optional.empty());
        when(userRepository.findByUsernameIgnoreCase("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.count()).thenReturn(5L);
        when(jwtService.createToken(anyString(), anyString())).thenReturn("jwt-token");

        AppUser savedUser = new AppUser();
        savedUser.setUsername("new@example.com");
        savedUser.setRole("USER");
        when(userRepository.save(any(AppUser.class))).thenReturn(savedUser);

        AuthResponse response = service.findOrCreateUser("new@example.com", "sub-123");

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).save(captor.capture());
        AppUser created = captor.getValue();
        assertEquals("new@example.com", created.getUsername());
        assertEquals("GOOGLE", created.getProvider());
        assertEquals("sub-123", created.getProviderId());
        assertEquals("USER", created.getRole());
        assertNotNull(response.token());
    }

    @Test
    void returnsExistingGoogleUserByProviderId() {
        AppUser existing = new AppUser();
        existing.setUsername("google@example.com");
        existing.setRole("USER");
        existing.setProvider("GOOGLE");
        existing.setProviderId("sub-456");

        when(userRepository.findByProviderId("sub-456")).thenReturn(Optional.of(existing));
        when(jwtService.createToken("google@example.com", "USER")).thenReturn("jwt-token");

        AuthResponse response = service.findOrCreateUser("google@example.com", "sub-456");

        verify(userRepository, never()).save(any());
        assertEquals("google@example.com", response.username());
        assertEquals("jwt-token", response.token());
    }

    @Test
    void linksExistingLocalAccountOnFirstGoogleSignIn() {
        AppUser localUser = new AppUser();
        localUser.setUsername("existing@example.com");
        localUser.setRole("ADMIN");
        localUser.setProvider("LOCAL");

        when(userRepository.findByProviderId("sub-789")).thenReturn(Optional.empty());
        when(userRepository.findByUsernameIgnoreCase("existing@example.com")).thenReturn(Optional.of(localUser));
        when(userRepository.save(localUser)).thenReturn(localUser);
        when(jwtService.createToken("existing@example.com", "ADMIN")).thenReturn("jwt-token");

        AuthResponse response = service.findOrCreateUser("existing@example.com", "sub-789");

        assertEquals("sub-789", localUser.getProviderId());
        assertEquals("ADMIN", response.role());
        verify(userRepository).save(localUser);
    }

    @Test
    void normalizesEmailToLowercase() {
        when(userRepository.findByProviderId("sub-000")).thenReturn(Optional.empty());
        when(userRepository.findByUsernameIgnoreCase("user@example.com")).thenReturn(Optional.empty());
        when(userRepository.count()).thenReturn(1L);

        AppUser saved = new AppUser();
        saved.setUsername("user@example.com");
        saved.setRole("USER");
        when(userRepository.save(any(AppUser.class))).thenReturn(saved);
        when(jwtService.createToken(anyString(), anyString())).thenReturn("jwt");

        service.findOrCreateUser("  USER@Example.COM  ", "sub-000");

        verify(userRepository).findByUsernameIgnoreCase("user@example.com");
    }

    @Test
    void firstUserGetsAdminRole() {
        when(userRepository.findByProviderId("sub-first")).thenReturn(Optional.empty());
        when(userRepository.findByUsernameIgnoreCase("admin@example.com")).thenReturn(Optional.empty());
        when(userRepository.count()).thenReturn(0L);

        AppUser saved = new AppUser();
        saved.setUsername("admin@example.com");
        saved.setRole("ADMIN");
        when(userRepository.save(any(AppUser.class))).thenReturn(saved);
        when(jwtService.createToken(anyString(), anyString())).thenReturn("jwt");

        service.findOrCreateUser("admin@example.com", "sub-first");

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).save(captor.capture());
        assertEquals("ADMIN", captor.getValue().getRole());
    }
}
