package com.github.lipinskipawel.server;

/**
 * This interface represents a connected client to the {@link FootballServer}.
 */
public interface ConnectedClient {

    /**
     * This method will send a message to the client represented by this class.
     */
    void send(final String message);

    /**
     * @return url which the client is connected to.
     */
    String getUrl();

    /**
     * This method close client connection to the {@link FootballServer}. Closing connection is not blocking current
     * thread.
     */
    void close();
}
