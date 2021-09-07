package com.github.lipinskipawel.server;

import org.assertj.core.api.Assertions;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.handshake.ClientHandshake;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.assertj.core.api.Assertions.fail;

final class HandshakePolicyTest {

    @Test
    void shouldNotPassTheWebConnectionPolicyWhenResourceIsRoot() {
        final var policy = createClientHandshakeMock("/");

        Assertions.assertThatThrownBy(() -> HandshakePolicy.webConnectionPolicy(policy));
    }

    @Test
    void shouldNotPassTheWebConnectionPolicyWhenResourceIsAbc() {
        final var policy = createClientHandshakeMock("/abc");

        Assertions.assertThatThrownBy(() -> HandshakePolicy.webConnectionPolicy(policy));
    }

    @Test
    void shouldNotPassTheWebConnectionPolicyWhenResourceIsChat() {
        final var policy = createClientHandshakeMock("/chat");

        Assertions.assertThatThrownBy(() -> HandshakePolicy.webConnectionPolicy(policy));
    }

    @Test
    void shouldPassTheWebConnectionPolicyWhenResourceIsChatWithNextLevelNestedPath() {
        final var policy = createClientHandshakeMock("/chat/12546");

        try {
            HandshakePolicy.webConnectionPolicy(policy);
        } catch (InvalidDataException e) {
            fail("Policy should allow one level under the chat endpoint");
        }
    }

    @Test
    void shouldNotPassTheWebConnectionPolicyWhenResourceChatWithTwoMoreNestedPaths() {
        final var policy = createClientHandshakeMock("/chat/123123/abc");

        Assertions.assertThatThrownBy(() -> HandshakePolicy.webConnectionPolicy(policy));
    }

    static ClientHandshake createClientHandshakeMock(final String resourceDescriptor) {
        return new ClientHandshake() {
            @Override
            public String getResourceDescriptor() {
                return resourceDescriptor;
            }

            @Override
            public Iterator<String> iterateHttpFields() {
                return null;
            }

            @Override
            public String getFieldValue(String name) {
                return null;
            }

            @Override
            public boolean hasFieldValue(String name) {
                return false;
            }

            @Override
            public byte[] getContent() {
                return new byte[0];
            }
        };
    }
}
