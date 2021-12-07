package com.github.lipinskipawel.server;

import com.github.lipinskipawel.api.Player;
import com.github.lipinskipawel.client.SimpleWebSocketClient;
import com.github.lipinskipawel.extension.Application;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static com.github.lipinskipawel.client.FootballClientCreator.getPairedClients;
import static com.github.lipinskipawel.client.FootballClientCreator.waitFor;
import static com.github.lipinskipawel.client.SimpleWebSocketClient.createClient;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Application(port = FootballServerIT.PORT)
final class FootballServerIT {
    private static final Parser parser = new Gson()::toJson;
    static final int PORT = 8090;
    static final String SERVER_URI = "ws://localhost:%d".formatted(PORT);

    @Test
    void shouldRejectClientWhenURIDoNotMeetPolicy() throws InterruptedException {
        final var client = createClient(SERVER_URI.concat("/example"));

        client.connectBlocking();

        final var connectionClose = client.isOpen();
        client.closeBlocking();
        assertThat(connectionClose).isFalse();
    }

    @Test
    void shouldCloseConnectionWhenUnexpectedMessageArrives() throws InterruptedException {
        final var client = createClient(SERVER_URI.concat("/lobby"));
        client.connectBlocking();
        final var player = Player.fromUsername("example");

        client.send(parser.toJson(player));

        waitFor(() -> client.getClose().size() == 1);
        final var isOpen = client.isOpen();
        client.closeBlocking();
        assertThat(isOpen).isFalse();
    }

    @Test
    void shouldClientNotReceivedMessageWhenNotAGameMove() throws InterruptedException {
        final var pairedClients = getPairedClients(SERVER_URI.concat("/lobby"));

        pairedClients[0].send("msg");

        pairedClients[0].closeBlocking();
        pairedClients[1].closeBlocking();
        assertThat(pairedClients[1])
                .extracting(SimpleWebSocketClient::getMessages)
                .asList()
                .hasSize(0);
    }

    @Test
    void shouldAllowOnlyTwoClientsConnectToTheSameEndpoint() throws InterruptedException {
        final var pairedClients = getPairedClients(SERVER_URI.concat("/lobby"));
        final var endpoint = pairedClients[0].getConnection().getResourceDescriptor();

        final var thirdClient = createClient(SERVER_URI.concat(endpoint));
        thirdClient.connectBlocking();

        final var isOpenFirst = pairedClients[0].isOpen();
        final var isOpenSecond = pairedClients[1].isOpen();
        pairedClients[0].closeBlocking();
        pairedClients[1].closeBlocking();
        assertThat(thirdClient.isClosed()).isTrue();
        assertThat(thirdClient.isClosed()).isTrue();
        assertThat(isOpenFirst).isTrue();
        assertThat(isOpenSecond).isTrue();
    }
}
