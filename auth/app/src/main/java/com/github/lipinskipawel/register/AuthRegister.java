package com.github.lipinskipawel.register;

import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;

public final class AuthRegister {
    private final Register register;

    public AuthRegister(Register register) {
        this.register = register;
    }

    public Optional<String> handle(Map<String, String> headers) {
        return ofNullable(headers.get("username"))
            .filter(register::register)
            .map(register::getTokenForUsername);
    }

    public Optional<String> findUsernameByToken(final String token) {
        return register.findUsernameForToken(token);
    }

    public void clearAll() {
        register.store().clear();
    }
}
