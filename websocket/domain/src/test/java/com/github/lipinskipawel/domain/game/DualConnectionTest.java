package com.github.lipinskipawel.domain.game;

import com.github.lipinskipawel.domain.mocks.TestConnectedClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

final class DualConnectionTest {
    private static final String EXAMPLE_TEST_MESSAGE = "example test message";
    private static final String USERNAME_MARK = "mark";
    private DualConnection dualConnection;

    @BeforeEach
    void setUp() {
        dualConnection = new DualConnection();
    }

    @Nested
    @DisplayName("Check number of players")
    class NumberOfPlayers {
        @Test
        void shouldAcceptOnePlayer() {
            var firstClient = new TestConnectedClient(USERNAME_MARK);

            var accepted = dualConnection.accept(firstClient);

            assertThat(accepted).isTrue();
        }

        @Test
        void shouldAcceptTwoPlayers() {
            var firstClient = new TestConnectedClient(USERNAME_MARK);
            var secondClient = new TestConnectedClient(USERNAME_MARK);

            dualConnection.accept(firstClient);
            var accepted = dualConnection.accept(secondClient);

            assertThat(accepted).isTrue();
        }

        @Test
        void shouldNotAcceptThreePlayers() {
            var firstClient = new TestConnectedClient(USERNAME_MARK);
            var secondClient = new TestConnectedClient(USERNAME_MARK);
            var thirdClient = new TestConnectedClient(USERNAME_MARK);

            dualConnection.accept(firstClient);
            dualConnection.accept(secondClient);
            var notAccepted = dualConnection.accept(thirdClient);

            assertThat(notAccepted).isFalse();
        }

        @Test
        void shouldAcceptTheSamePlayerTwice() {
            var firstClient = new TestConnectedClient(USERNAME_MARK);

            dualConnection.accept(firstClient);
            var accepted = dualConnection.accept(firstClient);

            assertThat(accepted).isTrue();
        }
    }

    @Nested
    @DisplayName("Check per endpoint communication")
    class EndpointOfPlayers {
        @Test
        void shouldReceivedMessagesConnectedToTheSameURL() {
            final var firstClient = new TestConnectedClient(USERNAME_MARK);
            final var secondClient = new TestConnectedClient(USERNAME_MARK);
            dualConnection.accept(firstClient);
            dualConnection.accept(secondClient);

            dualConnection.sendMessageFrom(EXAMPLE_TEST_MESSAGE, firstClient);

            assertThat(secondClient.getMessages().get(0)).isEqualTo(EXAMPLE_TEST_MESSAGE);
        }

        @Test
        void shouldNotReceivedMessagesConnectedToDifferentUrl() {
            final var firstClient = new TestConnectedClient(USERNAME_MARK);
            final var secondClient = new TestConnectedClient("/example/2");
            dualConnection.accept(firstClient);
            dualConnection.accept(secondClient);

            dualConnection.sendMessageFrom(EXAMPLE_TEST_MESSAGE, firstClient);

            assertThat(firstClient.getMessages().size()).isEqualTo(0);
        }
    }

    @Test
    void shouldLeaveTheTable() {
        final var firstClient = new TestConnectedClient(USERNAME_MARK);
        final var secondClient = new TestConnectedClient(USERNAME_MARK);
        dualConnection.accept(firstClient);
        dualConnection.accept(secondClient);

        dualConnection.dropConnectionFor(firstClient);
        dualConnection.sendMessageFrom(EXAMPLE_TEST_MESSAGE, firstClient);

        assertThat(firstClient.getMessages().size()).isEqualTo(0);
    }

    @Test
    void shouldNotLeakConnection() {
        final var firstClient = new TestConnectedClient(USERNAME_MARK);
        final var secondClient = new TestConnectedClient(USERNAME_MARK);
        dualConnection.accept(firstClient);
        dualConnection.accept(secondClient);

        dualConnection.dropConnectionFor(firstClient);

        assertThat(firstClient.isClosed()).isTrue();
    }

    @Test
    void shouldNotSendMessageWhenClientIsNotAccepted() {
        final var firstClient = new TestConnectedClient(USERNAME_MARK);
        final var secondClient = new TestConnectedClient(USERNAME_MARK);
        final var notAccepted = new TestConnectedClient(USERNAME_MARK);
        dualConnection.accept(secondClient);
        dualConnection.accept(firstClient);

        dualConnection.sendMessageTo(EXAMPLE_TEST_MESSAGE, notAccepted);

        assertThat(notAccepted)
            .extracting(TestConnectedClient::getMessages)
            .asList()
            .hasSize(0);
    }
}
