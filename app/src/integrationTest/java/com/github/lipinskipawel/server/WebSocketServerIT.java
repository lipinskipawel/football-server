package com.github.lipinskipawel.server;

import com.github.lipinskipawel.api.Player;
import com.github.lipinskipawel.client.SimpleWebSocketClient;
import com.github.lipinskipawel.extension.Application;
import com.github.lipinskipawel.extension.AuthModuleFacade;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static com.github.lipinskipawel.client.FootballClientCreator.getPairedClients;
import static com.github.lipinskipawel.client.FootballClientCreator.waitFor;
import static com.github.lipinskipawel.client.SimpleWebSocketClient.createClient;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Application(port = WebSocketServerIT.PORT)
final class WebSocketServerIT {
    private static final Parser parser = new Gson()::toJson;
    static final int PORT = 8090;
    static final String SERVER_URI = "ws://localhost:%d/ws".formatted(PORT);

    @Test
    void shouldRejectClientWhenURIDoesNotStartsWithWs(AuthModuleFacade facade) throws InterruptedException {
        facade.register("first", "aa");
        final var client = createClient(SERVER_URI.replace("/ws", "/example"));
        client.addHeader("cookie", "aa");

        client.connectBlocking();

        waitFor(() -> client.getClose().size() == 1);
        final var isOpen = client.isOpen();
        client.closeBlocking();
        assertThat(isOpen).isFalse();
    }

    @Test
    void shouldRejectClientWhenURIDoNotMeetPolicy(AuthModuleFacade facade) throws InterruptedException {
        facade.register("first", "aa");
        final var client = createClient(SERVER_URI.concat("/example"));
        client.addHeader("cookie", "aa");

        client.connectBlocking();

        waitFor(() -> client.getClose().size() == 1);
        final var isOpen = client.isOpen();
        client.closeBlocking();
        assertThat(isOpen).isFalse();
    }

    @Test
    void shouldCloseConnectionWhenUnexpectedMessageArrives(AuthModuleFacade facade) throws InterruptedException {
        facade.register("first", "aa");
        final var client = createClient(SERVER_URI.concat("/lobby"));
        client.addHeader("cookie", "aa");
        client.connectBlocking();
        final var player = Player.fromUsername("example");

        client.send(parser.toJson(player));

        waitFor(() -> client.getClose().size() == 1);
        final var isOpen = client.isOpen();
        client.closeBlocking();
        assertThat(isOpen).isFalse();
    }

    @Test
    void shouldClientNotReceivedMessageWhenNotAGameMove(AuthModuleFacade facade) throws InterruptedException {
        final var pairedClients = getPairedClients(SERVER_URI.concat("/lobby"), facade);

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
    void shouldAllowOnlyTwoClientsConnectToTheSameEndpoint(AuthModuleFacade facade) throws InterruptedException {
        final var pairedClients = getPairedClients(SERVER_URI.concat("/lobby"), facade);
        final var endpoint = pairedClients[0].getConnection().getResourceDescriptor();

        final var thirdClient = createClient(SERVER_URI.concat(endpoint));
        facade.register("third", "cc");
        thirdClient.addHeader("cookie", "cc");
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
