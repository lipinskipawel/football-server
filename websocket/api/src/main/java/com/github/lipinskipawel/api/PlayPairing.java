package com.github.lipinskipawel.api;

import java.util.Objects;

/**
 * This class is an API.
 * This class is a representation of body request which will be sent by the server to connected clients.
 * Whenever connected client received this message it means:
 * - server will close current connection
 * - client must redirect to the given redirectEndpoint in order to play with the opponent
 */
public final class PlayPairing {
    private final String redirectEndpoint;
    private final Player first;
    private final Player second;

    private PlayPairing(String redirectEndpoint, Player first, Player second) {
        this.redirectEndpoint = redirectEndpoint;
        this.first = first;
        this.second = second;
    }

    public String getRedirectEndpoint() {
        return redirectEndpoint;
    }

    public Player getFirst() {
        return first;
    }

    public Player getSecond() {
        return second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayPairing that = (PlayPairing) o;
        return Objects.equals(redirectEndpoint, that.redirectEndpoint) && Objects.equals(first, that.first) && Objects.equals(second, that.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(redirectEndpoint, first, second);
    }

    @Override
    public String toString() {
        return "PlayPairing{" +
            "redirectEndpoint='" + redirectEndpoint + '\'' +
            ", first=" + first +
            ", second=" + second +
            '}';
    }

    public static Builder aPlayPairing() {
        return new Builder();
    }

    public static class Builder {
        private String redirectEndpoint;
        private Player first;
        private Player second;

        private Builder() {
        }

        public Builder withRedirectEndpoint(String endpoint) {
            this.redirectEndpoint = endpoint;
            return this;
        }

        public Builder withFirst(Player first) {
            this.first = first;
            return this;
        }

        public Builder withSecond(Player second) {
            this.second = second;
            return this;
        }

        public PlayPairing build() {
            Objects.requireNonNull(redirectEndpoint);
            Objects.requireNonNull(first);
            Objects.requireNonNull(second);
            return new PlayPairing(redirectEndpoint, first, second);
        }
    }
}
