package com.github.lipinskipawel.user;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

final class AuthorizedClient implements ConnectedClient {
    private final Channel channel;
    private final String username;
    private final Parser parser;

    public AuthorizedClient(Channel channel, String username, Parser parser) {
        this.channel = channel;
        this.username = username;
        this.parser = parser;
    }

    @Override
    public void send(Object objectToSend) {
        try {
            final var message = parser.toJson(objectToSend);
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
