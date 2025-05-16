package com.github.lipinskipawel.model;

import java.util.Objects;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

public final class Username {
    private static final Pattern NOT_STARTS_WITH_LETTER = Pattern.compile("^[^a-zA-Z]"); // \\W -> [^a-zA-Z0-9_]
    private static final Pattern NOT_ALLOWED_CHARACTERS = Pattern.compile("[^a-zA-Z0-9_]");

    private final String username;

    private Username(String username) {
        this.username = username;
    }

    public static Username username(String username) {
        final var str = requireNonNull(username);
        if (str.length() < 3 || str.length() > 16) {
            throw new IllegalArgumentException("Username length must be between 3-16, but was [%s]".formatted(str.length()));
        }
        if (NOT_STARTS_WITH_LETTER.matcher(str).find()) {
            throw new IllegalArgumentException("Username have to start with a letter [%s] is not allowed".formatted(str));
        }
        if (NOT_ALLOWED_CHARACTERS.matcher(str).find()) {
            throw new IllegalArgumentException("Username can not have illegal characters [%s]".formatted(str));
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
