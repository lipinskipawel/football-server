package com.github.lipinskipawel.server;

import com.github.lipinskipawel.api.Player;
import com.github.lipinskipawel.api.RequestToPlay;
import com.github.lipinskipawel.api.move.GameMove;
import com.github.lipinskipawel.domain.ActiveGames;
import com.github.lipinskipawel.user.ConnectedClient;
import com.google.gson.Gson;
import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshakeBuilder;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.net.InetSocketAddress;
import java.util.Optional;

import static com.github.lipinskipawel.server.HandshakePolicy.webConnectionPolicy;
import static com.github.lipinskipawel.user.ConnectedClient.findBy;
import static com.github.lipinskipawel.user.ConnectedClient.findByUsername;
import static com.github.lipinskipawel.user.ConnectedClient.from;
import static org.java_websocket.framing.CloseFrame.POLICY_VALIDATION;

public final class FootballServer extends WebSocketServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(FootballServer.class);
    private final Gson parser;
    private final Lobby lobby;
    private final ActiveGames activeGames;

    public FootballServer(final InetSocketAddress address) {
        super(address);
        this.parser = new Gson();
        this.lobby = Lobby.of(parser::toJson);
        this.activeGames = ActiveGames.of();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        final var url = conn.getResourceDescriptor();
        MDC.put("gameUrl", url);
        final var username = usernameFromCookie(handshake);
        final var optionalClient = from(conn, username);
        if (optionalClient.isPresent()) {
            final var client = optionalClient.get();
            MDC.put("ConnectedClientUsername", client.getUsername());
            LOGGER.info("Server onOpen");
            if (url.equals("/lobby")) {
                this.lobby.accept(client);
                return;
            }
            processGameConnection(conn, client);
        }
        if (optionalClient.isEmpty()) {
            final var message = "Server does not allow two different clients authenticate using the same connection";
            LOGGER.error(message);
            conn.closeConnection(POLICY_VALIDATION, message);
            MDC.clear();
        }
    }

    /**
     * This method performs username retrieval of the {@link WebSocket} connection.
     *
     * @param handshake that is used to retrieve username
     * @return unique identifier
     */
    private String usernameFromCookie(final ClientHandshake handshake) {
        final var cookie = handshake.getFieldValue("cookie");
        return cookie.equals("") ? "anonymous" : cookie;
    }

    private void processGameConnection(
            final WebSocket conn,
            final ConnectedClient client
    ) {
        final var url = conn.getResourceDescriptor();
        final var isAdded = this.activeGames.accept(url, client);
        if (!isAdded) {
            final var message = "Server does not allow connecting to the game.";
            conn.closeConnection(POLICY_VALIDATION, message);
            LOGGER.info(message);
            LOGGER.info("Connection has been closed");
            MDC.clear();
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        MDC.put("gameUrl", conn.getResourceDescriptor());
        findBy(conn)
                .ifPresent(client -> {
                    MDC.put("ConnectedClientUsername", client.getUsername());
                    this.activeGames.dropConnectionFor(conn.getResourceDescriptor(), client);
                    this.lobby.dropConnectionFor(client);
                });
        LOGGER.info("Server onClose: {}, reason: {}", code, reason);
        MDC.clear();
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        LOGGER.info("Server onMessage: {}", message);
        final var optionalClient = findBy(conn);
        if (optionalClient.isPresent()) {
            final var client = optionalClient.get();
            if (conn.getResourceDescriptor().equals("/lobby")) {
                parseRequestToPlay(message)
                        .flatMap(request -> findByUsername(request.getOpponent().getUsername()))
                        .ifPresentOrElse(opponent -> pairBothClients(client, opponent),
                                () -> closeWebSocketConnection(conn));
                return;
            }
            parseToGameMove(message)
                    .ifPresentOrElse(
                            move -> this.activeGames.registerMove(conn.getResourceDescriptor(), move, client),
                            () -> LOGGER.error("Can not parse given message to GameMove object. {}", message)
                    );
        }
        MDC.clear();
    }

    private Optional<RequestToPlay> parseRequestToPlay(final String json) {
        try {
            final var value = this.parser.fromJson(json, RequestToPlay.class);
            return Optional.of(RequestToPlay.with(Player.fromUsername(value.getOpponent().getUsername())));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private void pairBothClients(final ConnectedClient client, final ConnectedClient opponent) {
        final var urlForNewGame = this.activeGames.createNewGame(client, opponent);
        this.lobby.pair(() -> urlForNewGame, client, opponent);
    }

    private void closeWebSocketConnection(WebSocket conn) {
        final var message = "Server allows only RequestToPlay messages to be sent in the /lobby endpoint";
        LOGGER.error(message);
        conn.closeConnection(POLICY_VALIDATION, message);
        MDC.clear();
    }

    private Optional<GameMove> parseToGameMove(final String json) {
        try {
            final var value = this.parser.fromJson(json, GameMove.class);
            return GameMove.from(value.getMove());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        MDC.put("gameUrl", conn.getResourceDescriptor());
        LOGGER.error("Server onError: ", ex);
        MDC.clear();
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
