package com.github.lipinskipawel.domain;

import com.github.lipinskipawel.mocks.TestConnectedClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

final class DualConnectionTest {
    private static final String EXAMPLE_TEST_MESSAGE = "example test message";
    private DualConnection dualConnection;

    @BeforeEach
    void setUp() {
        dualConnection = new DualConnection(Object::toString);
    }

    @Nested
    @DisplayName("Check number of players")
    class NumberOfPlayers {
        @Test
        void shouldAcceptOnePlayer() {
            var firstClient = new TestConnectedClient("/example/1");

            var accepted = dualConnection.accept(firstClient);

            assertThat(accepted).isTrue();
        }

        @Test
        void shouldAcceptTwoPlayers() {
            var firstClient = new TestConnectedClient("/example/1");
            var secondClient = new TestConnectedClient("/example/1");

            dualConnection.accept(firstClient);
            var accepted = dualConnection.accept(secondClient);

            assertThat(accepted).isTrue();
        }

        @Test
        void shouldNotAcceptThreePlayers() {
            var firstClient = new TestConnectedClient("/example/1");
            var secondClient = new TestConnectedClient("/example/1");
            var thirdClient = new TestConnectedClient("/example/1");

            dualConnection.accept(firstClient);
            dualConnection.accept(secondClient);
            var notAccepted = dualConnection.accept(thirdClient);

            assertThat(notAccepted).isFalse();
        }
    }

    @Nested
    @DisplayName("Check per endpoint communication")
    class EndpointOfPlayers {
        @Test
        void shouldReceivedMessagesConnectedToTheSameURL() {
            final var firstClient = new TestConnectedClient("/example/1");
            final var secondClient = new TestConnectedClient("/example/1");
            dualConnection.accept(firstClient);
            dualConnection.accept(secondClient);

            dualConnection.sendMessageFrom(EXAMPLE_TEST_MESSAGE, firstClient);

            assertThat(secondClient.getMessages().get(0)).isEqualTo(EXAMPLE_TEST_MESSAGE);
        }

        @Test
        void shouldNotReceivedMessagesConnectedToDifferentUrl() {
            final var firstClient = new TestConnectedClient("/example/1");
            final var secondClient = new TestConnectedClient("/example/2");
            dualConnection.accept(firstClient);
            dualConnection.accept(secondClient);

            dualConnection.sendMessageFrom(EXAMPLE_TEST_MESSAGE, firstClient);

            assertThat(firstClient.getMessages().size()).isEqualTo(0);
        }
    }

    @Test
    void shouldLeaveTheTable() {
        final var firstClient = new TestConnectedClient("/example/1");
        final var secondClient = new TestConnectedClient("/example/1");
        dualConnection.accept(firstClient);
        dualConnection.accept(secondClient);

        dualConnection.dropConnectionFor(firstClient);
        dualConnection.sendMessageFrom(EXAMPLE_TEST_MESSAGE, firstClient);

        assertThat(firstClient.getMessages().size()).isEqualTo(0);
    }

    @Test
    void shouldNotLeakConnection() {
        final var firstClient = new TestConnectedClient("/example/1");
        final var secondClient = new TestConnectedClient("/example/1");
        dualConnection.accept(firstClient);
        dualConnection.accept(secondClient);

        dualConnection.dropConnectionFor(firstClient);

        assertThat(firstClient.isClosed()).isTrue();
    }

}