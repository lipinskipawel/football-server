package com.github.lipinskipawel.server;

import com.github.lipinskipawel.api.move.AcceptMove;
import com.github.lipinskipawel.api.move.GameMove;
import com.github.lipinskipawel.api.move.RejectMove;
import com.github.lipinskipawel.client.FootballClientCreator;
import com.github.lipinskipawel.client.SimpleWebSocketClient;
import com.google.gson.Gson;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

final class GamePlayIT implements WithAssertions {
    private static final ExecutorService pool = Executors.newFixedThreadPool(1);
    private static final int PORT = 8092;
    private static final String SERVER_LOBBY = "ws://localhost:%d/lobby".formatted(PORT);
    private static final FootballServer server = new FootballServer(new InetSocketAddress("localhost", PORT));
    private static final Gson parser = new Gson();
    private static final String ACCEPT_RESPONSE = parser.toJson(new AcceptMove());

    @BeforeAll
    static void setUp() {
        pool.submit(server);
    }

    @AfterAll
    static void cleanUp() throws InterruptedException {
        server.stop(1000);
        pool.shutdown();
    }

    @Test
    void shouldAllowToSendMoveToAnotherPlayerWhenPlayingTogether() throws InterruptedException {
        final var onAcceptMessage = new CountDownLatch(1);
        final var onMessage = new CountDownLatch(1);
        final var pairedClients = FootballClientCreator.getPairedClients(SERVER_LOBBY);
        pairedClients[0].latchOnMessage(onAcceptMessage);
        pairedClients[1].latchOnMessage(onMessage);

        final var move = GameMove.from(List.of("N")).get();
        final var message = parser.toJson(move);
        pairedClients[0].send(message);

        final var gotMessage = onMessage.await(1, TimeUnit.SECONDS);
        final var gotAccept = onAcceptMessage.await(1, TimeUnit.SECONDS);
        pairedClients[0].closeBlocking();
        pairedClients[1].closeBlocking();
        assertThat(gotMessage).isTrue();
        assertThat(pairedClients[1])
                .extracting(SimpleWebSocketClient::getMessages)
                .asList()
                .hasSize(1)
                .containsExactly(message);
        assertThat(gotAccept).isTrue();
        assertThat(pairedClients[0])
                .extracting(SimpleWebSocketClient::getMessages)
                .asList()
                .hasSize(1)
                .containsExactly(ACCEPT_RESPONSE);
    }

    @Test
    void shouldAllowToExchangeMovesBetweenPlayersWhenPlayingTogether() throws InterruptedException {
        var onMessage = new CountDownLatch(1);
        final var pairedClients = FootballClientCreator.getPairedClients(SERVER_LOBBY);
        pairedClients[1].latchOnMessage(onMessage);

        final var moveToSecond = GameMove.from(List.of("N")).get();
        final var messageForSecond = parser.toJson(moveToSecond);
        pairedClients[0].send(messageForSecond);
        var gotMessage = onMessage.await(1, TimeUnit.SECONDS);
        assertThat(gotMessage).isTrue();
        final var moveToFirst = GameMove.from(List.of("E")).get();
        final var messageForFirst = parser.toJson(moveToFirst);

        onMessage = new CountDownLatch(1);
        pairedClients[0].latchOnMessage(onMessage);
        pairedClients[1].send(messageForFirst);
        gotMessage = onMessage.await(1, TimeUnit.SECONDS);
        assertThat(gotMessage).isTrue();

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
        var onMessage = new CountDownLatch(1);
        final var pairedClients = FootballClientCreator.getPairedClients(SERVER_LOBBY);
        pairedClients[1].latchOnMessage(onMessage);

        final var moveToSecond = GameMove.from(List.of("N")).get();
        final var messageForSecond = parser.toJson(moveToSecond);
        pairedClients[0].send(messageForSecond);
        var gotMessage = onMessage.await(1, TimeUnit.SECONDS);
        assertThat(gotMessage).isTrue();

        onMessage = new CountDownLatch(1);
        final var moveToSecondAgain = GameMove.from(List.of("N")).get();
        final var messageForSecondAgain = parser.toJson(moveToSecondAgain);
        pairedClients[0].send(messageForSecondAgain);
        gotMessage = onMessage.await(1, TimeUnit.SECONDS);
        assertThat(gotMessage).isFalse();

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
