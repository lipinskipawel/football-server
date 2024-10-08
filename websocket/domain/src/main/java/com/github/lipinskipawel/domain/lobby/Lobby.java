package com.github.lipinskipawel.domain.lobby;

import com.github.lipinskipawel.api.PlayPairing;
import com.github.lipinskipawel.api.Player;
import com.github.lipinskipawel.api.WaitingPlayers;
import com.github.lipinskipawel.user.ConnectedClient;
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
 */
@ThreadSafe
public final class Lobby {
    private final List<ConnectedClient> connectedClients;
    private final ReentrantLock lock;

    private Lobby(final List<ConnectedClient> connectedClients) {
        this.connectedClients = connectedClients;
        this.lock = new ReentrantLock();
    }

    /**
     * Public factory method for creating new object of {@link Lobby}.
     *
     * @return lobby object
     */
    public static Lobby of() {
        return new Lobby(new ArrayList<>());
    }

    static Lobby of(final List<ConnectedClient> connectedClients) {
        return new Lobby(connectedClients);
    }

    public void accept(final ConnectedClient client) {
        executesUnderLock(() -> {
            this.connectedClients.add(client);
            sendWaitingPlayersToAllInTheLobby();
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

    private void sendWaitingPlayersToAllInTheLobby() {
        final var players = this.connectedClients
            .stream()
            .map(it -> Player.fromUsername(it.getUsername()))
            .collect(Collectors.toList());
        this.connectedClients.forEach(it -> it.send(WaitingPlayers.fromPlayers(players)));
    }

    public void dropConnectionFor(final ConnectedClient leaveLobby) {
        executesUnderLock(() -> {
            if (this.connectedClients.contains(leaveLobby)) {
                this.connectedClients.remove(leaveLobby);
                leaveLobby.close();
                sendWaitingPlayersToAllInTheLobby();
            }
        });
    }

    private <T> T executesUnderLock(final Supplier<T> code) {
        this.lock.lock();
        try {
            return code.get();
        } finally {
            this.lock.unlock();
        }
    }

    public void pair(final Supplier<String> endpoint, final ConnectedClient first, final ConnectedClient second) {
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
                .withFirst(Player.fromUsername(first.getUsername()))
                .withSecond(Player.fromUsername(second.getUsername()))
                .build();
            first.send(reply);
            second.send(reply);
            first.close();
            second.close();
        }
    }

    private boolean checkWhetherBothAreInLobby(final ConnectedClient first, final ConnectedClient second) {
        return connectedClients.contains(first) && connectedClients.contains(second);
    }

    public boolean isInLobby(final ConnectedClient client) {
        return connectedClients.contains(client);
    }
}
