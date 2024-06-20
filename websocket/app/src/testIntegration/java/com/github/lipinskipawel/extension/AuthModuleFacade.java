package com.github.lipinskipawel.extension;

import com.github.lipinskipawel.api.QueryRegister;

import java.util.Optional;

/**
 * {@link AuthModuleFacade} is used to inject external dependency such as Auth module.
 */
public interface AuthModuleFacade extends QueryRegister {

    /**
     * This method register a username associated with given token.
     *
     * @param username to register
     * @param token    to register along with the given username
     */
    void register(String username, String token);

    @Override
    Optional<String> findUsernameByToken(final String token);

    /**
     * Clears all registered usernames so far.
     */
    void clearAll();
}
