package com.github.lipinskipawel.register;

import com.github.lipinskipawel.api.QueryRegister;

import java.util.Optional;

public final class RegisterEntrypoint {
    public static final Register register = new Register(new TokenGenerator());

    /**
     * This method returns production ready implementation of {@link QueryRegister} interface.
     *
     * @return query register object
     */
    public static QueryRegister getRegister() {
        return token -> {
            try {
                return register.findUsernameForToken(token);
            } catch (IllegalArgumentException ex) {
                return Optional.empty();
            }
        };
    }
}
