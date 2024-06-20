package com.github.lipinskipawel.server.mocks;

import com.github.lipinskipawel.api.QueryRegister;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class TestRegister implements QueryRegister {
    private final Map<String, String> usernamesToTokens;

    public TestRegister() {
        this.usernamesToTokens = new HashMap<>();
    }

    public void register(final String token) {
        this.usernamesToTokens.put(token, token);
    }

    @Override
    public Optional<String> findUsernameByToken(String token) {
        return Optional.ofNullable(this.usernamesToTokens.get(token));
    }
}
