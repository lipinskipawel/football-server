package com.github.lipinskipawel.server;

import com.github.lipinskipawel.api.move.GameMove;
import com.google.gson.Gson;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

final class FootballServerIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(FootballServerIT.class);
    private static final ExecutorService pool = Executors.newFixedThreadPool(1);
    private static final int PORT = 8090;
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
    void shouldRejectClientWhenURIDoNotMeetPolicy() throws InterruptedException {
        final var client = createClient("/example");

        client.connectBlocking();

        final var connectionClose = client.isOpen();
        client.closeBlocking();
        assertThat(connectionClose).isFalse();
    }

    @Test
    void shouldClientNotReceivedMessageWhenNotAGameMove() throws InterruptedException {
        final var onMessage = new CountDownLatch(1);
        final var endpoint = "/game/123";
        final var firstClient = createClient(endpoint);
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
        final var firstClient = createClient("/game/123");
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
        final var firstClient = createClient(endpoint);
        final var secondClient = createClient(endpoint);
        firstClient.connectBlocking();
        secondClient.connectBlocking();

        final var thirdClient = createClient(endpoint, null, onClose);
        thirdClient.connectBlocking();

        final var notConnected = onClose.await(1, TimeUnit.SECONDS);
        firstClient.closeBlocking();
        secondClient.closeBlocking();
        assertThat(notConnected).isTrue();
        assertThat(thirdClient.isClosed()).isTrue();
    }

    private WebSocketClientWrapper createClient(final String endpoint) {
        return createClient(endpoint, new CountDownLatch(0), new CountDownLatch(0));
    }

    private WebSocketClientWrapper createClient(final String endpoint, final CountDownLatch onMessage) {
        return createClient(endpoint, onMessage, new CountDownLatch(0));
    }

    private WebSocketClientWrapper createClient(final String endpoint, final CountDownLatch onMessage, final CountDownLatch onClose) {
        final var uri = URI.create(WebSocketClientWrapper.SERVER_URI.concat(endpoint));
        return new WebSocketClientWrapper(uri, onMessage, onClose);
    }

    private static class WebSocketClientWrapper extends WebSocketClient {
        private final CountDownLatch onMessage;
        private final CountDownLatch onClose;
        static final String SERVER_URI = "ws://localhost:%d".formatted(PORT);

        WebSocketClientWrapper(final URI uri,
                               final CountDownLatch onMessage,
                               final CountDownLatch onClose) {
            super(uri);
            this.onMessage = onMessage;
            this.onClose = onClose;
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            LOGGER.info("Client onOpen");
        }

        @Override
        public void onMessage(String message) {
            LOGGER.info("Client onMessage: " + message);
            onMessage.countDown();
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            LOGGER.info("Client onClose");
            onClose.countDown();
        }

        @Override
        public void onError(Exception ex) {
            LOGGER.error("Client onError: ", ex);
        }
    }
}
