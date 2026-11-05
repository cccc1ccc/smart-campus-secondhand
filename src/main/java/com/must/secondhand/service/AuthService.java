package com.must.secondhand.service;

import com.must.secondhand.common.BizException;
import com.must.secondhand.dto.LoginForm;
import com.must.secondhand.dto.RegisterForm;
import com.must.secondhand.model.entity.User;
import com.must.secondhand.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

/** Registration, login and account lockout (REQ-1.1 / REQ-1.2). */
@Service
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(10);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(RegisterForm form) {
        if (!form.password().equals(form.confirmPassword())) {
            throw new BizException("The two passwords do not match");
        }
        String email = form.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByStudentNo(form.studentNo().trim())) {
            throw new BizException("This student number is already registered");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BizException("This e-mail address is already registered");
        }
        User user = new User(form.name().trim(), form.studentNo().trim(), email,
                passwordEncoder.encode(form.password()));
        user.setCreatedAt(Instant.now());
        return userRepository.save(user);
    }

    /**
     * Verifies the credential against the stored salted hash. After five consecutive failures
     * the account is locked for ten minutes (SEC-1.4).
     */
    @Transactional
    public User login(LoginForm form) {
        String identifier = form.identifier().trim();
        Optional<User> found = identifier.contains("@")
                ? userRepository.findByEmailIgnoreCase(identifier)
                : userRepository.findByStudentNo(identifier);
        User user = found.orElseThrow(() -> BizException.unauthorized("Invalid credential"));

        Instant now = Instant.now();
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(now)) {
            long minutes = Math.max(1, Duration.between(now, user.getLockedUntil()).toMinutes() + 1);
            throw new BizException(HttpStatus.LOCKED,
                    "Account locked after 5 failed attempts. Try again in " + minutes + " minute(s).");
        }

        if (!passwordEncoder.matches(form.password(), user.getPasswordHash())) {
            int attempts = user.getFailedAttempts() + 1;
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                user.setFailedAttempts(0);
                user.setLockedUntil(now.plus(LOCK_DURATION));
            } else {
                user.setFailedAttempts(attempts);
            }
            userRepository.save(user);
            throw BizException.unauthorized("Invalid credential");
        }

        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        return userRepository.save(user);
    }
}
