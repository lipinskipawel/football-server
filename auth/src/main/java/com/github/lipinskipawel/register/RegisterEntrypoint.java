package com.github.lipinskipawel.register;

import com.github.lipinskipawel.api.QueryRegister;
import io.netty.channel.ChannelHandler;

import java.util.Optional;

/**
 * This class holds method to obtain production ready implementation of the {@link RegisterHandler} class.
 */
public final class RegisterEntrypoint {
    public static final Register register = new Register(new TokenGenerator());

    /**
     * Production ready RegisterHandler.
     *
     * @return register handler
     */
    public static ChannelHandler getRegisterHandler() {
        return createRegister(register);
    }

    /**
     * This method returns production ready implementation of {@link QueryRegister} interface.
     *
     * @return query register object
     */
    public static QueryRegister getRegister() {
        return token -> {
            try {
                return Optional.of(register.getUsernameForToken(token));
            } catch (IllegalArgumentException ex) {
                return Optional.empty();
            }
        };
    }

    /**
     * Package private helper method for testing.
     *
     * @param register to inject into {@link RegisterHandler}
     * @return custom register handler
     */
    static ChannelHandler createRegister(final Register register) {
        return new RegisterHandler(register);
    }
}
