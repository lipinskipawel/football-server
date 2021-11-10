package com.github.lipinskipawel.server;

import com.github.lipinskipawel.api.Player;
import com.github.lipinskipawel.api.RequestToPlay;
import com.github.lipinskipawel.api.WaitingPlayers;
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

final class LobbyIT implements WithAssertions {
    private static final ExecutorService pool = Executors.newFixedThreadPool(1);
    private static final int PORT = 8091;
    private static final String SERVER_URI = "ws://localhost:%d/lobby".formatted(PORT);
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
    void shouldAllowToConnectToLobby() throws InterruptedException {
        final var onMessage = new CountDownLatch(1);
        final var client = createClient(SERVER_URI, onMessage);
        client.connectBlocking();

        final var isOpen = client.isOpen();

        client.closeBlocking();
        assertThat(isOpen).isTrue();
    }

    @Test
    void shouldReceiveWaitingPlayersMessageWhenOnlyOnePlayerIsInTheLobby() throws InterruptedException {
        final var expectedWaitingPlayers = WaitingPlayers.fromPlayers(
                List.of(Player.fromUsername("anonymous"))
        );
        final var onMessage = new CountDownLatch(1);
        final var client = createClient(SERVER_URI, onMessage);
        client.connectBlocking();

        final var messageReceived = onMessage.await(1, TimeUnit.SECONDS);

        client.closeBlocking();
        assertThat(messageReceived).isTrue();
        assertThat(client.getMessages()).hasSize(1);
        final var waitingPlayers = parser.fromJson(client.getMessages().get(0), WaitingPlayers.class);
        assertThat(waitingPlayers).isEqualTo(expectedWaitingPlayers);
    }

    @Test
    void shouldReceivedWaitingPlayersMessageWithTwoEntriesWhenTwoClientAreInLobby() throws InterruptedException {
        final var expected = Player.fromUsername("anonymous");
        final var onMessage = new CountDownLatch(1);
        final var client = createClient(SERVER_URI);
        final var secondClient = createClient(SERVER_URI, onMessage);
        client.connectBlocking();
        secondClient.connectBlocking();

        final var gotMessage = onMessage.await(1, TimeUnit.SECONDS);

        client.closeBlocking();
        secondClient.closeBlocking();
        assertThat(gotMessage).isTrue();
        assertThat(secondClient.getMessages()).hasSize(1);
        final var waitingPlayers = parser.fromJson(secondClient.getMessages().get(0), WaitingPlayers.class);
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
        final var firstClient = createClient(SERVER_URI, firstLatch);
        final var secondClient = createClient(SERVER_URI, secondLatch);
        firstClient.addHeader("cookie", "firstClient");
        secondClient.addHeader("cookie", "secondClient");
        firstClient.connectBlocking();
        final var firstEntry = firstLatch.await(1, TimeUnit.SECONDS);
        assertThat(firstEntry).isTrue();
        firstClient.latchOnMessage(new CountDownLatch(1));
        secondClient.connectBlocking();
        waitForBothClientsForWaitingPlayersAPI(
                firstLatch, secondLatch,
                firstClient, secondClient
        );

        final var message = firstClient.getMessages().get(1);
        final var opponent = parser.fromJson(message, WaitingPlayers.class)
                .players()
                .get(1);
        final var requestToPlay = RequestToPlay.with(opponent);
        firstLatch = new CountDownLatch(1);
        firstClient.latchOnMessage(firstLatch);
        secondLatch = new CountDownLatch(1);
        secondClient.latchOnMessage(secondLatch);
        firstClient.send(parser.toJson(requestToPlay));

        final var forFirstClient = firstLatch.await(1, TimeUnit.SECONDS);
        final var forSecondClient = secondLatch.await(1, TimeUnit.SECONDS);
        firstClient.closeBlocking();
        secondClient.closeBlocking();
        assertThat(forFirstClient).isTrue();
        assertThat(forSecondClient).isTrue();
        assertThat(firstClient)
                .extracting(SimpleWebSocketClient::getMessages)
                .asList()
                .hasSize(3);
        assertThat(secondClient)
                .extracting(SimpleWebSocketClient::getMessages)
                .asList()
                .hasSize(2);
    }

    private void waitForBothClientsForWaitingPlayersAPI(
            CountDownLatch firstLatch,
            CountDownLatch secondLatch,
            SimpleWebSocketClient firstClient,
            SimpleWebSocketClient secondClient
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
}
