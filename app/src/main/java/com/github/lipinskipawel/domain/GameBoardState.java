package com.github.lipinskipawel.domain;

import com.github.lipinskipawel.board.engine.Board;
import com.github.lipinskipawel.board.engine.Boards;
import com.github.lipinskipawel.board.engine.Move;
import com.github.lipinskipawel.board.engine.Player;
import com.github.lipinskipawel.user.ConnectedClient;

/**
 * This class represents a game state.
 * It is a thin wrapper around the {@link Board} interface.
 */
final class GameBoardState {
    private final ConnectedClient first;
    private final ConnectedClient second;
    private ConnectedClient currentlyMovingPlayer;
    private Board boardState;

    private GameBoardState(final ConnectedClient first, final ConnectedClient second) {
        this.first = first;
        this.second = second;
        this.currentlyMovingPlayer = first;
        this.boardState = Boards.immutableBoard();
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
    boolean makeMoveBy(final Move move, final ConnectedClient player) {
        if (!this.currentlyMovingPlayer.equals(player)) {
            return false;
        }
        final var afterMove = this.boardState.executeMove(move);
        if (this.boardState.moveHistory().equals(afterMove.moveHistory())) {
            return false;
        }
        this.boardState = afterMove;
        this.currentlyMovingPlayer = this.currentlyMovingPlayer == first ? second : first;
        return true;
    }

    Player currentPlayerToMove() {
        return this.boardState.getPlayer();
    }


    static Builder aGameBoardState() {
        return new Builder();
    }

    static class Builder {
        private ConnectedClient first, second;

        public Builder() {
        }

        public Builder withFirstPlayer(final ConnectedClient firstPlayer) {
            this.first = firstPlayer;
            return this;
        }

        public Builder withSecondPlayer(final ConnectedClient secondPlayer) {
            this.second = secondPlayer;
            return this;
        }

        public GameBoardState build() {
            return new GameBoardState(first, second);
        }
    }
}
