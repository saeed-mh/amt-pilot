package com.amtpilot.service;

import java.util.Locale;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.amtpilot.auth.exception.EmailAlreadyExistsException;
import com.amtpilot.auth.exception.InvalidCredentialsException;
import com.amtpilot.entity.User;
import com.amtpilot.repository.UserRepository;
import com.amtpilot.user.dto.UpdateUserProfileRequest;
import com.amtpilot.user.exception.UserNotFoundException;

import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(String email, String password) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException();
        }

        String hashedPassword = passwordEncoder.encode(password);
        User createdUser = new User(normalizedEmail, hashedPassword);
        return userRepository.save(createdUser);
    }

    @Transactional(readOnly = true)
    public User authenticate(String email, String password) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return user;
    }

    @Transactional(readOnly = true)
    public User getById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);
    }

    @Transactional
    public User updateProfile(UUID id, UpdateUserProfileRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);

        if (request.preferredLanguage() != null) {
            user.setPreferredLanguage(request.preferredLanguage().trim());
        }

        if (request.city() != null) {
            user.setCity(request.city().trim());
        }

        if (request.countryOfOrigin() != null) {
            user.setCountryOfOrigin(request.countryOfOrigin().trim());
        }

        if (request.userType() != null) {
            user.setUserType(request.userType().trim());
        }

        if (request.timezone() != null) {
            user.setTimezone(request.timezone().trim());
        }

        return user;
    }
}
