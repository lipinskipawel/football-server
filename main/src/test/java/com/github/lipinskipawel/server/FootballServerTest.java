package com.github.lipinskipawel.server;

import org.assertj.core.api.Assertions;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class FootballServerTest {
    private static final ExecutorService pool = Executors.newFixedThreadPool(1);
    private static final FootballServer server = new FootballServer(new InetSocketAddress("localhost", 8090));

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
        final var latch = new CountDownLatch(1);
        final var commonUri = URI.create("ws://localhost:8090/some");
        final var firstClient = createClient(commonUri, null);
        final var secondClient = createClient(commonUri, latch);
        firstClient.connectBlocking();
        secondClient.connectBlocking();

        firstClient.send("msg");

        firstClient.closeBlocking();
        secondClient.closeBlocking();
        latch.await();
        Assertions.assertThat(secondClient.onMessageCount).isEqualTo(1);
    }

    private WebSocketClientWrapper createClient(final URI uri, final CountDownLatch latch) {
        return new WebSocketClientWrapper(uri, latch);
    }

    static class WebSocketClientWrapper extends WebSocketClient {
        private final Optional<CountDownLatch> latch;
        private int onMessageCount;

        WebSocketClientWrapper(final URI uri, final CountDownLatch latch) {
            super(uri);
            this.onMessageCount = 0;
            this.latch = Optional.ofNullable(latch);
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            System.out.println("client onOpen");
        }

        @Override
        public void onMessage(String message) {
            System.out.println("Client onMessage: " + message);
            onMessageCount++;
            latch.ifPresent(CountDownLatch::countDown);
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            System.out.println("Client: onClose");
        }

        @Override
        public void onError(Exception ex) {

        }
    }
}
