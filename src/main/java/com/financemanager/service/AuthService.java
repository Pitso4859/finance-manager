package com.financemanager.service;

import com.financemanager.exception.AuthenticationException;
import com.financemanager.exception.ValidationException;
import com.financemanager.model.User;
import com.financemanager.repository.UserRepository;
import com.financemanager.security.PasswordHasher;
import com.financemanager.security.SessionManager;
import com.financemanager.util.Validation;

import java.util.Arrays;

public final class AuthService {
    private final UserRepository users;
    private final PasswordHasher passwords;
    private final SessionManager session;

    public AuthService(UserRepository users, PasswordHasher passwords, SessionManager session) {
        this.users = users; this.passwords = passwords; this.session = session;
    }

    public User register(String fullName, String email, char[] password, char[] confirm) {
        String name = Validation.requiredText(fullName, "Full name", 100);
        String normalizedEmail = Validation.email(email);
        if (password.length < 8) throw new ValidationException("Password must contain at least 8 characters.");
        if (!Arrays.equals(password, confirm)) throw new ValidationException("Passwords do not match.");
        if (users.existsByEmail(normalizedEmail)) throw new ValidationException("An account already exists for this email address.");
        try {
            return users.save(new User(name, normalizedEmail, passwords.hash(password)));
        } finally {
            Arrays.fill(password, '\0'); Arrays.fill(confirm, '\0');
        }
    }

    public User login(String email, char[] password) {
        String normalizedEmail = Validation.email(email);
        try {
            User user = users.findByEmail(normalizedEmail)
                    .orElseThrow(() -> new AuthenticationException("Invalid email address or password."));
            if (!passwords.verify(password, user.getPasswordHash())) {
                throw new AuthenticationException("Invalid email address or password.");
            }
            session.login(user);
            return user;
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    public void logout() { session.logout(); }
}
