package com.github.lipinskipawel.server;

import org.java_websocket.WebSocket;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class TestConnectedClient implements ConnectedClient {
    private final String url;
    List<String> messages;

    TestConnectedClient(final String url) {
        this.url = url;
        this.messages = new ArrayList<>();
    }

    @Override
    public void send(String message) {
        this.messages.add(message);
    }

    @Override
    public String getUrl() {
        return this.url;
    }

    @Override
    public WebSocket getWebSocket() {
        return null;
    }

    public List<String> getMessages() {
        return new ArrayList<>(messages);
    }
}
