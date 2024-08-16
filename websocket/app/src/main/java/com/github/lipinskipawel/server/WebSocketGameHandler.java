package com.github.lipinskipawel.server;

import com.github.lipinskipawel.api.move.GameMove;
import com.github.lipinskipawel.domain.game.ActiveGames;
import com.github.lipinskipawel.user.ConnectedClientFactory;
import com.google.gson.Gson;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

final class WebSocketGameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private static final Logger log = LoggerFactory.getLogger(WebSocketGameHandler.class);
    private static final Gson parser = new Gson();
    private final ActiveGames activeGames;
    private final ConnectedClientFactory factory;

    WebSocketGameHandler(ActiveGames games, ConnectedClientFactory factory) {
        this.activeGames = games;
        this.factory = factory;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) {
        if (msg instanceof TextWebSocketFrame frame) {
            final var text = frame.text();
            final var optionalClient = factory.findBy(ctx.channel());
            if (optionalClient.isPresent()) {
                final var client = optionalClient.get();

                parseToGameMove(text)
                    .ifPresentOrElse(
                        move -> this.activeGames.registerMove(move, client),
                        () -> log.info("Can not parse given message to GameMove object. " + text)
                    );
            }
        }
    }

    private Optional<GameMove> parseToGameMove(final String json) {
        try {
            final var value = parser.fromJson(json, GameMove.class);
            return GameMove.from(value.getMove());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof final WebSocketServerProtocolHandler.HandshakeComplete cast) {
            if (cast.requestUri().startsWith("/ws/game/")) {
                final var client = factory.from(ctx.channel(), cast.requestHeaders().get("cookie"));
                final var uri = cast.requestUri();
                final var isAdded = this.activeGames.accept(uri, client);
                if (!isAdded) {
                    log.info("Server does not allow connecting to the game.");
                    ctx.close();
                }
            } else {
                // handling requests that do not meet /ws/lobby and /ws/game but matches /ws/...
                log.info("Server closing connection at " + cast.requestUri());
                ctx.close();
            }
        }
    }
}
