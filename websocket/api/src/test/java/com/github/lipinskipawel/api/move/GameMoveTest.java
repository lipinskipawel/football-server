package com.github.lipinskipawel.api.move;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

final class GameMoveTest implements WithAssertions {

    @Test
    void shouldAllowToCreateAGameMoveFromOneDirection() {
        final var gameMove = GameMove.from(List.of("N")).get();

        assertThat(gameMove)
            .isNotNull()
            .extracting(GameMove::getMove)
            .asList()
            .hasSize(1)
            .containsExactly("N");
    }

    @Test
    void shouldAllowToCreateAGameMoveFromTwoDirection() {
        final var gameMove = GameMove.from(List.of("N", "SE")).get();

        assertThat(gameMove)
            .isNotNull()
            .extracting(GameMove::getMove)
            .asList()
            .hasSize(2)
            .containsExactly("N", "SE");
    }

    @Test
    void shouldNotAllowToCreateAGameMoveFromIllegalDirection() {
        final var gameMove = GameMove.from(List.of("illegal"));

        Assertions.assertThrows(NoSuchElementException.class, gameMove::get);
    }
}
