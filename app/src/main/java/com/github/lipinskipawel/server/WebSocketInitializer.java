package com.github.lipinskipawel.server;

import com.github.lipinskipawel.domain.ActiveGames;
import com.github.lipinskipawel.user.ConnectedClientFactory;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolConfig;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;

final class WebSocketInitializer extends ChannelInitializer<SocketChannel> {
    private final Lobby lobby;
    private final ConnectedClientFactory factory;
    private final ActiveGames activeGames;

    WebSocketInitializer(final Lobby lobby, final ConnectedClientFactory factory, final ActiveGames activeGames) {
        this.lobby = lobby;
        this.factory = factory;
        this.activeGames = activeGames;
    }

    public static WebSocketInitializer initForTest(Lobby lobby, ConnectedClientFactory factory, ActiveGames activeGames) {
        return new WebSocketInitializer(lobby, factory, activeGames);
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new HttpServerCodec());
        p.addLast(new HttpObjectAggregator(8192));
        p.addLast(new WebSocketFilter());
        p.addLast(new WebSocketServerCompressionHandler());
        final var webSocketConfig = WebSocketServerProtocolConfig
                .newBuilder()
                .websocketPath("/ws")
                .subprotocols(null)
                .allowExtensions(true)
                .checkStartsWith(true)
                .build();
        p.addLast(new WebSocketServerProtocolHandler(webSocketConfig));
        p.addLast(new WebSocketLobbyHandler(lobby, activeGames, factory));
        p.addLast(new WebSocketGameHandler(activeGames, factory));
    }
}
