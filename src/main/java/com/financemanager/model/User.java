package com.financemanager.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public final class User implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    private final String id;
    private String fullName;
    private String email;
    private String passwordHash;
    private final LocalDateTime createdAt;

    public User(String fullName, String email, String passwordHash) {
        this(UUID.randomUUID().toString(), fullName, email, passwordHash, LocalDateTime.now());
    }

    public User(String id, String fullName, String email, String passwordHash, LocalDateTime createdAt) {
        this.id = Objects.requireNonNull(id);
        this.fullName = Objects.requireNonNull(fullName);
        this.email = Objects.requireNonNull(email);
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public String getId() { return id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object other) {
        return other instanceof User user && id.equals(user.id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }
}
