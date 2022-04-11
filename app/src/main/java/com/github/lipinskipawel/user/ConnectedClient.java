package com.github.lipinskipawel.user;

import com.github.lipinskipawel.server.FootballServer;

/**
 * This interface represents a connected client to the {@link FootballServer}.
 */
public interface ConnectedClient {

    /**
     * This method will send a message to the connected client.
     */
    void send(final String message);

    /**
     * @return url which the client is connected to.
     */
    String getUrl();

    /**
     * @return username which the client is identified as.
     */
    String getUsername();

    /**
     * This method close client connection to the {@link FootballServer}. Closing connection is not blocking current
     * thread.
     */
    void close();
}
