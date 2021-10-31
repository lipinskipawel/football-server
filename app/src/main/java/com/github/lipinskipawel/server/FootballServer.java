package com.github.lipinskipawel.server;

import com.github.lipinskipawel.api.RequestToPlay;
import com.github.lipinskipawel.api.move.GameMove;
import com.github.lipinskipawel.domain.GameLifeCycle;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshakeBuilder;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Optional;

import static com.github.lipinskipawel.server.HandshakePolicy.webConnectionPolicy;
import static com.github.lipinskipawel.user.ConnectedClient.findBy;
import static com.github.lipinskipawel.user.ConnectedClient.from;
import static org.java_websocket.framing.CloseFrame.POLICY_VALIDATION;

public final class FootballServer extends WebSocketServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(FootballServer.class);
    private final Gson parser;
    private final Lobby lobby;
    private final GameLifeCycle gameHandler;

    public FootballServer(final InetSocketAddress address) {
        super(address);
        this.parser = new Gson();
        this.lobby = Lobby.of(parser::toJson);
        this.gameHandler = GameLifeCycle.of(parser::toJson);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        final var url = conn.getResourceDescriptor();
        LOGGER.info("Server onOpen: {}", url);
        final var client = from(conn);
        if (url.equals("/lobby")) {
            this.lobby.accept(client);
            return;
        }
        final var isAdded = this.gameHandler.accept(client);
        if (!isAdded) {
            final var message = "Server does not allow more than 2 clients to connect to the same endpoint";
            conn.closeConnection(POLICY_VALIDATION, message);
            LOGGER.info(message);
            LOGGER.info("Connection has been closed");
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        LOGGER.info("Server onClose: {}, reason: {}", code, reason);
        final var client = from(conn);
        this.gameHandler.dropConnectionFor(client);
        this.lobby.dropConnectionFor(client);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        LOGGER.info("Server onMessage: {}", message);
        final var client = from(conn);
        if (conn.getResourceDescriptor().equals("/lobby")) {
            final var requestToPlay = this.parser.fromJson(message, RequestToPlay.class);
            final var optionalOpponentClient = findBy(requestToPlay.getOpponent());
            optionalOpponentClient.ifPresent(opponent -> {
                this.lobby.pair(() -> "/endpoint", client, opponent);
            });
            return;
        }
        parseToGameMove(message)
                .ifPresentOrElse(
                        move -> this.gameHandler.makeMove(move, client),
                        () -> LOGGER.error("Can not parse given message to GameMove object. {}", message)
                );
    }

    private Optional<GameMove> parseToGameMove(final String json) {
        try {
            final var value = this.parser.fromJson(json, GameMove.class);
            return GameMove.from(value.getMove());
        } catch (JsonSyntaxException exception) {
            return Optional.empty();
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        LOGGER.error("Server onError: ", ex);
    }

    @Override
    public void onStart() {
        LOGGER.info("Server onStart");
    }

    @Override
    public ServerHandshakeBuilder onWebsocketHandshakeReceivedAsServer(final WebSocket conn,
                                                                       final Draft draft,
                                                                       final ClientHandshake request)
            throws InvalidDataException {
        final var builder = super.onWebsocketHandshakeReceivedAsServer(conn, draft, request);
        webConnectionPolicy(request);
        return builder;
    }
}
