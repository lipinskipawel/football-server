package com.github.lipinskipawel.db;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;

public final class User {
    private final UUID id;
    private final String username;
    private final String token;
    private final Instant created;
    private final Optional<Instant> terminated;

    private User(Builder builder) {
        this.id = requireNonNull(builder.id);
        this.username = requireLength(builder.username);
        this.token = requireNonNull(builder.token);
        this.created = requireNonNull(builder.created);
        this.terminated = requireNonNull(builder.terminated);
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

    public Instant created() {
        return created;
    }

    public Optional<Instant> terminated() {
        return terminated;
    }

    private String requireLength(String string) {
        final var str = requireNonNull(string);
        if (str.length() < 3 || str.length() > 15) {
            throw new IllegalArgumentException("Username length must be between 3-15, but was [%s]".formatted(str));
        }
        return str;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(username, user.username) && Objects.equals(token, user.token) && Objects.equals(created, user.created) && Objects.equals(terminated, user.terminated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, token, created, terminated);
    }

    public static class Builder {
        private UUID id;
        private String username;
        private String token;
        private Instant created;
        private Optional<Instant> terminated = empty();

        private Builder() {
        }

        public static Builder userBuilder() {
            return new Builder();
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

        public Builder created(Instant created) {
            this.created = created;
            return this;
        }

        public Builder terminated(Optional<Instant> terminated) {
            this.terminated = terminated;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }
}
