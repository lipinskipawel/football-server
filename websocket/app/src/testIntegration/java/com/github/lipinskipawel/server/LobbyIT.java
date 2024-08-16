package com.github.lipinskipawel.server;

import com.github.lipinskipawel.IntegrationSpec;
import com.github.lipinskipawel.api.Player;
import com.github.lipinskipawel.api.WaitingPlayers;
import com.google.gson.Gson;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.lipinskipawel.SimpleWebSocketClient.createClient;

final class LobbyIT extends IntegrationSpec implements WithAssertions {
    private static final Gson parser = new Gson();
    static final String SERVER_URI = "ws://localhost:%d/ws/lobby".formatted(PORT);

    @Test
    void shouldAllowToConnectToLobby() throws InterruptedException {
        registerUsername("first");
        final var client = createClient(SERVER_URI);
        client.addHeader("cookie", "first_token");
        client.connectBlocking();

        final var isOpen = client.isOpen();

        client.closeBlocking();
        assertThat(isOpen).isTrue();
    }

    @Test
    void shouldNotifyClientsWhenOneOfThemLeavesLobby() throws InterruptedException {
        registerUsername("first");
        registerUsername("second");
        final var client = createClient(SERVER_URI);
        client.addHeader("cookie", "first_token");
        final var secondClient = createClient(SERVER_URI);
        secondClient.addHeader("cookie", "second_token");
        client.connectBlocking();
        secondClient.connectBlocking();
        waitFor(() -> secondClient.getMessages().size() == 1);

        client.closeBlocking();

        waitFor(() -> secondClient.getMessages().size() == 2);
        secondClient.closeBlocking();
    }

    @Test
    void shouldReceiveWaitingPlayersMessageWhenOnlyOnePlayerIsInTheLobby() throws InterruptedException {
        registerUsername("first");
        final var expectedWaitingPlayers = WaitingPlayers.fromPlayers(
            List.of(Player.fromUsername("first"))
        );
        final var client = createClient(SERVER_URI);
        client.addHeader("cookie", "first_token");
        client.connectBlocking();

        waitFor(() -> client.getMessages().size() == 1);
        client.closeBlocking();

        final var waitingPlayers = parser.fromJson(client.getMessages().get(0), WaitingPlayers.class);
        assertThat(waitingPlayers).isEqualTo(expectedWaitingPlayers);
    }

    @Test
    void shouldReceivedWaitingPlayersMessageWithTwoEntriesWhenTwoClientAreInLobby() throws InterruptedException {
        registerUsername("first");
        registerUsername("second");
        final var expectedFirst = Player.fromUsername("first");
        final var expectedSecond = Player.fromUsername("second");
        final var client = createClient(SERVER_URI);
        client.addHeader("cookie", "first_token");
        final var secondClient = createClient(SERVER_URI);
        secondClient.addHeader("cookie", "second_token");
        client.connectBlocking();

        secondClient.connectBlocking();
        waitFor(() -> secondClient.getMessages().size() == 1);
        client.closeBlocking();
        secondClient.closeBlocking();

        final var waitingPlayers = parser.fromJson(secondClient.getMessages().get(0), WaitingPlayers.class);
        assertThat(waitingPlayers)
            .extracting(WaitingPlayers::players)
            .asList()
            .hasSize(2)
            .containsExactly(expectedFirst, expectedSecond);
    }

    @Test
    void shouldPairBothClientsWhenRequested() throws InterruptedException {
        final var pairedClients = getPairedClients(SERVER_URI);

        assertThat(pairedClients).hasSize(2);
        final var isOpenFirst = pairedClients[0].isOpen();
        final var isOpenSecond = pairedClients[1].isOpen();
        pairedClients[0].closeBlocking();
        pairedClients[1].closeBlocking();
        assertThat(isOpenFirst).isTrue();
        assertThat(isOpenSecond).isTrue();
    }
}
