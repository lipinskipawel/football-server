package com.github.lipinskipawel.user;

/**
 * This interface represents a connected client to the Football Server.
 */
public interface ConnectedClient {

    /**
     * This method will send a message to the connected client.
     *
     * @param objectToSend to send
     */
    void send(Object objectToSend);

    /**
     * @return username which the client is identified as.
     */
    String getUsername();

    /**
     * This method close client connection to the Football Server. Closing connection is not blocking current
     * thread.
     */
    void close();
}
