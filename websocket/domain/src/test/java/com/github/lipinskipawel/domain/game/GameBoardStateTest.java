package com.github.lipinskipawel.domain.game;

import com.github.lipinskipawel.board.engine.Direction;
import com.github.lipinskipawel.board.engine.Move;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

class GameBoardStateTest implements WithAssertions {

    @Nested
    class PlayersMakeMovesOneAfterAnother {
        @Test
        void shouldAllowToMakeAMove() {
            final var state = new GameBoardState.Builder()
                .withFirstPlayer("firstPlayer")
                .withSecondPlayer("secondPlayer")
                .build();

            final var isMade = state.makeMoveBy(new Move(List.of(Direction.E)), "firstPlayer");

            assertThat(isMade).isTrue();
            assertThat(state)
                .extracting(GameBoardState::currentPlayerToMove)
                .isEqualTo("secondPlayer");
        }

        @Test
        void shouldAllowToMakeAMoveForSecondPlayer() {
            final var state = new GameBoardState.Builder()
                .withFirstPlayer("firstPlayer")
                .withSecondPlayer("secondPlayer")
                .build();

            state.makeMoveBy(new Move(List.of(Direction.E)), "firstPlayer");
            final var isMade = state.makeMoveBy(new Move(List.of(Direction.S)), "secondPlayer");

            assertThat(isMade).isTrue();
            assertThat(state)
                .extracting(GameBoardState::currentPlayerToMove)
                .isEqualTo("firstPlayer");
        }

        @Test
        void shouldNotAllowToMakeAMoveWhenMoveDoesNotChangeTurn() {
            final var state = new GameBoardState.Builder()
                .withFirstPlayer("firstPlayer")
                .withSecondPlayer("secondPlayer")
                .build();
            state.makeMoveBy(new Move(List.of(Direction.N)), "firstPlayer");
            state.makeMoveBy(new Move(List.of(Direction.W)), "secondPlayer");

            final var isMade = state.makeMoveBy(new Move(List.of(Direction.SE)), "firstPlayer");

            assertThat(isMade).isFalse();
            assertThat(state)
                .extracting(GameBoardState::currentPlayerToMove)
                .isEqualTo("firstPlayer");
        }
    }
}
