package com.github.lipinskipawel.api;

import java.util.Optional;

/**
 * This is high level interface designed to encapsulate registering domain as an application program interface (API).
 */
public interface QueryRegister {

    /**
     * Checks whether given username was registered. Every username must be registered beforehand this method will
     * return true.
     *
     * @param username to check for registrations
     * @return true or false, true when user was registered, otherwise false
     */
    boolean isRegistered(final String username);

    /**
     * Finds username that is associated with the given token.
     *
     * @param token that will be used to find associated username
     * @return Optional username that is associated with the given token
     */
    Optional<String> usernameForToken(final String token);
}
