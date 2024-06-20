package com.github.lipinskipawel.api.game;

import com.github.lipinskipawel.api.Player;

import java.util.Objects;

/**
 * This class is an API.
 * This class represent an object send by the server whenever the game has been ended.
 */
public final class GameEnd {
    private final Player winner;

    public GameEnd(Player winner) {
        this.winner = winner;
    }

    public Player getWinner() {
        return winner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameEnd gameEnd = (GameEnd) o;
        return Objects.equals(winner, gameEnd.winner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(winner);
    }

    @Override
    public String toString() {
        return "GameEnd{" +
                "winner=" + winner +
                '}';
    }
}
