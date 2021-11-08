package com.github.lipinskipawel.client;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * This class is a helper class for integration tests.
 * It connects to server using {@link org.java_websocket.client.WebSocketClient}.
 * This class keeps track of all messages received on WebSocket.
 * This class uses {@link CountDownLatch} in onMessage and onClose methods as a synchronization mechanism.
 */
public final class SimpleWebSocketClient extends WebSocketClient {
    private final List<String> messages;
    private final CountDownLatch onClose;
    private CountDownLatch onMessage;

    private SimpleWebSocketClient(final URI uri, final CountDownLatch onMessage, final CountDownLatch onClose) {
        super(uri);
        this.messages = new ArrayList<>();
        this.onClose = onClose;
        this.onMessage = onMessage;
    }

    public static SimpleWebSocketClient createClient(String endpoint) {
        return createClient(endpoint, new CountDownLatch(0));
    }

    public static SimpleWebSocketClient createClient(String endpoint, CountDownLatch onMessage) {
        return createClient(endpoint, onMessage, new CountDownLatch(0));
    }

    public static SimpleWebSocketClient createClient(String endpoint, CountDownLatch onMessage, CountDownLatch onClose) {
        return new SimpleWebSocketClient(URI.create(endpoint), onMessage, onClose);
    }

    public List<String> getMessages() {
        return this.messages;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {

    }

    @Override
    public void onMessage(String message) {
        this.messages.add(message);
        this.onMessage.countDown();
    }

    public void latchOnMessage(final CountDownLatch onMessage) {
        this.onMessage = onMessage;
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        this.onClose.countDown();
    }

    @Override
    public void onError(Exception ex) {

    }
}
