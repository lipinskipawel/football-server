package com.github.lipinskipawel.register;

import io.netty.channel.ChannelHandler;

/**
 * This class holds method to obtain production ready implementation of the {@link RegisterHandler} class.
 */
public final class RegisterEntrypoint {

    /**
     * Production ready RegisterHandler.
     *
     * @return register handler
     */
    public static ChannelHandler getRegisterHandler() {
        return createRegister(new Register(new TokenGenerator()));
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
