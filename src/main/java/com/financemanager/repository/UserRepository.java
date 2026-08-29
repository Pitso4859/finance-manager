package com.financemanager.repository;

import com.financemanager.model.User;
import com.financemanager.persistence.FileDataStore;

import java.util.Locale;
import java.util.Optional;

public final class UserRepository {
    private final FileDataStore store;

    public UserRepository(FileDataStore store) {
        this.store = store;
    }

    public Optional<User> findByEmail(String email) {
        String normalized = email.toLowerCase(Locale.ROOT).trim();
        return store.read(state -> state.users().stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(normalized))
                .findFirst());
    }

    public Optional<User> findById(String id) {
        return store.read(state -> state.users().stream()
                .filter(user -> user.getId().equals(id))
                .findFirst());
    }

    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    public User save(User user) {
        return store.write(state -> {
            state.users().removeIf(existing -> existing.getId().equals(user.getId()));
            state.users().add(user);
            return user;
        });
    }
}
