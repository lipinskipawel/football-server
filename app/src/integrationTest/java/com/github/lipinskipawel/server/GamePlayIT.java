package com.github.lipinskipawel.server;

import com.github.lipinskipawel.api.move.AcceptMove;
import com.github.lipinskipawel.api.move.GameMove;
import com.github.lipinskipawel.api.move.RejectMove;
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

import static com.github.lipinskipawel.client.SimpleWebSocketClient.createClient;

final class GamePlayIT implements WithAssertions {
    private static final ExecutorService pool = Executors.newFixedThreadPool(1);
    private static final int PORT = 8092;
    private static final String SERVER_URI = "ws://localhost:%d".formatted(PORT);
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
        final var firstClient = createClient(SERVER_URI.concat("/game/one"), onAcceptMessage);
        final var secondClient = createClient(SERVER_URI.concat("/game/one"), onMessage);
        firstClient.connectBlocking();
        secondClient.connectBlocking();

        final var move = GameMove.from(List.of("N")).get();
        final var message = parser.toJson(move);
        firstClient.send(message);

        final var gotMessage = onMessage.await(1, TimeUnit.SECONDS);
        final var gotAccept = onAcceptMessage.await(1, TimeUnit.SECONDS);
        firstClient.closeBlocking();
        secondClient.closeBlocking();
        assertThat(gotMessage).isTrue();
        assertThat(secondClient)
                .extracting(SimpleWebSocketClient::getMessages)
                .asList()
                .hasSize(1)
                .containsExactly(message);
        assertThat(gotAccept).isTrue();
        assertThat(firstClient)
                .extracting(SimpleWebSocketClient::getMessages)
                .asList()
                .hasSize(1)
                .containsExactly(ACCEPT_RESPONSE);
    }

    @Test
    void shouldAllowToExchangeMovesBetweenPlayersWhenPlayingTogether() throws InterruptedException {
        var onMessage = new CountDownLatch(1);
        final var firstClient = createClient(SERVER_URI.concat("/game/two"));
        final var secondClient = createClient(SERVER_URI.concat("/game/two"), onMessage);
        firstClient.connectBlocking();
        secondClient.connectBlocking();

        final var moveToSecond = GameMove.from(List.of("N")).get();
        final var messageForSecond = parser.toJson(moveToSecond);
        firstClient.send(messageForSecond);
        var gotMessage = onMessage.await(1, TimeUnit.SECONDS);
        assertThat(gotMessage).isTrue();
        final var moveToFirst = GameMove.from(List.of("E")).get();
        final var messageForFirst = parser.toJson(moveToFirst);

        onMessage = new CountDownLatch(1);
        firstClient.latchOnMessage(onMessage);
        secondClient.send(messageForFirst);
        gotMessage = onMessage.await(1, TimeUnit.SECONDS);
        assertThat(gotMessage).isTrue();

        firstClient.closeBlocking();
        secondClient.closeBlocking();
        assertThat(secondClient)
                .extracting(SimpleWebSocketClient::getMessages)
                .asList()
                .hasSize(2)
                .containsExactly(messageForSecond, ACCEPT_RESPONSE);
        assertThat(firstClient)
                .extracting(SimpleWebSocketClient::getMessages)
                .asList()
                .hasSize(2)
                .containsExactly(ACCEPT_RESPONSE, messageForFirst);
    }

    @Test
    void shouldNotAllowToMoveTwiceByTheSamePlayer() throws InterruptedException {
        var onMessage = new CountDownLatch(1);
        final var firstClient = createClient(SERVER_URI.concat("/game/two"));
        final var secondClient = createClient(SERVER_URI.concat("/game/two"), onMessage);
        firstClient.connectBlocking();
        secondClient.connectBlocking();

        final var moveToSecond = GameMove.from(List.of("N")).get();
        final var messageForSecond = parser.toJson(moveToSecond);
        firstClient.send(messageForSecond);
        var gotMessage = onMessage.await(1, TimeUnit.SECONDS);
        assertThat(gotMessage).isTrue();

        onMessage = new CountDownLatch(1);
        final var moveToSecondAgain = GameMove.from(List.of("N")).get();
        final var messageForSecondAgain = parser.toJson(moveToSecondAgain);
        firstClient.send(messageForSecondAgain);
        gotMessage = onMessage.await(1, TimeUnit.SECONDS);
        assertThat(gotMessage).isFalse();

        firstClient.closeBlocking();
        secondClient.closeBlocking();
        assertThat(secondClient)
                .extracting(SimpleWebSocketClient::getMessages)
                .asList()
                .hasSize(1)
                .containsExactly(messageForSecond);
        assertThat(firstClient)
                .extracting(SimpleWebSocketClient::getMessages)
                .asList()
                .hasSize(2)
                .containsExactly(ACCEPT_RESPONSE, parser.toJson(new RejectMove(moveToSecondAgain)));
    }
}
