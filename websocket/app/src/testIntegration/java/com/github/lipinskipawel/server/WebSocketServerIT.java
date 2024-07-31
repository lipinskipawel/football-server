package com.github.lipinskipawel.server;

import com.github.lipinskipawel.api.Player;
import com.github.lipinskipawel.client.SimpleWebSocketClient;
import com.github.lipinskipawel.extension.IntegrationSpec;
import org.junit.jupiter.api.Test;

import static com.github.lipinskipawel.client.SimpleWebSocketClient.createClient;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

final class WebSocketServerIT extends IntegrationSpec {
    static final String SERVER_URI = "ws://localhost:%d/ws".formatted(PORT);

    @Test
    void shouldRejectClientWhenURIDoesNotStartsWithWs() throws InterruptedException {
        registerUsername("first");
        final var client = createClient(SERVER_URI.replace("/ws", "/example"));
        client.addHeader("cookie", "first_token");

        client.connectBlocking();

        waitFor(() -> client.getClose().size() == 1);
        final var isOpen = client.isOpen();
        client.closeBlocking();
        assertThat(isOpen).isFalse();
    }

    @Test
    void shouldRejectClientWhenURIDoNotMeetPolicy() throws InterruptedException {
        registerUsername("first");
        final var client = createClient(SERVER_URI.concat("/example"));
        client.addHeader("cookie", "first_token");

        client.connectBlocking();

        waitFor(() -> client.getClose().size() == 1);
        final var isOpen = client.isOpen();
        client.closeBlocking();
        assertThat(isOpen).isFalse();
    }

    @Test
    void shouldCloseConnectionWhenUnexpectedMessageArrives() throws InterruptedException {
        registerUsername("first");
        final var client = createClient(SERVER_URI.concat("/lobby"));
        client.addHeader("cookie", "first_token");
        client.connectBlocking();
        final var player = Player.fromUsername("example");

        client.send(player);

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
        waitFor(() -> pairedClients[1].getClose().size() == 1);
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
        registerUsername("third");
        thirdClient.addHeader("cookie", "third_token");
        thirdClient.connectBlocking();

        final var isOpenFirst = pairedClients[0].isOpen();
        final var isOpenSecond = pairedClients[1].isOpen();
        pairedClients[0].closeBlocking();
        pairedClients[1].closeBlocking();
        waitFor(() -> thirdClient.getClose().size() == 1);
        assertThat(thirdClient.isClosed()).isTrue();
        assertThat(isOpenFirst).isTrue();
        assertThat(isOpenSecond).isTrue();
    }
}
