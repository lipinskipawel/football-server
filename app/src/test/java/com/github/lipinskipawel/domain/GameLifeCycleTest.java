package com.github.lipinskipawel.domain;

import com.github.lipinskipawel.api.Player;
import com.github.lipinskipawel.api.game.GameEnd;
import com.github.lipinskipawel.api.move.AcceptMove;
import com.github.lipinskipawel.api.move.GameMove;
import com.github.lipinskipawel.api.move.RejectMove;
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
        game = GameLifeCycle.of(Objects::toString, "first", "second");
    }

    @Test
    void shouldAllowToMakeAMove() {
        final var firstClient = new TestConnectedClient("/one", "first");
        final var secondClient = new TestConnectedClient("/one", "second");
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
        final var firstClient = new TestConnectedClient("/one", "first");
        final var secondClient = new TestConnectedClient("/one", "second");
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

    @Test
    void shouldNotAllowToMakeIllegalMove() {
        final var firstClient = new TestConnectedClient("/one", "first");
        final var secondClient = new TestConnectedClient("/one", "second");
        game.accept(firstClient);
        game.accept(secondClient);
        final var gameMove = GameMove.from(List.of("N")).get();
        game.makeMove(gameMove, firstClient);

        final var illegalMove = GameMove.from(List.of("S")).get();
        game.makeMove(illegalMove, secondClient);

        assertThat(secondClient)
                .extracting(TestConnectedClient::getMessages)
                .asList()
                .hasSize(2)
                .containsExactly(gameMove.toString(), new RejectMove(illegalMove).toString());
        assertThat(firstClient)
                .extracting(TestConnectedClient::getMessages)
                .asList()
                .hasSize(1)
                .containsExactly(new AcceptMove().toString());
    }

    @Test
    void shouldDetectWhenGameIsEnded() {
        final var firstClient = new TestConnectedClient("/one", "first");
        final var secondClient = new TestConnectedClient("/one", "second");
        final var expectedWinner = new GameEnd(Player.fromUsername(firstClient.getUsername()));
        game.accept(firstClient);
        game.accept(secondClient);

        final var northMove = GameMove.from(List.of("N")).get();
        game.makeMove(northMove, firstClient);
        game.makeMove(northMove, secondClient);
        game.makeMove(northMove, firstClient);
        game.makeMove(northMove, secondClient);
        game.makeMove(northMove, firstClient);
        game.makeMove(northMove, secondClient);

        final var msgWithoutAccept = List.of(
                northMove.toString(), northMove.toString(), northMove.toString(), expectedWinner.toString()
        );
        assertThat(secondClient)
                .extracting(TestConnectedClient::getMessages)
                .asList()
                .map(it -> (String) it)
                .filteredOn(it -> !it.contains("AcceptMove"))
                .hasSize(4)
                .containsExactlyElementsOf(msgWithoutAccept);
        assertThat(firstClient)
                .extracting(TestConnectedClient::getMessages)
                .asList()
                .map(it -> (String) it)
                .filteredOn(it -> !it.contains("AcceptMove"))
                .hasSize(4)
                .containsExactlyElementsOf(msgWithoutAccept);
    }
}