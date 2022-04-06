package com.github.lipinskipawel.register;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * This class is an entrypoint for all related things to the registering usernames functionalities.
 * Responsibilities of this class:
 * - registering usernames
 * - providing tokens for registered usernames
 * <p>
 * The process of generating the tokens is delegated to the {@link Supplier<String>} lambada passed into the constructor.
 * <p>
 * This class holds usernames and tokens in memory, so on every restart of the application the information is lost.
 */
final class Register {
    private final Map<String, String> usernamesToTokens;
    private final Supplier<String> tokenGenerator;

    Register(final Supplier<String> tokenGenerator) {
        this.usernamesToTokens = new HashMap<>();
        this.tokenGenerator = tokenGenerator;
    }

    /**
     * Register a given username.
     *
     * @param username to be registered
     * @return true or false, true when the username has been registered otherwise false
     */
    boolean register(final String username) {
        if (usernamesToTokens.get(username) != null) {
            return false;
        }
        final var maybeToken = tryToGenerateUniqueToken();
        if (maybeToken.isPresent()) {
            usernamesToTokens.put(username, maybeToken.get());
            return true;
        } else {
            return false;
        }
    }

    private Optional<String> tryToGenerateUniqueToken() {
        for (var i = 0; i < 3; i++) {
            final var randomToken = tokenGenerator.get();
            if (usernamesToTokens.containsValue(randomToken)) {
                continue;
            }
            return Optional.of(randomToken);
        }
        return Optional.empty();
    }

    /**
     * Obtains a token for the given username.
     *
     * @param username to get the token for
     * @return token that is associated with the given username
     */
    String getTokenForUsername(final String username) {
        final var token = usernamesToTokens.get(username);
        if (token == null) {
            throw new IllegalArgumentException("The " + username + " has not been registered yet.");
        }
        return token;
    }
}
