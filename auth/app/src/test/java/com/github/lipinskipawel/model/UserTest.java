package com.github.lipinskipawel.model;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static com.github.lipinskipawel.model.Token.token;
import static com.github.lipinskipawel.model.User.Builder.createdUser;
import static com.github.lipinskipawel.model.Username.username;
import static java.time.Instant.now;

class UserTest implements WithAssertions {

    @Test
    void creates_correct_user() {
        final var now = now();

        createdUser()
            .token(token("34342"))
            .username(username("john"))
            .createdDate(now)
            .updatedDate(now)
            .build();
    }

    @Test
    void does_not_create_user_when_created_date_is_after_updated_date() {
        final var now = now();
        final var secondAgo = now.minusSeconds(1);

        final var result = catchThrowable(() -> createdUser()
            .token(token("34342"))
            .username(username("john"))
            .createdDate(now)
            .updatedDate(secondAgo)
            .build());

        assertThat(result)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Created date [%s] can not be after updated date [%s]".formatted(now, secondAgo));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
            1                   | Token length must be between 3-16, but was [1]
            12345678901234567   | Token length must be between 3-16, but was [17]
        """)
    void does_not_create_user_when_incorrect_token(String token, String errorMessage) {
        final var now = now();

        final var result = catchThrowable(() -> createdUser()
            .username(username("john"))
            .token(token(token))
            .createdDate(now)
            .updatedDate(now)
            .build());

        assertThat(result)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(errorMessage);
    }
}
