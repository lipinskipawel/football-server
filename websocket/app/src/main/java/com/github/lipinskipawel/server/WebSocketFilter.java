package com.github.lipinskipawel.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.Map;

/**
 * This class is responsible for closing any WebSocket upgrades that are not trying to connect under '/ws/...' endpoint.
 */
final class WebSocketFilter extends SimpleChannelInboundHandler<FullHttpRequest> {

    @Override
    public boolean acceptInboundMessage(Object msg) throws Exception {
        if (msg instanceof FullHttpRequest req) {
            final var uri = req.uri();
            final var headers = req.headers();
            return shouldFilter(headers, uri);
        }
        return super.acceptInboundMessage(msg);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        ctx.close().sync();
    }

    static boolean shouldFilter(Iterable<Map.Entry<String, String>> headers, String url) {
        for (var entry : headers) {
            if (entry.getKey().equals("Upgrade") &&
                entry.getValue().equals("websocket") &&
                !url.startsWith("/ws")) {
                return true;
            }
        }
        return false;
    }
}
