package com.github.lipinskipawel.register;

import com.github.lipinskipawel.api.QueryRegister;
import io.netty.channel.ChannelHandler;

import java.util.Optional;

/**
 * This class holds method to obtain production ready implementation of the {@link RegisterHandler} class.
 */
public final class RegisterEntrypoint {
    private static final Register register = new Register(new TokenGenerator());

    /**
     * Production ready RegisterHandler.
     *
     * @return register handler
     */
    public static ChannelHandler getRegisterHandler() {
        return createRegister(register);
    }

    /**
     * This method returns
     *
     * @return register
     */
    public static QueryRegister getRegister() {
        return new QueryRegister() {
            @Override
            public boolean isRegistered(final String username) {
                try {
                    register.getTokenForUsername(username);
                    return true;
                } catch (IllegalArgumentException ex) {
                    return false;
                }
            }

            @Override
            public Optional<String> usernameForToken(String token) {
                try {
                    return Optional.of(register.getUsernameForToken(token));
                } catch (IllegalArgumentException ex) {
                    return Optional.empty();
                }
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
