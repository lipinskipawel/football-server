package com.github.lipinskipawel.server;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

class LobbyTest implements WithAssertions {
    private static final Consumer<String> NO_OP = data -> {
    };

    @Nested
    class AcceptingClients {
        @Test
        void shouldAcceptOneClient() {
            final var list = new ArrayList<ConnectedClient>();
            final var client = new TestConnectedClient("/lobby");
            final var lobby = Lobby.notThreadSafe(list, Objects::toString);

            lobby.accept(client, NO_OP);

            assertThat(list)
                    .hasSize(1)
                    .containsExactly(client);
        }

        @Test
        void shouldAcceptTwoClients() {
            final var numberOfConnectedClients = new ArrayList<ConnectedClient>();
            final var firstClient = new TestConnectedClient("/lobby");
            final var secondClient = new TestConnectedClient("/lobby");
            final var lobby = Lobby.notThreadSafe(numberOfConnectedClients, Objects::toString);

            lobby.accept(firstClient, NO_OP);
            lobby.accept(secondClient, NO_OP);

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
            final var lobby = Lobby.notThreadSafe(numberOfConnectedClients, Objects::toString);

            lobby.dropConnectionFor(client);

            assertThat(numberOfConnectedClients).hasSize(0);
        }

        @Test
        void shouldNoOneBeInTheLobbyWhenOneJoinsAndLeave() {
            final var numberOfConnectedClients = new ArrayList<ConnectedClient>();
            final var client = new TestConnectedClient("/lobby");
            final var lobby = Lobby.notThreadSafe(numberOfConnectedClients, Objects::toString);

            lobby.accept(client, NO_OP);
            lobby.dropConnectionFor(client);

            assertThat(numberOfConnectedClients).hasSize(0);
        }
    }
}