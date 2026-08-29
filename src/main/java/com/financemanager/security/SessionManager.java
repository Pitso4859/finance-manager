package com.financemanager.security;

import com.financemanager.model.User;

import java.util.Optional;

public final class SessionManager {
    private User currentUser;

    public void login(User user) { currentUser = user; }
    public void logout() { currentUser = null; }
    public Optional<User> currentUser() { return Optional.ofNullable(currentUser); }

    public User requireUser() {
        if (currentUser == null) {
            throw new IllegalStateException("No authenticated user is available.");
        }
        return currentUser;
    }
}
