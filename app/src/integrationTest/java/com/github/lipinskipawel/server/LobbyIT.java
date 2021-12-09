package com.github.lipinskipawel.server;

import com.github.lipinskipawel.api.Player;
import com.github.lipinskipawel.api.WaitingPlayers;
import com.github.lipinskipawel.client.FootballClientCreator;
import com.github.lipinskipawel.extension.Application;
import com.google.gson.Gson;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.lipinskipawel.client.FootballClientCreator.waitFor;
import static com.github.lipinskipawel.client.SimpleWebSocketClient.createClient;

@Application(port = LobbyIT.PORT)
final class LobbyIT implements WithAssertions {
    private static final Gson parser = new Gson();
    static final int PORT = 8091;
    static final String SERVER_URI = "ws://localhost:%d/lobby".formatted(PORT);

    @Test
    void shouldAllowToConnectToLobby() throws InterruptedException {
        final var client = createClient(SERVER_URI);
        client.connectBlocking();

        final var isOpen = client.isOpen();

        client.closeBlocking();
        assertThat(isOpen).isTrue();
    }

    @Test
    void shouldNotifyClientsWhenOneOfThemLeavesLobby() throws InterruptedException {
        final var client = createClient(SERVER_URI);
        final var secondClient = createClient(SERVER_URI);
        client.connectBlocking();
        secondClient.connectBlocking();
        waitFor(() -> secondClient.getMessages().size() == 1);

        client.closeBlocking();

        waitFor(() -> secondClient.getMessages().size() == 2);
        secondClient.closeBlocking();
    }

    @Test
    void shouldReceiveWaitingPlayersMessageWhenOnlyOnePlayerIsInTheLobby() throws InterruptedException {
        final var expectedWaitingPlayers = WaitingPlayers.fromPlayers(
                List.of(Player.fromUsername("anonymous"))
        );
        final var client = createClient(SERVER_URI);
        client.connectBlocking();

        waitFor(() -> client.getMessages().size() == 1);
        client.closeBlocking();

        final var waitingPlayers = parser.fromJson(client.getMessages().get(0), WaitingPlayers.class);
        assertThat(waitingPlayers).isEqualTo(expectedWaitingPlayers);
    }

    @Test
    void shouldReceivedWaitingPlayersMessageWithTwoEntriesWhenTwoClientAreInLobby() throws InterruptedException {
        final var expected = Player.fromUsername("anonymous");
        final var client = createClient(SERVER_URI);
        final var secondClient = createClient(SERVER_URI);
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
                .containsExactly(expected, expected);
    }

    @Test
    void shouldPairBothClientsWhenRequested() throws InterruptedException {
        final var pairedClients = FootballClientCreator.getPairedClients(SERVER_URI);

        assertThat(pairedClients).hasSize(2);
        final var isOpenFirst = pairedClients[0].isOpen();
        final var isOpenSecond = pairedClients[1].isOpen();
        pairedClients[0].closeBlocking();
        pairedClients[1].closeBlocking();
        assertThat(isOpenFirst).isTrue();
        assertThat(isOpenSecond).isTrue();
    }
}
