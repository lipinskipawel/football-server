package com.github.lipinskipawel.domain;

import com.github.lipinskipawel.user.ConnectedClient;
import com.github.lipinskipawel.util.ThreadSafe;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represent a DualConnection where two clients are connected to each other. Therefore, it manages number of
 * clients connected to the same endpoint.
 * This class is a helper class and used as a dependency for the {@link GameLifeCycle} class.
 */
@ThreadSafe
final class DualConnection {
    private final List<ConnectedClient> connectedClients;
    private final Object lock;

    DualConnection() {
        this.connectedClients = new ArrayList<>();
        this.lock = new Object();
    }

    boolean accept(final ConnectedClient client) {
        synchronized (lock) {
            if (connectedClients.size() < 2) {
                connectedClients.add(client);
                return true;
            } else {
                return false;
            }
        }
    }

    boolean areBothClientsConnected() {
        return connectedClients.size() == 2;
    }

    ConnectedClient first() {
        return connectedClients.get(0);
    }

    ConnectedClient second() {
        return connectedClients.get(1);
    }

    void sendMessageFrom(final String message, final ConnectedClient sender) {
        if (connectedClients.size() != 2) {
            return;
        }
        connectedClients
                .stream()
                .filter(client -> findSender(client, sender))
                .findFirst()
                .ifPresent(client -> client.send(message));
    }

    private boolean findSender(ConnectedClient client, ConnectedClient sender) {
        return !client.equals(sender);
    }

    void sendMessageTo(final String message, final ConnectedClient receiver) {
        if (connectedClients.size() != 2) {
            return;
        }
        receiver.send(message);
    }

    void dropConnectionFor(final ConnectedClient toLeave) {
        synchronized (lock) {
            final var removed = connectedClients.removeIf(client -> client.equals(toLeave));
            if (removed) {
                toLeave.close();
            }
        }
    }
}
