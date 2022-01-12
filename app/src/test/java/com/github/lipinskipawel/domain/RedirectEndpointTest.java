package com.github.lipinskipawel.domain;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

final class RedirectEndpointTest implements WithAssertions {

    @Nested
    class CreateNewRedirectEndpoint {
        @Test
        void shouldCreateRedirectEndpoint() {
            final var subject = new RedirectEndpoint();

            final var endpoint = subject.createNewRedirectEndpoint("first", "second");

            assertThat(endpoint)
                    .isNotBlank()
                    .startsWith("/game/");
        }

        @Test
        void shouldGenerateTheSameRedirectEndpointForTheSameConnectedClients() {
            final var subject = new RedirectEndpoint();

            final var firstEndpoint = subject.createNewRedirectEndpoint("first", "second");
            final var secondEndpoint = subject.createNewRedirectEndpoint("first", "second");

            assertThat(firstEndpoint)
                    .isNotBlank()
                    .startsWith("/game/")
                    .isEqualTo(secondEndpoint);
        }

        @Test
        void shouldCreateUniqueRedirectEndpoint() {
            final var subject = new RedirectEndpoint();

            final var firstEndpoint = subject.createNewRedirectEndpoint("first", "second");
            final var secondEndpoint = subject.createNewRedirectEndpoint("third", "fourth");

            assertThat(firstEndpoint)
                    .isNotBlank()
                    .startsWith("/game/");
            assertThat(secondEndpoint)
                    .isNotBlank()
                    .startsWith("/game/");
            assertThat(firstEndpoint).isNotEqualTo(secondEndpoint);
        }
    }

    @Nested
    class CanJoin {
        @Test
        void shouldBeAbleToJoinTheGame() {
            final var subject = new RedirectEndpoint();
            final var redirectedEndpoint = subject.createNewRedirectEndpoint("first", "second");

            final var isJoined = subject.canJoin("first", redirectedEndpoint);

            assertThat(isJoined).isTrue();
        }

        @Test
        void shouldNotBeAbleToJoinTheGame() {
            final var subject = new RedirectEndpoint();
            final var redirectedEndpoint = subject.createNewRedirectEndpoint("first", "second");

            final var isJoined = subject.canJoin("third", redirectedEndpoint);

            assertThat(isJoined).isFalse();
        }

        @Test
        void shouldNotBeAbleToJoinTNonExistingEndpoint() {
            final var subject = new RedirectEndpoint();
            subject.createNewRedirectEndpoint("first", "second");

            final var isJoined = subject.canJoin("third", "non-existing");

            assertThat(isJoined).isFalse();
        }
    }
}
