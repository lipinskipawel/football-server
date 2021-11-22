package com.github.lipinskipawel.server;

import com.github.lipinskipawel.client.FootballClientCreator;
import com.github.lipinskipawel.client.SimpleWebSocketClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.github.lipinskipawel.client.SimpleWebSocketClient.createClient;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

final class FootballServerIT {
    private static final ExecutorService pool = Executors.newFixedThreadPool(1);
    private static final int PORT = 8090;
    private static final String SERVER_URI = "ws://localhost:%d".formatted(PORT);
    private static final FootballServer server = new FootballServer(new InetSocketAddress("localhost", PORT));

    @BeforeAll
    static void setUp() {
        pool.submit(server);
    }

    @AfterAll
    static void cleanUp() throws InterruptedException {
        server.stop(1000);
        pool.shutdown();
    }

    @Test
    void shouldRejectClientWhenURIDoNotMeetPolicy() throws InterruptedException {
        final var client = createClient(SERVER_URI.concat("/example"));

        client.connectBlocking();

        final var connectionClose = client.isOpen();
        client.closeBlocking();
        assertThat(connectionClose).isFalse();
    }

    @Test
    void shouldClientNotReceivedMessageWhenNotAGameMove() throws InterruptedException {
        final var serverUri = "ws://localhost:%d/lobby".formatted(PORT);
        final var pairedClients = FootballClientCreator.getPairedClients(serverUri);

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
        final var serverUri = "ws://localhost:%d/lobby".formatted(PORT);
        final var pairedClients = FootballClientCreator.getPairedClients(serverUri);
        final var endpoint = pairedClients[0].getConnection().getResourceDescriptor();

        final var thirdClient = createClient("ws://localhost:%d".formatted(PORT).concat(endpoint));
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
