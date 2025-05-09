package com.github.lipinskipawel.db;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import static com.github.lipinskipawel.db.UserState.CREATED;
import static java.util.Objects.hash;
import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public final class User {
    private final UUID id;
    private final String username;
    private final String token;
    private final UserState state;
    private final Instant createdDate;
    private final Instant updatedDate;

    private User(Builder builder) {
        this.id = requireNonNull(builder.id);
        this.username = requireLength(builder.username);
        this.token = requireNonNull(builder.token);
        this.state = requireNonNull(builder.state);
        this.createdDate = requireNonNull(builder.createdDate);
        this.updatedDate = requireNonNull(builder.updatedDate);
    }

    public UUID id() {
        return id;
    }

    public String username() {
        return username;
    }

    public String token() {
        return token;
    }

    public UserState state() {
        return state;
    }

    public Instant createdDate() {
        return createdDate;
    }

    public Instant updatedDate() {
        return updatedDate;
    }

    private String requireLength(String string) {
        final var str = requireNonNull(string);
        if (str.length() < 3 || str.length() > 16) {
            throw new IllegalArgumentException("Username length must be between 3-16, but was [%s]".formatted(str));
        }
        return str;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id)
            && Objects.equals(username, user.username)
            && Objects.equals(token, user.token)
            && Objects.equals(state, user.state)
            && Objects.equals(createdDate, user.createdDate)
            && Objects.equals(updatedDate, user.updatedDate);
    }

    @Override
    public int hashCode() {
        return hash(id, username, token, state, createdDate, updatedDate);
    }

    public static class Builder {
        private UUID id;
        private String username;
        private String token;
        private UserState state;
        private Instant createdDate;
        private Instant updatedDate;

        private Builder() {
        }

        public static Builder createdUser() {
            final var builder = new Builder();
            builder.id(randomUUID());
            builder.state(CREATED);
            return builder;
        }

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder state(UserState userState) {
            this.state = userState;
            return this;
        }

        public Builder createdDate(Instant createdDate) {
            this.createdDate = createdDate;
            return this;
        }

        public Builder updatedDate(Instant updatedDate) {
            this.updatedDate = updatedDate;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }
}
