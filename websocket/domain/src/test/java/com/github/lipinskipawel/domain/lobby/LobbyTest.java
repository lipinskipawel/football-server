package com.github.lipinskipawel.domain.lobby;

import com.github.lipinskipawel.domain.mocks.TestConnectedClient;
import com.github.lipinskipawel.user.ConnectedClient;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

class LobbyTest implements WithAssertions {

    @Nested
    class AcceptingClients {
        @Test
        void shouldAcceptOneClient() {
            final var list = new ArrayList<ConnectedClient>();
            final var client = new TestConnectedClient("a");
            final var lobby = Lobby.of(list);

            lobby.accept(client);

            assertThat(list)
                    .hasSize(1)
                    .containsExactly(client);
        }

        @Test
        void shouldAcceptTwoClients() {
            final var numberOfConnectedClients = new ArrayList<ConnectedClient>();
            final var firstClient = new TestConnectedClient("a");
            final var secondClient = new TestConnectedClient("b");
            final var lobby = Lobby.of(numberOfConnectedClients);

            lobby.accept(firstClient);
            lobby.accept(secondClient);

            assertThat(numberOfConnectedClients)
                    .hasSize(2)
                    .containsExactly(firstClient, secondClient);
        }

        @Test
        void shouldSendMessagesWhenTwoClientsJoins() {
            final var numberOfConnectedClients = new ArrayList<ConnectedClient>();
            final var firstClient = new TestConnectedClient("a");
            final var secondClient = new TestConnectedClient("b");
            final var lobby = Lobby.of(numberOfConnectedClients);

            lobby.accept(firstClient);
            lobby.accept(secondClient);

            assertThat(numberOfConnectedClients)
                    .hasSize(2)
                    .containsExactly(firstClient, secondClient);
        }
    }

    @Nested
    class AcceptingAndRemovingClients {
        @Test
        void shouldNotThrowExceptionWhenRemovingClientWhenClientIsNotInTheLobby() {
            final var numberOfConnectedClients = new ArrayList<ConnectedClient>();
            final var client = new TestConnectedClient("/lobby");
            final var lobby = Lobby.of(numberOfConnectedClients);

            lobby.dropConnectionFor(client);

            assertThat(numberOfConnectedClients).hasSize(0);
        }

        @Test
        void shouldNoOneBeInTheLobbyWhenOneJoinsAndLeave() {
            final var numberOfConnectedClients = new ArrayList<ConnectedClient>();
            final var client = new TestConnectedClient("a");
            final var lobby = Lobby.of(numberOfConnectedClients);

            lobby.accept(client);
            lobby.dropConnectionFor(client);

            assertThat(numberOfConnectedClients).hasSize(0);
        }
    }

    @Nested
    class PairingClients {
        @Test
        void shouldPairTwoClientsWhenRequested() {
            final var numberOfConnectedClients = new ArrayList<ConnectedClient>();
            final var firstClient = new TestConnectedClient("a");
            final var secondClient = new TestConnectedClient("b");
            final var lobby = Lobby.of(numberOfConnectedClients);
            lobby.accept(firstClient);
            lobby.accept(secondClient);

            lobby.pair(() -> "/endpoint", firstClient, secondClient);

            assertThat(numberOfConnectedClients).hasSize(0);
            assertThat(firstClient)
                    .extracting(TestConnectedClient::isClosed, as(InstanceOfAssertFactories.BOOLEAN)).isTrue();
            assertThat(secondClient)
                    .extracting(TestConnectedClient::isClosed, as(InstanceOfAssertFactories.BOOLEAN)).isTrue();
        }
    }
}