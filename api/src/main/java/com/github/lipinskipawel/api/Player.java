package com.github.lipinskipawel.api;

import java.util.Objects;

/**
 * This class is an API.
 * This class is a representation of connected player to the server.
 */
public final class Player {
    private final String url;

    Player(String url) {
        this.url = url;
    }

    public static Player fromUrl(final String url) {
        if (url != null && !url.isBlank()) {
            return new Player(url);
        }
        throw new RuntimeException("url must not be null or blank");
    }

    public String getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return Objects.equals(url, player.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }
}
