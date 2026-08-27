package com.amtpilot.service;

import com.amtpilot.auth.exception.EmailAlreadyExistsException;
import com.amtpilot.entity.User;
import com.amtpilot.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

}
