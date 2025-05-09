package com.github.lipinskipawel.model;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public final class Token {
    private final String token;

    private Token(String token) {
        this.token = token;
    }

    public static Token token(String token) {
        final var str = requireNonNull(token);
        if (str.length() < 3 || str.length() > 16) {
            throw new IllegalArgumentException("Token length must be between 3-16, but was [%s]".formatted(str.length()));
        }
        return new Token(str);
    }

    public String token() {
        return token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final var that = (Token) o;
        return Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(token);
    }
}
