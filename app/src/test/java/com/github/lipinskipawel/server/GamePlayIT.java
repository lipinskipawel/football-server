package com.github.lipinskipawel.server;

import com.github.lipinskipawel.api.move.GameMove;
import com.google.gson.Gson;
import org.assertj.core.api.WithAssertions;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

final class GamePlayIT implements WithAssertions {
    private static final ExecutorService pool = Executors.newFixedThreadPool(1);
    private static final int PORT = 8092;
    private static final FootballServer server = new FootballServer(new InetSocketAddress("localhost", PORT));
    private static final Gson parser = new Gson();

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
        final var onMessage = new CountDownLatch(1);
        final var firstClient = createClient("/game/one");
        final var secondClient = createClient("/game/one", onMessage);
        firstClient.connectBlocking();
        secondClient.connectBlocking();

        final var move = GameMove.from(List.of("N")).get();
        final var message = parser.toJson(move);
        firstClient.send(message);

        final var gotMessage = onMessage.await(1, TimeUnit.SECONDS);
        firstClient.closeBlocking();
        secondClient.closeBlocking();
        assertThat(gotMessage).isTrue();
        assertThat(secondClient)
                .extracting(it -> it.messages)
                .asList()
                .hasSize(1)
                .containsExactly(message);
    }

    @Test
    void shouldAllowToExchangeMovesBetweenPlayersWhenPlayingTogether() throws InterruptedException {
        var onMessage = new CountDownLatch(1);
        final var firstClient = createClient("/game/two");
        final var secondClient = createClient("/game/two", onMessage);
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
                .extracting(it -> it.messages)
                .asList()
                .hasSize(1)
                .containsExactly(messageForSecond);
        assertThat(firstClient)
                .extracting(it -> it.messages)
                .asList()
                .hasSize(1)
                .containsExactly(messageForFirst);
    }

    @Test
    void shouldNotAllowToMoveTwiceByTheSamePlayer() throws InterruptedException {
        var onMessage = new CountDownLatch(1);
        final var firstClient = createClient("/game/two");
        final var secondClient = createClient("/game/two", onMessage);
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
                .extracting(it -> it.messages)
                .asList()
                .hasSize(1)
                .containsExactly(messageForSecond);
        assertThat(firstClient)
                .extracting(it -> it.messages)
                .asList()
                .hasSize(0);
    }

    private TestWebSocketClient createClient(final String endpoint) {
        return createClient(endpoint, new CountDownLatch(0));
    }

    private TestWebSocketClient createClient(final String endpoint, final CountDownLatch onMessage) {
        final var uri = URI.create(TestWebSocketClient.SERVER_URI.concat(endpoint));
        return new TestWebSocketClient(uri, onMessage);
    }

    private static class TestWebSocketClient extends WebSocketClient {
        private static final String SERVER_URI = "ws://localhost:%d".formatted(PORT);
        private final List<String> messages;
        private CountDownLatch onMessage;

        public TestWebSocketClient(final URI uri, final CountDownLatch onMessage) {
            super(uri);
            this.onMessage = onMessage;
            this.messages = new ArrayList<>();
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {

        }

        @Override
        public void onMessage(String message) {
            this.messages.add(message);
            this.onMessage.countDown();
        }

        public void latchOnMessage(final CountDownLatch onMessage) {
            this.onMessage = onMessage;
        }


        @Override
        public void onClose(int code, String reason, boolean remote) {

        }

        @Override
        public void onError(Exception ex) {

        }
    }
}
