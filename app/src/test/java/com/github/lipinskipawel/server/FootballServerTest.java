package com.github.lipinskipawel.server;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

final class FootballServerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(FootballServerTest.class);
    private static final ExecutorService pool = Executors.newFixedThreadPool(1);
    private static final FootballServer server = new FootballServer(
            new InetSocketAddress("localhost", 8090), new DualConnection()
    );

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
    void shouldClientReceivedMessageFromOtherClient() throws InterruptedException {
        final var onMessage = new CountDownLatch(1);
        final var commonUri = URI.create("ws://localhost:8090/chat/123");
        final var firstClient = createClient(commonUri, null);
        final var secondClient = createClient(commonUri, onMessage);
        firstClient.connectBlocking();
        secondClient.connectBlocking();

        firstClient.send("msg");

        final var gotMessage = onMessage.await(1, TimeUnit.SECONDS);
        firstClient.closeBlocking();
        secondClient.closeBlocking();
        assertThat(gotMessage).isTrue();
    }

    @Test
    void shouldNotReceivedMsgWhenConnectedToDifferentUri() throws InterruptedException {
        final var onMessage = new CountDownLatch(1);
        final var firstClient = createClient(URI.create("ws://localhost:8090/chat/123"), null);
        final var secondClient = createClient(URI.create("ws://localhost:8090/chat/other"), onMessage);
        firstClient.connectBlocking();
        secondClient.connectBlocking();

        firstClient.send("msg");

        final var gotMessage = onMessage.await(1, TimeUnit.SECONDS);
        firstClient.closeBlocking();
        secondClient.closeBlocking();
        assertThat(gotMessage).isFalse();
    }

    @Test
    void shouldAllowOnlyTwoClientsConnectToTheSameEndpoint() throws InterruptedException {
        final var onClose = new CountDownLatch(1);
        final var uri = URI.create("ws://localhost:8090/chat/one");
        final var firstClient = createClient(uri, null);
        final var secondClient = createClient(uri, null);
        firstClient.connectBlocking();
        secondClient.connectBlocking();

        final var thirdClient = createClient(uri, null, onClose);
        thirdClient.connectBlocking();

        final var notConnected = onClose.await(1, TimeUnit.SECONDS);
        firstClient.closeBlocking();
        secondClient.closeBlocking();
        assertThat(notConnected).isTrue();
        assertThat(thirdClient.isClosed()).isTrue();
    }

    private WebSocketClientWrapper createClient(final URI uri, final CountDownLatch latch) {
        return createClient(uri, latch, null);
    }

    private WebSocketClientWrapper createClient(final URI uri, final CountDownLatch latch, final CountDownLatch onClose) {
        return new WebSocketClientWrapper(uri, latch, onClose);
    }

    static class WebSocketClientWrapper extends WebSocketClient {
        private final Optional<CountDownLatch> latch;
        private final Optional<CountDownLatch> onClose;

        WebSocketClientWrapper(final URI uri, final CountDownLatch latch, final CountDownLatch onClose) {
            super(uri);
            this.latch = Optional.ofNullable(latch);
            this.onClose = Optional.ofNullable(onClose);
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            LOGGER.info("Client onOpen");
        }

        @Override
        public void onMessage(String message) {
            LOGGER.info("Client onMessage: " + message);
            latch.ifPresent(CountDownLatch::countDown);
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
