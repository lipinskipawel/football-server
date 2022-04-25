package com.github.lipinskipawel.extension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class is designed to implement {@link AuthModuleFacade} that implements Register from AuthModule.
 * Its usage is only though the integration tests.
 */
final class TestRegister implements AuthModuleFacade {
    private final Map<String, String> usernamesToTokens;

    public TestRegister() {
        this.usernamesToTokens = new HashMap<>();
    }

    /**
     * This method does not implement any logic related to registering tokens and username. It just register anything
     * passed as argument to this method.
     *
     * @param username to register
     * @param token    to register
     */
    public void register(final String username, final String token) {
        this.usernamesToTokens.put(username, token);
    }

    @Override
    public Optional<String> usernameForToken(String token) {
        final var username = this.usernamesToTokens
                .entrySet()
                .stream()
                .filter(it -> it.getValue().equals(token))
                .map(Map.Entry::getKey)
                .toList();
        if (username.size() != 1) return Optional.empty();
        return Optional.of(username.get(0));
    }

    @Override
    public String toString() {
        return this.usernamesToTokens
                .entrySet()
                .stream()
                .map(it -> it.getKey() + ": " + it.getValue())
                .collect(Collectors.joining("\n"));
    }
}
