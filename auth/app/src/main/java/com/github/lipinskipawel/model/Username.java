package com.github.lipinskipawel.model;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public final class Username {
    private final String username;

    private Username(String username) {
        this.username = username;
    }

    public static Username username(String username) {
        final var str = requireNonNull(username);
        if (str.length() < 3 || str.length() > 16) {
            throw new IllegalArgumentException("Username length must be between 3-16, but was [%s]".formatted(str.length()));
        }
        return new Username(str);
    }

    public String username() {
        return username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final var that = (Username) o;
        return Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(username);
    }
}
