package com.github.lipinskipawel.server;

import com.github.lipinskipawel.api.QueryRegister;
import com.github.lipinskipawel.domain.game.ActiveGames;
import com.github.lipinskipawel.domain.lobby.Lobby;
import com.github.lipinskipawel.user.ConnectedClientFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.net.InetSocketAddress;

/**
 * Simple wrapper class that can start the Netty WebSocket server.
 */
public final class WebSocketServer {
    private final NioEventLoopGroup boss;
    private final NioEventLoopGroup worker;
    private Channel channel;

    public WebSocketServer() {
        this.boss = new NioEventLoopGroup(1);
        this.worker = new NioEventLoopGroup();
    }

    /**
     * This method will start the websocket server. This method is blocking.
     *
     * @param address  to start the server on
     * @param register API from auth module
     */
    public void start(InetSocketAddress address, QueryRegister register) {
        try {
            final var nettyServer = new ServerBootstrap();
            nettyServer
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .localAddress(address)
                    .group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new WebSocketInitializer(
                            Lobby.of(),
                            new ConnectedClientFactory(register),
                            ActiveGames.of())
                    );

            this.channel = nettyServer.bind().sync().channel();

            this.channel.closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            stop();
        }
    }

    public void stop() {
        if (this.channel != null) {
            this.channel.close();
        }
        boss.shutdownGracefully();
        worker.shutdownGracefully();
    }
}
