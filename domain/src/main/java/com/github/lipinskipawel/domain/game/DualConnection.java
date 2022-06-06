package com.github.lipinskipawel.domain.game;

import com.github.lipinskipawel.spi.Parser;
import com.github.lipinskipawel.user.ConnectedClient;
import com.github.lipinskipawel.util.ThreadSafe;

/**
 * This class represent a DualConnection where two clients are connected to each other. Therefore, it manages number of
 * clients connected to the same endpoint.
 * This class is a helper class and used as a dependency for the {@link GameLifeCycle} class.
 */
@ThreadSafe
final class DualConnection {
    private ConnectedClient first;
    private ConnectedClient second;
    private final Parser parser;

    DualConnection(final Parser parser) {
        this.parser = parser;
    }

    /**
     * This method stores the reference of {@link ConnectedClient} for later use such as sending messages or dropping
     * connection.
     * {@code DualConnection} can store only two references. {@code client} must not be null. In regard to the
     * {@code client} reference identity, it is possible to call this method twice with the same reference.
     *
     * @param client that is trying to be accepted
     * @return true or false, true only if {@code client} was stored successfully, otherwise false
     */
    boolean accept(final ConnectedClient client) {
        if (client == null) {
            return false;
        }
        synchronized (this) {
            if (first == null) {
                first = client;
                return true;
            } else if (second == null) {
                second = client;
                return true;
            }
        }
        return false;
    }

    /**
     * This method will send the {@code message} to the receiving side.
     *
     * @param message to be sent
     * @param sender  that is about ot send the message
     */
    void sendMessageFrom(final Object message, final ConnectedClient sender) {
        if (sender == null) {
            return;
        }
        synchronized (this) {
            final var client = sender.equals(first) ? second : first;
            if (client == null) {
                return;
            }
            client.send(parser.toJson(message));
        }
    }

    /**
     * This method will send the {@code message} to the receiver. Prior to sending message the {@code receiver} must be
     * accepted by the {@link DualConnection#accept(ConnectedClient)} method.
     *
     * @param message  to be sent
     * @param receiver that will receive the message
     */
    void sendMessageTo(final Object message, final ConnectedClient receiver) {
        if (receiver == null) {
            return;
        }
        synchronized (this) {
            if (receiver.equals(first) || receiver.equals(second)) {
                receiver.send(parser.toJson(message));
            }
        }
    }

    /**
     * This method will close the underlying connection and remove the {@code toLeave} from the {@code DualConnection}
     * storage.
     *
     * @param toLeave ConnectedClient that will be disconnected
     */
    void dropConnectionFor(final ConnectedClient toLeave) {
        if (toLeave == null) {
            return;
        }
        synchronized (this) {
            if (toLeave.equals(first)) {
                first.close();
                first = null;
            } else if (toLeave.equals(second)) {
                second.close();
                second = null;
            }
        }
    }

    /**
     * Checks whether given client is connected or not.
     *
     * @param client to check if it is connected
     * @return true or false, true only when connected
     */
    boolean isConnected(final ConnectedClient client) {
        synchronized (this) {
            return first == client || second == client;
        }
    }
}
