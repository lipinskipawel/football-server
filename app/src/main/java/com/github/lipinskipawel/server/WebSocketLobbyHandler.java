package com.github.lipinskipawel.server;

import com.github.lipinskipawel.api.Player;
import com.github.lipinskipawel.api.RequestToPlay;
import com.github.lipinskipawel.domain.ActiveGames;
import com.github.lipinskipawel.user.ConnectedClient;
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

final class WebSocketLobbyHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private static final Logger log = LoggerFactory.getLogger(WebSocketLobbyHandler.class);
    private static final Gson parser = new Gson();
    private final Lobby lobby;
    private final ConnectedClientFactory factory;
    private final ActiveGames activeGames;

    WebSocketLobbyHandler(final Lobby lobby, final ActiveGames activeGames, final ConnectedClientFactory factory) {
        this.lobby = lobby;
        this.factory = factory;
        this.activeGames = activeGames;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) {
        if (msg instanceof TextWebSocketFrame frame) {
            String text = frame.text();

            final var optionalClient = factory.findBy(ctx.channel());
            if (optionalClient.isPresent()) {
                final var client = optionalClient.get();

                parseRequestToPlay(text)
                        .flatMap(request -> factory.findByUsername(request.getOpponent().getUsername()))
                        .ifPresentOrElse(
                                opponent -> pairBothClients(client, opponent),
                                () -> onNotRequestToPlay(ctx, client, frame)
                        );
            }
        }
    }

    private Optional<RequestToPlay> parseRequestToPlay(final String json) {
        try {
            final var value = parser.fromJson(json, RequestToPlay.class);
            return Optional.of(RequestToPlay.with(Player.fromUsername(value.getOpponent().getUsername())));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private void pairBothClients(final ConnectedClient client, final ConnectedClient opponent) {
        final var urlForNewGame = this.activeGames.createNewGame(client, opponent);
        this.lobby.pair(() -> urlForNewGame, client, opponent);
    }

    private void onNotRequestToPlay(
            final ChannelHandlerContext ctx,
            final ConnectedClient client,
            final TextWebSocketFrame frame) {
        if (this.lobby.isInLobby(client)) {
            log.info("Server allows RequestToPlay. Server received: " + frame.text());
            log.info("Server closes connection.");
            ctx.close();
        } else {
            ctx.fireChannelRead(frame);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        factory.findBy(ctx.channel()).ifPresent(this.lobby::dropConnectionFor);
        super.channelInactive(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof final WebSocketServerProtocolHandler.HandshakeComplete cast) {
            if (cast.requestUri().equals("/ws/lobby")) {
                final var username = cast.requestHeaders().get("cookie");

                try {
                    final var client = factory.from(ctx.channel(), username);
                    this.lobby.accept(client);
                } catch (RuntimeException e) {
                    if ("Already authenticated".equals(e.getMessage())) {
                        // you can not join lobby twice or playing other game
                        ctx.close().sync();
                    }
                }
            } else {
                super.userEventTriggered(ctx, evt);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
