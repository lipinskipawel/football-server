package com.github.lipinskipawel.client;

import java.util.Optional;

public interface AuthClient {

    /**
     * Register username.
     *
     * @param username that would like to register
     * @return Optional with token if username was registered, empty otherwise
     */
    Optional<String> register(final String username);

    /**
     * Finds username that is associated with the given token.
     *
     * @param token that will be used to find associated username
     * @return Optional username that is associated with the given token
     */
    Optional<String> findUsernameByToken(final String token);
}
