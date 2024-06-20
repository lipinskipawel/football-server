package com.github.lipinskipawel.api;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * This class is an API.
 * This class is a representation of body request to start the game with the provided {@link Player} details.
 */
public final class RequestToPlay {
    private final Player opponent;

    RequestToPlay(Player opponent) {
        this.opponent = opponent;
    }

    public static RequestToPlay with(final Player opponent) {
        requireNonNull(opponent);
        return new RequestToPlay(opponent);
    }

    public Player getOpponent() {
        return opponent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestToPlay that = (RequestToPlay) o;
        return Objects.equals(opponent, that.opponent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(opponent);
    }

    @Override
    public String toString() {
        return "RequestToPlay{" +
                "opponent=" + opponent +
                '}';
    }
}
