package com.github.lipinskipawel.client;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Optional.empty;

public final class StubAuthClient implements AuthClient {

    private final Map<String, String> usernamesToTokens = new ConcurrentHashMap<>();

    @Override
    public Optional<String> register(String username) {
        if (usernamesToTokens.containsKey(username)) {
            return empty();
        }
        final var token = username + "_token";
        usernamesToTokens.put(username, token);
        return Optional.of(token);
    }

    @Override
    public Optional<String> findUsernameByToken(String token) {
        return usernamesToTokens.entrySet()
                .stream()
                .filter(it -> it.getValue().equals(token))
                .map(Map.Entry::getKey)
                .findFirst();
    }
}
