package com.github.lipinskipawel.server;

import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ClientHandshake;

final class HandshakePolicy {

    /**
     * Defines the policy under the {@link FootballServer} should run. Allows connections to /chat/{id} endpoint and
     * prevents under any other endpoint.
     *
     * @param clientHandshake parameter which is given by the
     *                        {@link org.java_websocket.server.WebSocketServer#onWebsocketHandshakeReceivedAsServer}
     * @throws InvalidDataException is expected from the
     *                              {@link org.java_websocket.server.WebSocketServer#onWebsocketHandshakeReceivedAsServer}
     */
    static void webConnectionPolicy(final ClientHandshake clientHandshake) throws InvalidDataException {
        final var resourceDescriptor = clientHandshake.getResourceDescriptor();
        if (resourceDescriptor.equals("/chat") ||
                !resourceDescriptor.startsWith("/chat") ||
                resourceDescriptor.split("/").length > 3) {
            throw new InvalidDataException(
                    CloseFrame.POLICY_VALIDATION, "WebSocket connection is allowed only at /chat"
            );
        }
    }
}
