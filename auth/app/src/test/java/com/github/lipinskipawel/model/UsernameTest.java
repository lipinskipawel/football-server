package com.github.lipinskipawel.model;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.github.lipinskipawel.model.Token.token;
import static com.github.lipinskipawel.model.User.Builder.createdUser;
import static com.github.lipinskipawel.model.Username.username;
import static java.time.Instant.now;
import static java.util.function.Function.identity;

final class UsernameTest implements WithAssertions {

    private static Stream<String> correctLengthUsernames() {
        return Stream.of(
            "aa3",
            "aaa4",
            "aaaa5",
            "aaaaa6",
            "aaaaaa7",
            "aaaaaaa8",
            "aaaaaaaa9",
            "aaaaaaaa10",
            "aaaaaaaaa11",
            "aaaaaaaaaa12",
            "aaaaaaaaaaa13",
            "aaaaaaaaaaaa14",
            "aaaaaaaaaaaaa15",
            "aaaaaaaaaaaaaa16"
        );
    }

    private static Stream<String> correctSpecialCharactersUsername() {
        return Stream.of(
            "some_username",
            "some___correct"
        );
    }

    private static Stream<Arguments> correctUsernames() {
        return Stream.of(
                correctLengthUsernames(),
                correctSpecialCharactersUsername()
            )
            .flatMap(identity())
            .map(Arguments::arguments);
    }

    @ParameterizedTest
    @MethodSource("correctUsernames")
    void create_username(String username) {
        username(username);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
            a                   | Username length must be between 3-16, but was [1]
            a\u03B1             | Username length must be between 3-16, but was [2]
            a2                  | Username length must be between 3-16, but was [2]
            a2345678901234567   | Username length must be between 3-16, but was [17]
            8someUser           | Username have to start with a letter [8someUser] is not allowed
            _fd!ii              | Username have to start with a letter [_fd!ii] is not allowed
            dfd!ii              | Username can not have illegal characters [dfd!ii]
            b\u03B1bb\u00A7     | Username can not have illegal characters [bαbb§]
        """)
    void does_not_create_user_when_incorrect_username(String username, String errorMessage) {
        final var now = now();

        final var result = catchThrowable(() -> createdUser()
            .token(token("888"))
            .username(username(username))
            .createdDate(now)
            .updatedDate(now)
            .build());

        assertThat(result)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(errorMessage);
    }
}
