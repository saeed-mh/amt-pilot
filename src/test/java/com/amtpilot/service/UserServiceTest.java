package com.amtpilot.service;

import java.util.Optional;
import java.util.UUID;

import com.amtpilot.auth.exception.EmailAlreadyExistsException;
import com.amtpilot.auth.exception.InvalidCredentialsException;
import com.amtpilot.entity.User;
import com.amtpilot.repository.UserRepository;
import com.amtpilot.user.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;


    @Test
    public void shouldReturnSuccessfullyRegisteredUser() {

        when(userRepository.existsByEmail("saeed@gmail.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("saeedsaeed10"))
                .thenReturn("HASHED_PASSWORD");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.register("Saeed@gmail.com", "saeedsaeed10");

        assertEquals("saeed@gmail.com", result.getEmail());
        assertEquals("HASHED_PASSWORD", result.getPasswordHash());
    }

    @Test
    public void shouldThrowExceptionWhenEmailAlreadyExists() {

        when(userRepository.existsByEmail("saeed@gmail.com"))
                .thenReturn(true);

        assertThrows(
                EmailAlreadyExistsException.class,
                () -> userService.register("saeed@gmail.com", "saeedsaeed10"));

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));

    }

    @Test
    public void shouldAuthenticateUserWithCorrectCredentials() {
        User user = new User("student@example.com", "HASHED_PASSWORD");

        when(userRepository.findByEmail("student@example.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correctPassword", "HASHED_PASSWORD"))
                .thenReturn(true);

        User result = userService.authenticate(
                " Student@Example.com ",
                "correctPassword");

        assertSame(user, result);
        verify(passwordEncoder).matches("correctPassword", "HASHED_PASSWORD");
    }

    @Test
    public void shouldRejectUnknownEmail() {
        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidCredentialsException.class,
                () -> userService.authenticate(
                        "unknown@example.com",
                        "correctPassword"));

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    public void shouldRejectWrongPassword() {
        User user = new User("student@example.com", "HASHED_PASSWORD");

        when(userRepository.findByEmail("student@example.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "HASHED_PASSWORD"))
                .thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class,
                () -> userService.authenticate(
                        "student@example.com",
                        "wrongPassword"));
    }

    @Test
    public void shouldReturnUserById() {
        UUID userId = UUID.randomUUID();
        User user = new User("student@example.com", "HASHED_PASSWORD");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = userService.getById(userId);

        assertSame(user, result);
        verify(userRepository).findById(userId);
    }

    @Test
    public void shouldThrowExceptionWhenUserIdDoesNotExist() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.getById(userId));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findById(userId);
    }

}
