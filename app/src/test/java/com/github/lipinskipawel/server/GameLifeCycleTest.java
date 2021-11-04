package com.github.lipinskipawel.server;

import com.github.lipinskipawel.api.move.AcceptMove;
import com.github.lipinskipawel.api.move.GameMove;
import com.github.lipinskipawel.api.move.RejectMove;
import com.github.lipinskipawel.domain.GameLifeCycle;
import com.github.lipinskipawel.mocks.TestConnectedClient;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

class GameLifeCycleTest implements WithAssertions {
    private GameLifeCycle game;

    @BeforeEach
    public void setUp() {
        game = GameLifeCycle.of(Objects::toString);
    }

    @Test
    void shouldAllowToMakeAMove() {
        final var firstClient = new TestConnectedClient("/one");
        final var secondClient = new TestConnectedClient("/one");
        game.accept(firstClient);
        game.accept(secondClient);

        final var gameMove = GameMove.from(List.of("N")).get();
        game.makeMove(gameMove, firstClient);

        assertThat(secondClient)
                .extracting(TestConnectedClient::getMessages)
                .asList()
                .hasSize(1)
                .containsExactly(gameMove.toString());
        assertThat(firstClient)
                .extracting(TestConnectedClient::getMessages)
                .asList()
                .hasSize(1)
                .containsExactly(new AcceptMove().toString());
    }

    @Test
    void shouldNotAllowToMakeAMoveTwiceByTheSamePlayer() {
        final var firstClient = new TestConnectedClient("/one");
        final var secondClient = new TestConnectedClient("/one");
        game.accept(firstClient);
        game.accept(secondClient);

        final var gameMove = GameMove.from(List.of("N")).get();
        game.makeMove(gameMove, firstClient);
        game.makeMove(gameMove, firstClient);

        assertThat(secondClient)
                .extracting(TestConnectedClient::getMessages)
                .asList()
                .hasSize(1)
                .containsExactly(gameMove.toString());
        assertThat(firstClient)
                .extracting(TestConnectedClient::getMessages)
                .asList()
                .hasSize(2)
                .containsExactly(new AcceptMove().toString(), new RejectMove(gameMove).toString());
    }
}