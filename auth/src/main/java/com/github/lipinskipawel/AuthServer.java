package com.github.lipinskipawel;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.net.SocketAddress;

/**
 * Simple wrapper class that can start the Netty HTTP server.
 */
public final class AuthServer {
    /**
     * This method will start the http server. This method is blocking.
     *
     * @param address to start the server on
     * @throws InterruptedException
     */
    public void startServer(final SocketAddress address) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap nettyServer = new ServerBootstrap();
            nettyServer
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .localAddress(address)
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new HttpServerInitializer());

            Channel ch = nettyServer.bind().sync().channel();

            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
