package com.github.lipinskipawel.server;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.github.lipinskipawel.server.WebSocketFilter.shouldFilter;

class WebSocketFilterTest implements WithAssertions {

    @Test
    void shouldNotFilterWhenURLStartsWithWSAndExampleAndItIsNotUpgrade() {
        final var headers = Map.of("some", "value").entrySet();
        final var result = shouldFilter(headers, "/ws");

        assertThat(result).isFalse();
    }

    @Test
    void shouldNotFilterWhenURLStartsWithWSAndLobby() {
        final var headers = Map.of("Upgrade", "websocket").entrySet();
        final var result = shouldFilter(headers, "/ws/lobby");

        assertThat(result).isFalse();
    }

    @Test
    void shouldNotFilterWhenURLDoesNotStartsWithWS() {
        final var headers = Map.of("Upgrade", "websocket").entrySet();
        final var result = shouldFilter(headers, "/some/ws/lobby");

        assertThat(result).isTrue();
    }
}