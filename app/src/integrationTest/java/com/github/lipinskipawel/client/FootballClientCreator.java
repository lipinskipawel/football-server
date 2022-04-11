package com.github.lipinskipawel.client;

import com.github.lipinskipawel.api.PlayPairing;
import com.github.lipinskipawel.api.RequestToPlay;
import com.github.lipinskipawel.api.WaitingPlayers;
import com.github.lipinskipawel.extension.AuthModuleFacade;
import com.google.gson.Gson;

import java.time.Duration;
import java.util.concurrent.Callable;

import static com.github.lipinskipawel.client.SimpleWebSocketClient.createClient;
import static org.awaitility.Awaitility.await;

/**
 * This is a helper class for all the integration tests.
 */
public final class FootballClientCreator {
    private static final Gson parser = new Gson();

    private FootballClientCreator() {
    }

    /**
     * This method returns array of two connected clients to the game endpoint. Those clients went through the process
     * of pairing using lobby endpoint.
     *
     * @param serverEndpoint must be ws://localhost:{PORT}/lobby
     * @return two connected clients
     */
    public static SimpleWebSocketClient[] getPairedClients(final String serverEndpoint, final AuthModuleFacade register)
            throws InterruptedException {
        register.register("firstClient", "firstClient");
        register.register("secondClient", "secondClient");
        final var result = new SimpleWebSocketClient[2];
        final var firstClient = createClient(serverEndpoint);
        final var secondClient = createClient(serverEndpoint);
        firstClient.addHeader("cookie", "firstClient");
        secondClient.addHeader("cookie", "secondClient");

        firstClient.connectBlocking();
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
        result[0].addHeader("cookie", "firstClient");
        result[1].addHeader("cookie", "secondClient");
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
        await().atMost(Duration.ofSeconds(1)).until(condition);
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
        final var redirectedServerUri = serverUri.replace("/lobby", "");

        final var size = client.getMessages().size();
        final var playPairing = parser.fromJson(client.getMessages().get(size - 1), PlayPairing.class);
        return createClient(redirectedServerUri.concat(playPairing.getRedirectEndpoint()));
    }
}
