package com.github.lipinskipawel.register;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

/**
 * This class is an integration point with Netty.
 * This is a {@link io.netty.channel.ChannelInboundHandler} that servers the requests at /register endpoint.
 * <p>
 * It will check all the HTTP parameters that this class take care of like:
 * - does method is HTTP POST
 * - does uri is equal to '/register'
 * <p>
 * This class will provide a http response after successfully registering username along with the header containing the
 * token associated with this username. This handler will <strong>NOT</strong> close the connection after that (see
 * {@link io.netty.util.ReferenceCounted#retain()} call).
 * <p>
 * Any unsuccessful registration of the username will be met with http unauthorized response and closing the
 * {@link io.netty.channel.Channel}.
 */
final class RegisterHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final Register register;

    RegisterHandler(Register register) {
        super();
        this.register = register;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) {
        if (isNotPostOnRegisterURI(msg)) {
            return;
        }

        final var username = msg.headers().get("username");
        final var isRegistered = register.register(username);
        if (!isRegistered) {
            sendUnauthorizedAndCloseChannel(ctx);
            return;
        }

        final var token = register.getTokenForUsername(username);
        ctx.writeAndFlush(buildOKResponse(token));
        msg.retain();
    }

    private boolean isNotPostOnRegisterURI(final FullHttpRequest msg) {
        return !msg.method().equals(HttpMethod.POST) || !msg.uri().equals("/register");
    }

    private void sendUnauthorizedAndCloseChannel(final ChannelHandlerContext ctx) {
        ctx.writeAndFlush(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.UNAUTHORIZED))
                .addListener(ChannelFutureListener.CLOSE);
    }

    private HttpResponse buildOKResponse(final String token) {
        final var response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().add("token", token);
        return response;
    }
}
