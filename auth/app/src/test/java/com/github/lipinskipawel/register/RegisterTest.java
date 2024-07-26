package com.github.lipinskipawel.register;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

final class RegisterTest implements WithAssertions {
    private Register register;

    @BeforeEach
    void setUp() {
        register = new Register(new TokenGenerator(new Random(123L), 16));
    }

    @Test
    void shouldRegisterNewUsername() {
        final var isRegistered = register.register("mark");

        assertThat(isRegistered).isTrue();
    }

    @Test
    void shouldNotRegisterTheSameUsername() {
        register.register("mark");
        final var isRegistered = register.register("mark");

        assertThat(isRegistered).isFalse();
    }

    @Test
    void shouldReturnTokenForCorrectlyRegisteredUsername() {
        final var isRegistered = register.register("mark");
        final var token = register.getTokenForUsername("mark");

        assertThat(isRegistered).isTrue();
        assertThat(token).isEqualTo("ooChTJGf682uMjbF");
    }

    @Test
    void shouldNotReturnTokenForNotRegisteredUsername() {
        assertThatThrownBy(() -> register.getTokenForUsername("mark"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The mark has not been registered yet.");
    }

    @Test
    void shouldReturnTokenDifferentTokensForDifferentUsernames() {
        register.register("mark");
        register.register("john");
        final var marksToken = register.getTokenForUsername("mark");
        final var johnsToken = register.getTokenForUsername("john");

        assertThat(marksToken).isEqualTo("ooChTJGf682uMjbF");
        assertThat(johnsToken).isEqualTo("yJQU7YCyGBQUx9XA");
        assertThat(marksToken).isNotEqualTo(johnsToken);
    }

    @Test
    void shouldReturnUsernameWhenGivenToken() {
        final var givenUsername = "mark";
        register.register(givenUsername);
        final var marksToken = register.getTokenForUsername(givenUsername);

        final var username = register.findUsernameForToken(marksToken);

        assertThat(username).satisfies(it -> {
            assertThat(it).isPresent();
            assertThat(it.get()).isEqualTo(givenUsername);
        });
    }

    @Test
    void shouldReturnUsernameWhenRegisterHaveManyUsers() {
        final var givenUsername = "mark";
        register.register("john");
        register.register(givenUsername);
        final var marksToken = register.getTokenForUsername(givenUsername);

        final var username = register.findUsernameForToken(marksToken);

        assertThat(username).satisfies(it -> {
            assertThat(it).isPresent();
            assertThat(it.get()).isEqualTo(givenUsername);
        });
    }
}
