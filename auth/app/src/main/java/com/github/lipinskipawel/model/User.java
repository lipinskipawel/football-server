package com.github.lipinskipawel.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import static com.github.lipinskipawel.model.UserState.CREATED;
import static java.util.Objects.hash;
import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public final class User {
    private final UUID id;
    private final Username username;
    private final Token token;
    private final UserState state;
    private final Instant createdDate;
    private final Instant updatedDate;

    private User(Builder builder) {
        this.id = requireNonNull(builder.id);
        this.username = builder.username;
        this.token = builder.token;
        this.state = requireNonNull(builder.state);

        this.createdDate = requireNonNull(builder.createdDate);
        this.updatedDate = requireNonNull(builder.updatedDate);
        validateTime(createdDate, updatedDate);
    }

    private void validateTime(Instant createdDate, Instant updatedDate) {
        if (createdDate.equals(updatedDate)) {
            return;
        }
        check(createdDate.isBefore(updatedDate), "Created date [%s] can not be after updated date [%s]".formatted(createdDate, updatedDate));
    }

    private void check(boolean condition, String errorMessage) {
        if (!condition) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    public UUID id() {
        return id;
    }

    public Username username() {
        return username;
    }

    public Token token() {
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
        private Username username;
        private Token token;
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

        public Builder username(Username username) {
            this.username = username;
            return this;
        }

        public Builder token(Token token) {
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
