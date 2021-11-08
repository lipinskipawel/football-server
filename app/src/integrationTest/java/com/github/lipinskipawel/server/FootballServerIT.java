package com.github.lipinskipawel.server;

import com.github.lipinskipawel.api.move.GameMove;
import com.google.gson.Gson;
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
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

final class FootballServerIT {
    private static final ExecutorService pool = Executors.newFixedThreadPool(1);
    private static final int PORT = 8090;
    private static final String SERVER_URI = "ws://localhost:%d".formatted(PORT);
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
        final var client = createClient(SERVER_URI.concat("/example"));

        client.connectBlocking();

        final var connectionClose = client.isOpen();
        client.closeBlocking();
        assertThat(connectionClose).isFalse();
    }

    @Test
    void shouldClientNotReceivedMessageWhenNotAGameMove() throws InterruptedException {
        final var onMessage = new CountDownLatch(1);
        final var endpoint = "/game/123";
        final var firstClient = createClient(SERVER_URI.concat(endpoint));
        final var secondClient = createClient(SERVER_URI.concat(endpoint), onMessage);
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
        final var firstClient = createClient(SERVER_URI.concat("/game/123"));
        final var secondClient = createClient(SERVER_URI.concat("/game/other"), onMessage);
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
        final var firstClient = createClient(SERVER_URI.concat(endpoint));
        final var secondClient = createClient(SERVER_URI.concat(endpoint));
        firstClient.connectBlocking();
        secondClient.connectBlocking();

        final var thirdClient = createClient(SERVER_URI.concat(endpoint), null, onClose);
        thirdClient.connectBlocking();

        final var notConnected = onClose.await(1, TimeUnit.SECONDS);
        firstClient.closeBlocking();
        secondClient.closeBlocking();
        assertThat(notConnected).isTrue();
        assertThat(thirdClient.isClosed()).isTrue();
    }
}
