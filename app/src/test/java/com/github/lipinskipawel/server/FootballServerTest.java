package com.github.lipinskipawel.server;

import com.github.lipinskipawel.api.WaitingPlayers;
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
    void shouldRejectClientWhenURIDoNotMeetPolicy() throws InterruptedException {
        final var client = createClient(URI.create("ws://localhost:8090/example"), null);

        client.connectBlocking();

        final var connectionClose = client.isOpen();
        client.closeBlocking();
        assertThat(connectionClose).isFalse();
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

        final var thirdClient = createClient(uri, null, onClose, null);
        thirdClient.connectBlocking();

        final var notConnected = onClose.await(1, TimeUnit.SECONDS);
        firstClient.closeBlocking();
        secondClient.closeBlocking();
        assertThat(notConnected).isTrue();
        assertThat(thirdClient.isClosed()).isTrue();
    }

    @Test
    void shouldReceivedListOfPlayerWhenConnectedOnlyOneClient() throws InterruptedException {
        final var onPlayerList = new CountDownLatch(1);
        final var uri = URI.create("ws://localhost:8090/chat/one");
        final var client = createClient(uri, null, null, onPlayerList);
        client.connectBlocking();

        final var receivedMessage = onPlayerList.await(1, TimeUnit.SECONDS);

        client.closeBlocking();
        assertThat(receivedMessage).isTrue();
        assertThat(client.playersList.players().size()).isEqualTo(1);
        client.playersList.players().forEach(it -> System.out.println(it.getUrl()));
    }

    @Test
    void shouldReceivedListOfPlayerWhenTwoClientsAreConnected() throws InterruptedException {
        final var onPlayerList = new CountDownLatch(1);
        final var firstUri = URI.create("ws://localhost:8090/chat/one");
        final var secondUri = URI.create("ws://localhost:8090/chat/two");
        final var firstClient = createClient(firstUri, null, null, null);
        final var secondClient = createClient(secondUri, null, null, onPlayerList);
        firstClient.connectBlocking();
        secondClient.connectBlocking();

        final var receivedMessage = onPlayerList.await(1, TimeUnit.SECONDS);

        firstClient.closeBlocking();
        secondClient.closeBlocking();
        assertThat(receivedMessage).isTrue();
        assertThat(secondClient.playersList.players().size()).isEqualTo(2);
        secondClient.playersList.players().forEach(it -> System.out.println(it.getUrl()));
    }

    @Test
    void shouldReceivedListOfPlayerWhenSecondClientIsConnected() throws InterruptedException {
        final var onPlayerList = new CountDownLatch(2);
        final var firstUri = URI.create("ws://localhost:8090/chat/one");
        final var secondUri = URI.create("ws://localhost:8090/chat/two");
        final var firstClient = createClient(firstUri, null, null, onPlayerList);
        final var secondClient = createClient(secondUri, null, null, onPlayerList);
        firstClient.connectBlocking();
        secondClient.connectBlocking();

        final var receivedMessage = onPlayerList.await(1, TimeUnit.SECONDS);

        firstClient.closeBlocking();
        secondClient.closeBlocking();
        assertThat(receivedMessage).isTrue();
        assertThat(firstClient.playersList.players().size()).isEqualTo(2);
        firstClient.playersList.players().forEach(it -> System.out.println(it.getUrl()));
    }

    private WebSocketClientWrapper createClient(final URI uri, final CountDownLatch onMessage) {
        return createClient(uri, onMessage, null, null);
    }

    private WebSocketClientWrapper createClient(final URI uri, final CountDownLatch onMessage, final CountDownLatch onClose,
                                                final CountDownLatch onPlayerList) {
        return new WebSocketClientWrapper(uri, onMessage, onClose, onPlayerList);
    }

    static class WebSocketClientWrapper extends WebSocketClient {
        private final Optional<CountDownLatch> onMessage;
        private final Optional<CountDownLatch> onClose;
        private final Optional<CountDownLatch> onPlayerList;
        private final Gson parser;
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
