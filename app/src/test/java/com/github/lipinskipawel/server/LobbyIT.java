package com.github.lipinskipawel.server;

import com.github.lipinskipawel.api.Player;
import com.github.lipinskipawel.api.RequestToPlay;
import com.github.lipinskipawel.api.WaitingPlayers;
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

final class LobbyIT implements WithAssertions {
    private static final ExecutorService pool = Executors.newFixedThreadPool(1);
    private static final int PORT = 8091;
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
    void shouldAllowToConnectToLobby() throws InterruptedException {
        final var onMessage = new CountDownLatch(1);
        final var uri = URI.create("ws://localhost:%d/lobby".formatted(PORT));
        final var client = createClient(uri, onMessage);
        client.connectBlocking();

        final var isOpen = client.isOpen();

        client.closeBlocking();
        assertThat(isOpen).isTrue();
    }

    @Test
    void shouldReceiveWaitingPlayersMessageWhenOnlyOnePlayerIsInTheLobby() throws InterruptedException {
        final var expectedWaitingPlayers = WaitingPlayers.fromPlayers(List.of(Player.fromUrl("/lobby")));
        final var onMessage = new CountDownLatch(1);
        final var uri = URI.create("ws://localhost:%d/lobby".formatted(PORT));
        final var client = createClient(uri, onMessage);
        client.connectBlocking();

        final var messageReceived = onMessage.await(1, TimeUnit.SECONDS);

        client.closeBlocking();
        assertThat(messageReceived).isTrue();
        assertThat(client.messages).hasSize(1);
        final var waitingPlayers = parser.fromJson(client.messages.get(0), WaitingPlayers.class);
        assertThat(waitingPlayers).isEqualTo(expectedWaitingPlayers);
    }

    @Test
    void shouldReceivedWaitingPlayersMessageWithOneEntryWhenOneClientIsAlreadyConnected() throws InterruptedException {
        final var expected = Player.fromUrl("/lobby");
        final var onMessage = new CountDownLatch(1);
        final var uri = URI.create("ws://localhost:%d/lobby".formatted(PORT));
        final var client = createClient(uri, null);
        final var secondClient = createClient(uri, onMessage);
        client.connectBlocking();
        secondClient.connectBlocking();

        final var gotMessage = onMessage.await(1, TimeUnit.SECONDS);

        client.closeBlocking();
        secondClient.closeBlocking();
        assertThat(gotMessage).isTrue();
        assertThat(secondClient.messages).hasSize(1);
        final var waitingPlayers = parser.fromJson(secondClient.messages.get(0), WaitingPlayers.class);
        assertThat(waitingPlayers)
                .extracting(WaitingPlayers::players)
                .asList()
                .hasSize(2)
                .containsExactly(expected, expected);
    }

    @Test
    void shouldPairBothClientsWhenRequested() throws InterruptedException {
        var firstLatch = new CountDownLatch(1);
        var secondLatch = new CountDownLatch(1);
        final var uri = URI.create("ws://localhost:%d/lobby".formatted(PORT));
        final var firstClient = createClient(uri, firstLatch);
        final var secondClient = createClient(uri, secondLatch);
        firstClient.connectBlocking();
        final var firstEntry = firstLatch.await(1, TimeUnit.SECONDS);
        assertThat(firstEntry).isTrue();
        firstClient.latchOnMessage(new CountDownLatch(1));
        secondClient.connectBlocking();
        waitForBothClientsForWaitingPlayersAPI(
                firstLatch, secondLatch,
                firstClient, secondClient
        );

        final var message = firstClient.messages.get(1);
        final var opponent = parser.fromJson(message, WaitingPlayers.class)
                .players()
                .get(1);
        final var requestToPlay = RequestToPlay.with(opponent);
        firstClient.send(parser.toJson(requestToPlay));

        final var forFirstClient = firstLatch.await(1, TimeUnit.SECONDS);
        final var forSecondClient = secondLatch.await(1, TimeUnit.SECONDS);
        final var isClosedFirst = firstClient.isClosed();
        final var isClosedSecond = secondClient.isClosed();
        firstClient.closeBlocking();
        secondClient.closeBlocking();
        assertThat(forFirstClient).isTrue();
        assertThat(forSecondClient).isTrue();
        assertThat(isClosedFirst).isTrue();
        assertThat(isClosedSecond).isTrue();
    }

    private void waitForBothClientsForWaitingPlayersAPI(
            CountDownLatch firstLatch,
            CountDownLatch secondLatch,
            TestWebSocketClient firstClient,
            TestWebSocketClient secondClient
    ) throws InterruptedException {
        final var gotTwoMessages = firstLatch.await(1, TimeUnit.SECONDS);
        assertThat(gotTwoMessages).isTrue();
        firstLatch = new CountDownLatch(1);
        firstClient.latchOnMessage(firstLatch);
        final var gotWaitingList = secondLatch.await(1, TimeUnit.SECONDS);
        assertThat(gotWaitingList).isTrue();
        secondLatch = new CountDownLatch(1);
        secondClient.latchOnMessage(secondLatch);
    }

    private static TestWebSocketClient createClient(final URI uri, CountDownLatch onMessage) {
        return new TestWebSocketClient(uri, onMessage == null ? new CountDownLatch(0) : onMessage);
    }

    private static class TestWebSocketClient extends WebSocketClient {
        private CountDownLatch onMessage;
        List<String> messages;

        public TestWebSocketClient(URI serverUri, CountDownLatch onMessage) {
            super(serverUri);
            this.onMessage = onMessage;
            this.messages = new ArrayList<>();
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {

        }

        @Override
        public void onMessage(String message) {
            System.out.println("TestWebSocketClient onMessage: " + message);
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
