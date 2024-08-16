package com.github.lipinskipawel.api;

import java.util.Objects;

/**
 * This class is an API.
 * This class is a representation of connected player to the server.
 */
public final class Player {
    private final String username;

    Player(String username) {
        this.username = username;
    }

    public static Player fromUsername(final String username) {
        if (username != null && !username.isBlank()) {
            return new Player(username);
        }
        throw new RuntimeException("username must not be null or blank");
    }

    public String getUsername() {
        return username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return Objects.equals(username, player.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    @Override
    public String toString() {
        return "Player{" +
            "username='" + username + '\'' +
            '}';
    }
}
