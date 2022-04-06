package com.github.lipinskipawel;

import com.github.lipinskipawel.register.RegisterEntrypoint;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

final class HttpServerInitializer extends ChannelInitializer<SocketChannel> {

    public HttpServerInitializer() {
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new HttpServerCodec());
        p.addLast(new HttpObjectAggregator(4096));
        p.addLast(RegisterEntrypoint.getRegisterHandler());
        p.addLast(new ClosingHandler());
    }
}
