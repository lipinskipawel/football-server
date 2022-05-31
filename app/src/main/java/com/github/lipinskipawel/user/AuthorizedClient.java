package com.github.lipinskipawel.user;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

final class AuthorizedClient implements ConnectedClient {
    private final Channel channel;
    private final String username;

    public AuthorizedClient(Channel channel, String username) {
        this.channel = channel;
        this.username = username;
    }

    @Override
    public void send(String message) {
        try {
            final var frame = new TextWebSocketFrame(message);
            this.channel.writeAndFlush(frame).sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public void close() {
        this.channel.close();
    }
}
