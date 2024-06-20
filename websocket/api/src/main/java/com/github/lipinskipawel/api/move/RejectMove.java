package com.github.lipinskipawel.api.move;

import java.util.Objects;

/**
 * This class is an API.
 * Object of this class is sent as a reply on {@link GameMove} which has been not accepted by the server.
 */
public final class RejectMove {
    private final GameMove gameMove;

    public RejectMove(GameMove gameMove) {
        this.gameMove = gameMove;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RejectMove that = (RejectMove) o;
        return Objects.equals(gameMove, that.gameMove);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameMove);
    }

    @Override
    public String toString() {
        return "RejectMove{" +
                "gameMove=" + gameMove +
                '}';
    }
}
