package com.github.lipinskipawel.server;

import java.util.ArrayList;
import java.util.List;

final class Lobby {
    private final List<ConnectedClient> connectedClients;

    public Lobby(final List<ConnectedClient> connectedClients) {
        this.connectedClients = connectedClients;
    }

    List<ConnectedClient> accept(final ConnectedClient client) {
        this.connectedClients.add(client);
        return new ArrayList<>(this.connectedClients);
    }

    void dropConnectionFor(final ConnectedClient leaveLobby) {
        this.connectedClients.removeIf(it -> it.equals(leaveLobby));
    }
}
