package com.github.lipinskipawel.server;

import java.util.ArrayList;
import java.util.List;

final class TestConnectedClient implements ConnectedClient {
    private final String url;
    private boolean isClosed;
    List<String> messages;

    TestConnectedClient(final String url) {
        this.url = url;
        this.isClosed = false;
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
    public void close() {
        this.isClosed = true;
    }

    public List<String> getMessages() {
        return new ArrayList<>(messages);
    }

    public boolean isClosed() {
        return this.isClosed;
    }
}
