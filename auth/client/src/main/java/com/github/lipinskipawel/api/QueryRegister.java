package com.github.lipinskipawel.api;

import java.util.Optional;

/**
 * This is high level interface designed to encapsulate registering domain as an application program interface (API).
 */
public interface QueryRegister {

    /**
     * Finds username that is associated with the given token.
     *
     * @param token that will be used to find associated username
     * @return Optional username that is associated with the given token
     */
    Optional<String> findUsernameByToken(final String token);
}
