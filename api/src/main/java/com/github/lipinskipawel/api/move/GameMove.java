package com.github.lipinskipawel.api.move;

import com.github.lipinskipawel.api.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * This class is an API.
 * This class is a representation of the turn made by a {@link Player} in a game. Currently, this class contains a list
 * of moves which server can process. Each {@link GameMove} is created from one or more small moves. Small move is when
 * the player makes one move with the possibility of making another legal move. In this case a {@link GameMove} is a
 * collection of small moves. Often the player is allowed (by the game rules) to make only one small move then
 * {@link GameMove} is created from this one small move.
 *
 * <p>Each move is encoded as a collection of {@link String}'s. There are only 8 allowed string values that can be used
 * for creating the {@link GameMove}.<bold>Those values are N, NE, E, SE, S, SW, W, NW</bold>. Each of them are
 * representing cardinal direction. Meaning of those values stays the same for the entire game. The current position of
 * the ball in the game is the reference point for appointing the correct direction. The North Pole is always at the
 * first player goal area. The South Pole is always at the second player goal area.
 */
public final class GameMove {
    private final List<String> move;

    private GameMove(List<String> move) {
        this.move = move;
    }

    public static Optional<GameMove> from(final List<String> move) {
        Objects.requireNonNull(move);
        if (move.isEmpty()) {
            return Optional.empty();
        }
        final var isIncorrectDirection = move
                .stream()
                .anyMatch(GameMove::isIncorrectDirection);
        if (isIncorrectDirection) {
            return Optional.empty();
        }
        return Optional.of(new GameMove(move));
    }

    private static boolean isIncorrectDirection(final String direction) {
        return !(direction.equals("N") ||
                direction.equals("NE") ||
                direction.equals("E") ||
                direction.equals("SE") ||
                direction.equals("S") ||
                direction.equals("SW") ||
                direction.equals("W") ||
                direction.equals("NW"));
    }

    public List<String> getMove() {
        return new ArrayList<>(move);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameMove gameState = (GameMove) o;
        return Objects.equals(move, gameState.move);
    }

    @Override
    public int hashCode() {
        return Objects.hash(move);
    }

    @Override
    public String toString() {
        return "GameState{" +
                "move=" + move +
                '}';
    }
}
