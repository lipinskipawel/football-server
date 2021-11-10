package com.github.lipinskipawel.mocks;

import com.github.lipinskipawel.user.ConnectedClient;

import java.util.ArrayList;
import java.util.List;

public final class TestConnectedClient implements ConnectedClient {
    private final String url;
    private final String username;
    private boolean isClosed;
    private List<String> messages;

    public TestConnectedClient(final String url) {
        this(url, "");
    }

    public TestConnectedClient(final String url, final String username) {
        this.url = url;
        this.username = username;
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
    public String getUsername() {
        return this.username;
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
