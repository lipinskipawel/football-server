package com.github.lipinskipawel.register;

import com.github.lipinskipawel.db.UserRepository;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class AuthRegisterTest implements WithAssertions {

    private final UserRepository userRepository = mock(UserRepository.class);

    private final AuthRegister handler = new AuthRegister(new Register(new TokenGenerator()), userRepository);

    @Test
    void should_return_token_when_username_header_is_provided() {
        final var noUsernameHeader = Map.of("username", "test");
        given(userRepository.save(any())).willReturn(1);

        final var noToken = handler.handle(noUsernameHeader);

        assertThat(noToken).isPresent();
    }

    @Test
    void should_return_no_token_when_no_username_header() {
        final var noUsernameHeader = Map.<String, String>of();

        final var noToken = handler.handle(noUsernameHeader);

        assertThat(noToken).isEmpty();
    }
}
