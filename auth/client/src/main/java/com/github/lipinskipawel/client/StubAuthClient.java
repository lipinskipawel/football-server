package com.github.lipinskipawel.client;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class StubAuthClient implements AuthClient {

    public final Map<String, String> usernamesToTokens = new ConcurrentHashMap<>();

    @Override
    public boolean register(String username) {
        if (usernamesToTokens.containsKey(username)) {
            return false;
        }
        usernamesToTokens.put(username, username + "_token");
        return true;
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
