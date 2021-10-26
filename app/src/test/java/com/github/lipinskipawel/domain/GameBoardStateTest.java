package com.github.lipinskipawel.domain;

import com.github.lipinskipawel.board.engine.Direction;
import com.github.lipinskipawel.board.engine.Move;
import com.github.lipinskipawel.board.engine.Player;
import com.github.lipinskipawel.mocks.TestConnectedClient;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

class GameBoardStateTest implements WithAssertions {

    @Nested
    class PlayersMakeMovesOneAfterAnother {
        @Test
        void shouldAllowToMakeAMove() {
            final var firstPlayer = new TestConnectedClient("/game/one");
            final var secondPlayer = new TestConnectedClient("/game/one");
            final var state = new GameBoardState.Builder()
                    .withFirstPlayer(firstPlayer)
                    .withSecondPlayer(secondPlayer)
                    .build();

            final var isMade = state.makeMoveBy(new Move(List.of(Direction.E)), firstPlayer);

            assertThat(isMade).isTrue();
            assertThat(state)
                    .extracting(GameBoardState::currentPlayerToMove)
                    .isEqualTo(Player.SECOND);
        }

        @Test
        void shouldAllowToMakeAMoveForSecondPlayer() {
            final var firstPlayer = new TestConnectedClient("/game/one");
            final var secondPlayer = new TestConnectedClient("/game/one");
            final var state = new GameBoardState.Builder()
                    .withFirstPlayer(firstPlayer)
                    .withSecondPlayer(secondPlayer)
                    .build();

            state.makeMoveBy(new Move(List.of(Direction.E)), firstPlayer);
            final var isMade = state.makeMoveBy(new Move(List.of(Direction.S)), secondPlayer);

            assertThat(isMade).isTrue();
            assertThat(state)
                    .extracting(GameBoardState::currentPlayerToMove)
                    .isEqualTo(Player.FIRST);
        }

        @Test
        void shouldNotAllowToMakeAMoveWhenMoveDoesNotChangeTurn() {
            final var firstPlayer = new TestConnectedClient("/game/one");
            final var secondPlayer = new TestConnectedClient("/game/one");
            final var state = new GameBoardState.Builder()
                    .withFirstPlayer(firstPlayer)
                    .withSecondPlayer(secondPlayer)
                    .build();
            state.makeMoveBy(new Move(List.of(Direction.N)), firstPlayer);
            state.makeMoveBy(new Move(List.of(Direction.W)), secondPlayer);

            final var isMade = state.makeMoveBy(new Move(List.of(Direction.SE)), firstPlayer);

            assertThat(isMade).isFalse();
            assertThat(state)
                    .extracting(GameBoardState::currentPlayerToMove)
                    .isEqualTo(Player.FIRST);
        }
    }
}