package com.github.lipinskipawel.server;

import com.github.lipinskipawel.util.ThreadSafe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class represent a DualConnection where two clients are connected to each other. Therefore, it manages number of
 * clients connected to the same endpoint.
 * This class is a helper class and used as a dependency for the {@link FootballServer} class.
 */
@ThreadSafe
public final class DualConnection {
    private final Map<String, List<ConnectedClient>> clientsPerUrl;
    private final Object lock;

    public DualConnection() {
        this.clientsPerUrl = new HashMap<>();
        this.lock = new Object();
    }

    boolean accept(final ConnectedClient client) {
        synchronized (lock) {
            var connectedClients = clientsPerUrl.computeIfAbsent(client.getUrl(), url -> new ArrayList<>());
            if (connectedClients.size() < 2) {
                connectedClients.add(client);
                clientsPerUrl.put(client.getUrl(), connectedClients);
                return true;
            } else {
                return false;
            }
        }
    }

    void sendMessageTo(final String message, final ConnectedClient receiver) {
        final var connectedClients = clientsPerUrl.get(receiver.getUrl());
        if (connectedClients.size() != 2) {
            return;
        }
        connectedClients
                .stream()
                .filter(client -> findSender(client, receiver))
                .findFirst()
                .ifPresent(client -> client.send(message));
    }

    private boolean findSender(ConnectedClient client, ConnectedClient receiver) {
        return !client.equals(receiver);
    }

    List<ConnectedClient> nonePairClients() {
        synchronized (lock) {
            return clientsPerUrl
                    .values()
                    .stream()
                    .filter(this::pickOnlyClientsWithoutSecondConnectedClient)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        }
    }

    private boolean pickOnlyClientsWithoutSecondConnectedClient(List<ConnectedClient> connectedClients) {
        return connectedClients.size() == 1;
    }

    void dropConnectionFor(final ConnectedClient toLeave) {
        synchronized (lock) {
            clientsPerUrl.get(toLeave.getUrl()).removeIf(client -> client.equals(toLeave));
        }
    }
}
