package com.github.lipinskipawel.server;

import com.github.lipinskipawel.api.Player;
import com.github.lipinskipawel.api.WaitingPlayers;
import com.google.gson.Gson;
import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshakeBuilder;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.github.lipinskipawel.server.HandshakePolicy.webConnectionPolicy;
import static com.github.lipinskipawel.server.MinimalisticClientContext.createMinimalisticClientContext;
import static java.util.stream.Collectors.toList;
import static org.java_websocket.framing.CloseFrame.POLICY_VALIDATION;

public final class FootballServer extends WebSocketServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(FootballServer.class);
    private final Gson parser;
    private final DualConnection dualConnection;
    private final Lobby lobby;

    public FootballServer(final InetSocketAddress address, final DualConnection dualConnection) {
        super(address);
        this.dualConnection = dualConnection;
        this.parser = new Gson();
        this.lobby = new Lobby(new CopyOnWriteArrayList<>());
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        final var url = conn.getResourceDescriptor();
        LOGGER.info("Server onOpen: {}", url);
        final var client = createMinimalisticClientContext(conn);
        if (url.equals("/lobby")) {
            final var clients = this.lobby.accept(client);
            final var webSocketClients = clients.stream().map(ConnectedClient::getWebSocket).collect(toList());
            final var clientsToPlayers = clients.stream().map(it -> Player.fromUrl(it.getUrl())).collect(toList());
            final var messageToBroadcast = this.parser.toJson(WaitingPlayers.fromPlayers(clientsToPlayers));
            broadcast(messageToBroadcast, webSocketClients);
            return;
        }
        final var isAdded = this.dualConnection.accept(client);
        if (!isAdded) {
            final var message = "Server does not allow more than 2 clients to connect to the same endpoint";
            conn.closeConnection(POLICY_VALIDATION, message);
            LOGGER.info(message);
            LOGGER.info("Connection has been closed");
        }
        sendPlayerJoinedMessageToAllWaitingClients();
    }

    private void sendPlayerJoinedMessageToAllWaitingClients() {
        System.out.println("Will no execute");
        final var nonePairClients = dualConnection
                .nonePairClients();
        final var waitingPlayers = WaitingPlayers.fromPlayers(nonePairClients
                .stream()
                .map(ConnectedClient::getUrl)
                .map(Player::fromUrl)
                .collect(toList()));

        nonePairClients.forEach(it -> it.send(this.parser.toJson(waitingPlayers)));
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        LOGGER.info("Server onClose: {}, reason: {}", code, reason);
        final var client = createMinimalisticClientContext(conn);
        this.dualConnection.dropConnectionFor(client);
        this.lobby.dropConnectionFor(client);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        LOGGER.info("Server onMessage: {}", message);
        final var client = createMinimalisticClientContext(conn);
        this.dualConnection.sendMessageTo(message, client);
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
