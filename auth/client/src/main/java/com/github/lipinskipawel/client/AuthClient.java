package com.github.lipinskipawel.client;

import java.util.Optional;

public interface AuthClient {

    /**
     * Register username.
     *
     * @param username that would like to register
     * @return true if username was registered, false otherwise
     */
    boolean register(final String username);

    /**
     * Finds username that is associated with the given token.
     *
     * @param token that will be used to find associated username
     * @return Optional username that is associated with the given token
     */
    Optional<String> findUsernameByToken(final String token);
}
