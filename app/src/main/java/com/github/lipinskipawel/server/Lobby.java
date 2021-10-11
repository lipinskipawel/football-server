package com.github.lipinskipawel.server;

import com.github.lipinskipawel.api.PlayPairing;
import com.github.lipinskipawel.api.Player;
import com.github.lipinskipawel.api.WaitingPlayers;
import com.github.lipinskipawel.util.ThreadSafe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * This class represent a Lobby where multiple clients are connected to the same resource. Therefore, it manages number
 * of clients connected to the lobby. It provides features such as:
 * - adding new client to the lobby
 * - removing client from the lobby
 * - pairing two clients together
 * This class is a helper class and used as a dependency for the {@link FootballServer} class.
 */
@ThreadSafe
final class Lobby {
    private final List<ConnectedClient> connectedClients;
    private final ReentrantLock lock;
    private final Parser parser;

    private Lobby(final List<ConnectedClient> connectedClients,
                  final Parser parser) {
        this.connectedClients = connectedClients;
        this.lock = new ReentrantLock();
        this.parser = parser;
    }

    static Lobby of(final Parser parser) {
        return new Lobby(new ArrayList<>(), parser);
    }

    static Lobby of(final List<ConnectedClient> connectedClients, final Parser parser) {
        return new Lobby(connectedClients, parser);
    }

    void accept(final ConnectedClient client) {
        executesUnderLock(() -> {
            this.connectedClients.add(client);
            final var players = this.connectedClients
                    .stream()
                    .map(it -> Player.fromUrl(it.getUrl()))
                    .collect(Collectors.toList());
            final var dataToSend = this.parser.toJson(WaitingPlayers.fromPlayers(players));
            this.connectedClients.forEach(it -> it.send(dataToSend));
        });
    }

    private void executesUnderLock(final Runnable code) {
        this.lock.lock();
        try {
            code.run();
        } finally {
            this.lock.unlock();
        }
    }

    void dropConnectionFor(final ConnectedClient leaveLobby) {
        executesUnderLock(() -> this.connectedClients.removeIf(it -> it.equals(leaveLobby)));
    }

    private <T> T executesUnderLock(final Supplier<T> code) {
        this.lock.lock();
        try {
            return code.get();
        } finally {
            this.lock.unlock();
        }
    }

    void pair(final Supplier<String> endpoint, final ConnectedClient first, final ConnectedClient second) {
        final boolean areBothInLobby = executesUnderLock(() -> {
            final var areBothInTheLobby = checkWhetherBothAreInLobby(first, second);
            if (areBothInTheLobby) {
                this.connectedClients.remove(first);
                this.connectedClients.remove(second);
                return true;
            } else {
                return false;
            }
        });
        if (areBothInLobby) {
            final var reply = PlayPairing
                    .aPlayPairing()
                    .withRedirectEndpoint(endpoint.get())
                    .withFirst(Player.fromUrl(first.getUrl()))
                    .withSecond(Player.fromUrl(second.getUrl()))
                    .build();
            first.send(this.parser.toJson(reply));
            second.send(this.parser.toJson(reply));
            first.close();
            second.close();
        }
    }

    private boolean checkWhetherBothAreInLobby(final ConnectedClient first, final ConnectedClient second) {
        return connectedClients.contains(first) && connectedClients.contains(second);
    }
}
