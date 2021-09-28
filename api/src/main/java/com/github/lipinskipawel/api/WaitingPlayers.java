package com.github.lipinskipawel.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class is an API.
 * This class is a representation of all the player waiting to start the game.
 * Each player is already connected to the server.
 */
public final class WaitingPlayers {
    private final List<Player> players;

    WaitingPlayers(List<Player> players) {
        this.players = players;
    }

    public static WaitingPlayers fromPlayers(final List<Player> players) {
        if (players != null) {
            return new WaitingPlayers(new ArrayList<>(players));
        }
        throw new RuntimeException("players must not be null");
    }

    public List<Player> players() {
        return new ArrayList<>(players);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WaitingPlayers that = (WaitingPlayers) o;
        return Objects.equals(players, that.players);
    }

    @Override
    public int hashCode() {
        return Objects.hash(players);
    }
}
