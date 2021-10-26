package com.github.lipinskipawel.server;

import com.github.lipinskipawel.api.move.GameMove;
import com.github.lipinskipawel.mocks.TestConnectedClient;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

class GameLifeCycleTest implements WithAssertions {

    @Test
    void shouldAllowToMakeAMove() {
        final var game = new GameLifeCycle(new DualConnection(), Objects::toString);
        final var firstClient = new TestConnectedClient("/one");
        final var secondClient = new TestConnectedClient("/one");
        game.accept(firstClient);
        game.accept(secondClient);

        game.makeMove(GameMove.from(List.of("N")).get(), firstClient);

        assertThat(secondClient)
                .extracting(TestConnectedClient::getMessages)
                .asList()
                .hasSize(1);
    }

    @Test
    void shouldNotAllowToMakeAMoveTwiceByTheSamePlayer() {
        final var game = new GameLifeCycle(new DualConnection(), Objects::toString);
        final var firstClient = new TestConnectedClient("/one");
        final var secondClient = new TestConnectedClient("/one");
        game.accept(firstClient);
        game.accept(secondClient);

        game.makeMove(GameMove.from(List.of("N")).get(), firstClient);
        game.makeMove(GameMove.from(List.of("N")).get(), firstClient);

        assertThat(secondClient)
                .extracting(TestConnectedClient::getMessages)
                .asList()
                .hasSize(1);
    }
}