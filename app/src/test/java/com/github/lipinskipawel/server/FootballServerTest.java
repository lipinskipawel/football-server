package com.github.lipinskipawel.server;

import com.github.lipinskipawel.api.WaitingPlayers;
import com.github.lipinskipawel.api.move.GameMove;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

final class FootballServerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(FootballServerTest.class);
    private static final ExecutorService pool = Executors.newFixedThreadPool(1);
    private static final int PORT = 8090;
    private static final FootballServer server = new FootballServer(
            new InetSocketAddress("localhost", PORT), new DualConnection()
    );
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
    void shouldRejectClientWhenURIDoNotMeetPolicy() throws InterruptedException {
        final var client = createClient("/example", null);

        client.connectBlocking();

        final var connectionClose = client.isOpen();
        client.closeBlocking();
        assertThat(connectionClose).isFalse();
    }

    @Test
    void shouldClientNotReceivedMessageWhenNotAGameMove() throws InterruptedException {
        final var onMessage = new CountDownLatch(1);
        final var endpoint = "/game/123";
        final var firstClient = createClient(endpoint, null);
        final var secondClient = createClient(endpoint, onMessage);
        firstClient.connectBlocking();
        secondClient.connectBlocking();

        firstClient.send("msg");

        final var notReceived = onMessage.await(1, TimeUnit.SECONDS);
        firstClient.closeBlocking();
        secondClient.closeBlocking();
        assertThat(notReceived).isFalse();
    }

    @Test
    void shouldNotReceivedMsgWhenConnectedToDifferentUri() throws InterruptedException {
        final var onMessage = new CountDownLatch(1);
        final var firstClient = createClient("/game/123", null);
        final var secondClient = createClient("/game/other", onMessage);
        firstClient.connectBlocking();
        secondClient.connectBlocking();

        final var move = GameMove.from(List.of("N")).get();
        final var message = parser.toJson(move);
        firstClient.send(message);

        final var notReceived = onMessage.await(1, TimeUnit.SECONDS);
        firstClient.closeBlocking();
        secondClient.closeBlocking();
        assertThat(notReceived).isFalse();
    }

    @Test
    void shouldAllowOnlyTwoClientsConnectToTheSameEndpoint() throws InterruptedException {
        final var onClose = new CountDownLatch(1);
        final var endpoint = "/game/one";
        final var firstClient = createClient(endpoint, null);
        final var secondClient = createClient(endpoint, null);
        firstClient.connectBlocking();
        secondClient.connectBlocking();

        final var thirdClient = createClient(endpoint, null, onClose, null);
        thirdClient.connectBlocking();

        final var notConnected = onClose.await(1, TimeUnit.SECONDS);
        firstClient.closeBlocking();
        secondClient.closeBlocking();
        assertThat(notConnected).isTrue();
        assertThat(thirdClient.isClosed()).isTrue();
    }

    private WebSocketClientWrapper createClient(final String endpoint, final CountDownLatch onMessage) {
        return createClient(endpoint, onMessage, null, null);
    }

    private WebSocketClientWrapper createClient(final String endpoint, final CountDownLatch onMessage, final CountDownLatch onClose,
                                                final CountDownLatch onPlayerList) {
        final var uri = URI.create(WebSocketClientWrapper.SERVER_URI.concat(endpoint));
        return new WebSocketClientWrapper(uri, onMessage, onClose, onPlayerList);
    }

    static class WebSocketClientWrapper extends WebSocketClient {
        private final Optional<CountDownLatch> onMessage;
        private final Optional<CountDownLatch> onClose;
        private final Optional<CountDownLatch> onPlayerList;
        private final Gson parser;
        static final String SERVER_URI = "ws://localhost:%d".formatted(PORT);
        WaitingPlayers playersList;

        WebSocketClientWrapper(final URI uri,
                               final CountDownLatch onMessage,
                               final CountDownLatch onClose,
                               final CountDownLatch onPlayerList) {
            super(uri);
            this.onMessage = Optional.ofNullable(onMessage);
            this.onClose = Optional.ofNullable(onClose);
            this.onPlayerList = Optional.ofNullable(onPlayerList);
            this.parser = new Gson();
            this.playersList = null;
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            LOGGER.info("Client onOpen");
        }

        @Override
        public void onMessage(String message) {
            LOGGER.info("Client onMessage: " + message);
            var type = new TypeToken<WaitingPlayers>() {
            }.getType();
            try {
                this.playersList = this.parser.fromJson(message, type);
                onPlayerList.ifPresent(CountDownLatch::countDown);
                return;
            } catch (JsonSyntaxException ignore) {
            }
            onMessage.ifPresent(CountDownLatch::countDown);
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            LOGGER.info("Client onClose");
            onClose.ifPresent(CountDownLatch::countDown);
        }

        @Override
        public void onError(Exception ex) {
            LOGGER.error("Client onError: ", ex);
        }
    }
}
