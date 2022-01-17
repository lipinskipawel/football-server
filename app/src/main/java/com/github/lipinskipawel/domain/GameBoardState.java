package com.github.lipinskipawel.domain;

import com.github.lipinskipawel.board.engine.Board;
import com.github.lipinskipawel.board.engine.Boards;
import com.github.lipinskipawel.board.engine.Move;

/**
 * This class represents a game state.
 * It is a thin wrapper around the {@link Board} interface.
 */
final class GameBoardState {
    private final String first;
    private final String second;
    private String currentlyMovingPlayer;
    private Board<String> boardState;

    private GameBoardState(final String first, final String second) {
        this.first = first;
        this.second = second;
        this.currentlyMovingPlayer = first;
        this.boardState = Boards.immutableBoardWithCustomPlayer(first, second);
    }

    /**
     * This method allow to make a move on the game board. Move must be a legal move which is defined by {@link Board}
     * interface. Move also must be complete meaning that after making the move next player to move is the opposite one.
     * Only move that is possible to make by the current player is accepted.
     *
     * @param move   to be made on a game board
     * @param player that makes the move
     * @return true if the move was executed successfully or false otherwise
     */
    boolean makeMoveBy(final Move move, final String player) {
        if (!this.currentlyMovingPlayer.equals(player)) {
            return false;
        }
        final var afterMove = makeMove(move);
        if (this.boardState.getPlayer().equals(afterMove.getPlayer())) {
            return false;
        }
        this.boardState = afterMove;
        this.currentlyMovingPlayer = this.currentlyMovingPlayer.equals(first) ? second : first;
        return true;
    }

    private Board<String> makeMove(final Move move) {
        var refOfBoard = this.boardState;
        for (var singleMove : move.getMove()) {
            if (refOfBoard.isMoveAllowed(singleMove)) {
                refOfBoard = refOfBoard.executeMove(singleMove);
            } else {
                return this.boardState;
            }
        }
        return refOfBoard;
    }

    String currentPlayerToMove() {
        return this.boardState.getPlayer();
    }

    String first() {
        return this.first;
    }

    String second() {
        return this.second;
    }

    boolean isGameOver() {
        return this.boardState.isGameOver();
    }

    /**
     * This method return the username of the winner.
     * This method will {@link java.util.NoSuchElementException} if the game doesn't have the winner yet. Hence, calling
     * {@link GameBoardState#isGameOver()} method is advised.
     *
     * @return username of the winner
     */
    String getWinner() {
        return this.boardState.takeTheWinner().get();
    }


    static Builder aGameBoardState() {
        return new Builder();
    }

    static class Builder {
        private String first, second;

        public Builder() {
        }

        public Builder withFirstPlayer(final String firstPlayer) {
            this.first = firstPlayer;
            return this;
        }

        public Builder withSecondPlayer(final String secondPlayer) {
            this.second = secondPlayer;
            return this;
        }

        public GameBoardState build() {
            return new GameBoardState(first, second);
        }
    }
}
