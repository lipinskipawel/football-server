package com.github.lipinskipawel;

import com.github.lipinskipawel.user.ConnectedClient;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is a helper class for integration tests.
 * It connects to server using {@link org.java_websocket.client.WebSocketClient}.
 * This class keeps track of all messages received through WebSocket.
 */
public final class SimpleWebSocketClient extends WebSocketClient implements ConnectedClient {
    private final List<String> messages;
    private final List<String> close;

    private SimpleWebSocketClient(final URI uri) {
        super(uri);
        this.messages = new ArrayList<>();
        this.close = new ArrayList<>();
    }

    public static SimpleWebSocketClient createClient(String endpoint) {
        return new SimpleWebSocketClient(URI.create(endpoint));
    }

    public List<String> getMessages() {
        return this.messages;
    }

    public List<String> getClose() {
        return this.close;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
    }

    @Override
    public void onMessage(String message) {
        this.messages.add(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        this.close.add(reason);
    }

    @Override
    public void onError(Exception ex) {
    }

    @Override
    public void send(Object objectToSend) {
        send(objectToSend.toString());
    }

    @Override
    public String getUsername() {
        return "no username";
    }
}
