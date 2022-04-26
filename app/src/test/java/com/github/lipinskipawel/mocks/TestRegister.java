package com.github.lipinskipawel.mocks;

import com.github.lipinskipawel.api.QueryRegister;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Simple implementation of the Auth module API for testing purposes.
 */
public class TestRegister implements QueryRegister {
    private final Map<String, String> tokensToUsernames;

    public TestRegister() {
        this.tokensToUsernames = new HashMap<>();
    }

    /**
     * This method does not implement any logic related to registering tokens and username. It just register anything
     * passed as argument to this method.
     *
     * @param token    to register
     * @param username to register
     */
    public void register(final String token, final String username) {
        this.tokensToUsernames.put(token, username);
    }

    @Override
    public Optional<String> findUsernameByToken(String token) {
        return Optional.ofNullable(this.tokensToUsernames.get(token));
    }
}
