package com.github.lipinskipawel.register;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

class AuthRegisterTest implements WithAssertions {

    private final AuthRegister handler = new AuthRegister(new Register(new TokenGenerator()));

    @Test
    void should_return_no_token_when_no_username_header() {
        final var noUsernameHeader = Map.<String, String>of();

        final var noToken = handler.handle(noUsernameHeader);

        assertThat(noToken).isEmpty();
    }

    @Test
    void should_return_token_when_username_header_is_provided() {
        final var noUsernameHeader = Map.of("username", "test");

        final var noToken = handler.handle(noUsernameHeader);

        assertThat(noToken).isPresent();
    }
}