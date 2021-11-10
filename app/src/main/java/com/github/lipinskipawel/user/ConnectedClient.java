package com.github.lipinskipawel.user;

import com.github.lipinskipawel.server.FootballServer;
import org.java_websocket.WebSocket;

import java.util.Optional;

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

    /**
     * This method is a factory for {@link ConnectedClient} objects.
     *
     * <p>This method does not assume anything about authentication nor authorization therefore it will create a new
     * {@link ConnectedClient} always when given different {@code connection} object with the same {@code username}.
     *
     * <p>This method will not allow registering different usernames along with the same {@code connection}. It means
     * that invoked twice with the same {@code connection} but different username will result in
     * {@link Optional#empty()}. @implNote Given aforementioned description the previous connection will <b>NOT</b> be
     * closed.
     *
     * @param connection WebSocket connection
     * @param username   that should be associated with the {@code connection}
     * @return instance of {@link ConnectedClient}
     */
    static Optional<ConnectedClient> from(final WebSocket connection, final String username) {
        return MinimalisticClientContext.from(connection, username);
    }

    static Optional<ConnectedClient> findBy(final WebSocket connection) {
        return MinimalisticClientContext.findBy(connection);
    }

    static Optional<ConnectedClient> findByUsername(final String username) {
        return MinimalisticClientContext.findByUsername(username);
    }
}
