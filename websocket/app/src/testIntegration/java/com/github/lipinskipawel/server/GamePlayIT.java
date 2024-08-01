package com.github.lipinskipawel.server;

import com.github.lipinskipawel.api.move.AcceptMove;
import com.github.lipinskipawel.api.move.GameMove;
import com.github.lipinskipawel.api.move.RejectMove;
import com.github.lipinskipawel.SimpleWebSocketClient;
import com.github.lipinskipawel.IntegrationSpec;
import com.google.gson.Gson;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.util.List;

final class GamePlayIT extends IntegrationSpec implements WithAssertions {
    private static final Gson parser = new Gson();
    private static final String ACCEPT_RESPONSE = parser.toJson(new AcceptMove());
    static final String SERVER_LOBBY = "ws://localhost:%d/ws/lobby".formatted(PORT);

    @Test
    void shouldAllowToSendMoveToAnotherPlayerWhenPlayingTogether() throws InterruptedException {
        final var pairedClients = getPairedClients(SERVER_LOBBY);

        final var move = GameMove.from(List.of("N")).get();
        final var message = parser.toJson(move);
        pairedClients[0].send(message);
        waitFor(() -> pairedClients[1].getMessages().size() == 1);
        waitFor(() -> pairedClients[0].getMessages().size() == 1);
        pairedClients[0].closeBlocking();
        pairedClients[1].closeBlocking();

        assertThat(pairedClients[1])
                .extracting(SimpleWebSocketClient::getMessages)
                .asList()
                .hasSize(1)
                .containsExactly(message);
        assertThat(pairedClients[0])
                .extracting(SimpleWebSocketClient::getMessages)
                .asList()
                .hasSize(1)
                .containsExactly(ACCEPT_RESPONSE);
    }

    @Test
    void shouldAllowToExchangeMovesBetweenPlayersWhenPlayingTogether() throws InterruptedException {
        final var pairedClients = getPairedClients(SERVER_LOBBY);

        final var moveToSecond = GameMove.from(List.of("N")).get();
        final var messageForSecond = parser.toJson(moveToSecond);
        pairedClients[0].send(messageForSecond);
        waitFor(() -> pairedClients[1].getMessages().size() == 1);
        final var moveToFirst = GameMove.from(List.of("E")).get();
        final var messageForFirst = parser.toJson(moveToFirst);

        pairedClients[1].send(messageForFirst);
        waitFor(() -> pairedClients[0].getMessages().size() == 2);

        pairedClients[0].closeBlocking();
        pairedClients[1].closeBlocking();
        assertThat(pairedClients[1])
                .extracting(SimpleWebSocketClient::getMessages)
                .asList()
                .hasSize(2)
                .containsExactly(messageForSecond, ACCEPT_RESPONSE);
        assertThat(pairedClients[0])
                .extracting(SimpleWebSocketClient::getMessages)
                .asList()
                .hasSize(2)
                .containsExactly(ACCEPT_RESPONSE, messageForFirst);
    }

    @Test
    void shouldNotAllowToMoveTwiceByTheSamePlayer() throws InterruptedException {
        final var pairedClients = getPairedClients(SERVER_LOBBY);

        final var moveToSecond = GameMove.from(List.of("N")).get();
        final var messageForSecond = parser.toJson(moveToSecond);
        pairedClients[0].send(messageForSecond);
        waitFor(() -> pairedClients[0].getMessages().size() == 1);

        final var moveToSecondAgain = GameMove.from(List.of("N")).get();
        final var messageForSecondAgain = parser.toJson(moveToSecondAgain);
        pairedClients[0].send(messageForSecondAgain);
        waitFor(() -> pairedClients[0].getMessages().size() == 2);

        pairedClients[0].closeBlocking();
        pairedClients[1].closeBlocking();
        assertThat(pairedClients[1])
                .extracting(SimpleWebSocketClient::getMessages)
                .asList()
                .hasSize(1)
                .containsExactly(messageForSecond);
        assertThat(pairedClients[0])
                .extracting(SimpleWebSocketClient::getMessages)
                .asList()
                .hasSize(2)
                .containsExactly(ACCEPT_RESPONSE, parser.toJson(new RejectMove(moveToSecondAgain)));
    }
}
