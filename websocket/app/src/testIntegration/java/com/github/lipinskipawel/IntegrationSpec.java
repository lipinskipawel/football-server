package com.github.lipinskipawel;

import com.github.lipinskipawel.api.PlayPairing;
import com.github.lipinskipawel.api.RequestToPlay;
import com.github.lipinskipawel.api.WaitingPlayers;
import com.github.lipinskipawel.client.StubAuthClient;
import com.github.lipinskipawel.server.WebSocketServer;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Optional;
import java.util.concurrent.Callable;

import static com.github.lipinskipawel.SimpleWebSocketClient.createClient;
import static java.time.Duration.ofSeconds;
import static org.awaitility.Awaitility.await;

public abstract class IntegrationSpec {

    public static final int PORT = findFreePort();
    private static final Gson parser = new Gson();

    private static final WebSocketServer server;

    public static final StubAuthClient authClient;

    static {
        authClient = new StubAuthClient();

        server = new WebSocketServer();
        server.start(new InetSocketAddress("localhost", PORT), authClient);
    }

    public static Optional<String> registerUsername(String username) {
        return authClient.register(username);
    }

    /**
     * This method returns array of two connected clients to the game endpoint. Those clients went through the process
     * of pairing using lobby endpoint.
     *
     * @param serverEndpoint must be ws://localhost:{PORT}/lobby
     * @return two connected clients
     */
    public static SimpleWebSocketClient[] getPairedClients(final String serverEndpoint)
            throws InterruptedException {
        registerUsername("firstClient");
        registerUsername("secondClient");
        final var result = new SimpleWebSocketClient[2];
        final var firstClient = createClient(serverEndpoint);
        final var secondClient = createClient(serverEndpoint);
        firstClient.addHeader("cookie", "firstClient_token");
        secondClient.addHeader("cookie", "secondClient_token");

        System.out.println("about to connect blocking");
        firstClient.connectBlocking();
        System.out.println("waiting for msg == 1");
        waitFor(() -> firstClient.getMessages().size() == 1);
        secondClient.connectBlocking();
        waitFor(() -> firstClient.getMessages().size() == 2);
        waitFor(() -> secondClient.getMessages().size() == 1);
        sendRequestToPlayFrom(firstClient);
        waitFor(() -> firstClient.getMessages().size() == 3);
        waitFor(() -> secondClient.getMessages().size() == 2);
        firstClient.closeBlocking();
        secondClient.closeBlocking();

        result[0] = createNewClient(firstClient, serverEndpoint);
        result[1] = createNewClient(secondClient, serverEndpoint);
        result[0].addHeader("cookie", "firstClient_token");
        result[1].addHeader("cookie", "secondClient_token");
        result[0].connectBlocking();
        result[1].connectBlocking();
        return result;
    }

    /**
     * Util method for awaiting for {@code condition}
     *
     * @param condition to await on
     */
    public static void waitFor(final Callable<Boolean> condition) {
        await().atMost(ofSeconds(1)).until(condition);
    }

    private static void sendRequestToPlayFrom(final SimpleWebSocketClient firstClient) {
        final var message = firstClient.getMessages().get(1);
        final var opponent = parser.fromJson(message, WaitingPlayers.class)
                .players()
                .get(1);
        final var requestToPlay = RequestToPlay.with(opponent);
        firstClient.send(parser.toJson(requestToPlay));
    }

    private static SimpleWebSocketClient createNewClient(SimpleWebSocketClient client, String serverUri) {
        final var redirectedServerUri = serverUri.replace("/ws/lobby", "");

        final var size = client.getMessages().size();
        final var playPairing = parser.fromJson(client.getMessages().get(size - 1), PlayPairing.class);
        return createClient(redirectedServerUri.concat(playPairing.getRedirectEndpoint()));
    }

    private static int findFreePort() {
        try (var server = new ServerSocket(0)) {
            return server.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
