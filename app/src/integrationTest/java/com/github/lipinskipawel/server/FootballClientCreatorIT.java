package com.github.lipinskipawel.server;

import com.github.lipinskipawel.client.FootballClientCreator;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class FootballClientCreatorIT implements WithAssertions {
    private static final ExecutorService pool = Executors.newFixedThreadPool(1);
    private static final int PORT = 8093;
    private static final String SERVER_URI = "ws://localhost:%d/lobby".formatted(PORT);
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
    void shouldConnectTwoClientsToPlayEachOther() throws InterruptedException {
        final var pairedClients = FootballClientCreator.getPairedClients(SERVER_URI);

        assertThat(pairedClients).hasSize(2);
        assertThat(pairedClients[0].isOpen()).isTrue();
        assertThat(pairedClients[1].isOpen()).isTrue();
    }
}
