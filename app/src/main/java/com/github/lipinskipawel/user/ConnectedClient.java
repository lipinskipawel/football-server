package com.github.lipinskipawel.user;

import com.github.lipinskipawel.server.WebSocketServer;

/**
 * This interface represents a connected client to the {@link WebSocketServer}.
 */
public interface ConnectedClient {

    /**
     * This method will send a message to the connected client.
     */
    void send(final String message);

    /**
     * @return username which the client is identified as.
     */
    String getUsername();

    /**
     * This method close client connection to the {@link WebSocketServer}. Closing connection is not blocking current
     * thread.
     */
    void close();
}
