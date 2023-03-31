package com.github.lipinskipawel.domain.mocks;

import com.github.lipinskipawel.user.ConnectedClient;

import java.util.ArrayList;
import java.util.List;

public final class TestConnectedClient implements ConnectedClient {
    private final String username;
    private boolean isClosed;
    private final List<String> messages;

    public TestConnectedClient(final String username) {
        this.username = username;
        this.isClosed = false;
        this.messages = new ArrayList<>();
    }

    @Override
    public void send(Object objectToSend) {
        this.messages.add(objectToSend.toString());
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
