package com.github.lipinskipawel.server;

import java.util.ArrayList;
import java.util.List;

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
}
