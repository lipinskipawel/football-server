package com.github.lipinskipawel.client;

import com.github.lipinskipawel.api.PlayPairing;
import com.github.lipinskipawel.api.RequestToPlay;
import com.github.lipinskipawel.api.WaitingPlayers;
import com.google.gson.Gson;

import java.time.Duration;
import java.util.concurrent.Callable;

import static com.github.lipinskipawel.client.SimpleWebSocketClient.createClient;
import static org.awaitility.Awaitility.await;

public final class FootballClientCreator {
    private static final Gson parser = new Gson();

    private FootballClientCreator() {
    }

    /**
     * This method returns array of two connected clients to the game endpoint. Those clients went through the process
     * of pairing using lobby endpoint.
     *
     * @return two connected clients
     */
    public static SimpleWebSocketClient[] getPairedClients(final String serverUri) throws InterruptedException {
        final var result = new SimpleWebSocketClient[2];
        final var firstClient = createClient(serverUri);
        final var secondClient = createClient(serverUri);
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

        result[0] = createNewClient(firstClient, serverUri);
        result[1] = createNewClient(secondClient, serverUri);
        return result;
    }

    private static void waitFor(final Callable<Boolean> condition) {
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

    private static SimpleWebSocketClient createNewClient(SimpleWebSocketClient client, String serverUri)
            throws InterruptedException {
        final var redirectedServerUri = serverUri.replace("/lobby", "");

        final var size = client.getMessages().size();
        final var playPairingFirst = parser.fromJson(client.getMessages().get(size - 1), PlayPairing.class);
        final var newClient = createClient(redirectedServerUri.concat(playPairingFirst.getRedirectEndpoint()));
        newClient.connectBlocking();
        return newClient;
    }
}
