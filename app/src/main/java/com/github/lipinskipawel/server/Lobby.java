package com.github.lipinskipawel.server;

import com.github.lipinskipawel.api.Player;
import com.github.lipinskipawel.api.WaitingPlayers;
import com.github.lipinskipawel.util.ThreadSafe;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * This class represent a Lobby where multiple clients are connected to the same resource. Therefore, it manages number
 * of clients connected to the lobby. It provides features such as:
 * - adding new client to the lobby
 * - removing client from the lobby
 * This class is a helper class and used as a dependency for the {@link FootballServer} class.
 */
@ThreadSafe
final class Lobby {
    private final List<ConnectedClient> connectedClients;
    private final Parser<WaitingPlayers> parser;

    static Lobby of(final Parser<WaitingPlayers> parser) {
        return new Lobby(new CopyOnWriteArrayList<>(), parser);
    }

    private Lobby(final List<ConnectedClient> connectedClients,
                  final Parser<WaitingPlayers> parser) {
        this.connectedClients = connectedClients;
        this.parser = parser;
    }

    static Lobby notThreadSafe(final List<ConnectedClient> connectedClients, final Parser<WaitingPlayers> parser) {
        return new Lobby(connectedClients, parser);
    }

    void accept(final ConnectedClient client, Consumer<String> consumer) {
        this.connectedClients.add(client);
        final var players = this.connectedClients
                .stream()
                .map(it -> Player.fromUrl(it.getUrl()))
                .collect(Collectors.toList());
        final var dataToSend = this.parser.toJson(WaitingPlayers.fromPlayers(players));
        consumer.accept(dataToSend);
    }

    void dropConnectionFor(final ConnectedClient leaveLobby) {
        this.connectedClients.removeIf(it -> it.equals(leaveLobby));
    }
}
