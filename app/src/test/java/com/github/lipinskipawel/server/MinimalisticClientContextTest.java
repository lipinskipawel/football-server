package com.github.lipinskipawel.server;

import org.assertj.core.api.WithAssertions;
import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.enums.Opcode;
import org.java_websocket.enums.ReadyState;
import org.java_websocket.framing.Framedata;
import org.java_websocket.protocols.IProtocol;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLSession;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Collection;

final class MinimalisticClientContextTest implements WithAssertions {

    @Test
    void shouldCreateConnectedClientWhenGivenWebSocket() {
        final WebSocket webSocket = new TestWebSocket();

        final var client = MinimalisticClientContext.from(webSocket);

        assertThat(client).isNotNull();
    }

    @Test
    void shouldReturnCachedConnectedClientWhenGivenTheSameWebSocket() {
        final WebSocket webSocket = new TestWebSocket();

        final var firstClient = MinimalisticClientContext.from(webSocket);
        final var secondClient = MinimalisticClientContext.from(webSocket);

        assertThat(firstClient)
                .isNotNull()
                .isSameAs(secondClient);
    }

    @Test
    void shouldReturnNewConnectedClientWhenGivenWasClosed() {
        final WebSocket webSocket = new TestWebSocket();

        final var firstClient = MinimalisticClientContext.from(webSocket);
        webSocket.close();
        final var secondClient = MinimalisticClientContext.from(webSocket);

        assertThat(firstClient)
                .isNotNull()
                .isNotSameAs(secondClient);
    }

    private static class TestWebSocket implements WebSocket {
        private boolean isClosed = false;

        @Override
        public void close(int code, String message) {

        }

        @Override
        public void close(int code) {

        }

        @Override
        public void close() {
            this.isClosed = true;
        }

        @Override
        public void closeConnection(int code, String message) {

        }

        @Override
        public void send(String text) {

        }

        @Override
        public void send(ByteBuffer bytes) {

        }

        @Override
        public void send(byte[] bytes) {

        }

        @Override
        public void sendFrame(Framedata framedata) {

        }

        @Override
        public void sendFrame(Collection<Framedata> frames) {

        }

        @Override
        public void sendPing() {

        }

        @Override
        public void sendFragmentedFrame(Opcode op, ByteBuffer buffer, boolean fin) {

        }

        @Override
        public boolean hasBufferedData() {
            return false;
        }

        @Override
        public InetSocketAddress getRemoteSocketAddress() {
            return null;
        }

        @Override
        public InetSocketAddress getLocalSocketAddress() {
            return null;
        }

        @Override
        public boolean isOpen() {
            return false;
        }

        @Override
        public boolean isClosing() {
            return false;
        }

        @Override
        public boolean isFlushAndClose() {
            return false;
        }

        @Override
        public boolean isClosed() {
            return isClosed;
        }

        @Override
        public Draft getDraft() {
            return null;
        }

        @Override
        public ReadyState getReadyState() {
            return null;
        }

        @Override
        public String getResourceDescriptor() {
            return null;
        }

        @Override
        public <T> void setAttachment(T attachment) {

        }

        @Override
        public <T> T getAttachment() {
            return null;
        }

        @Override
        public boolean hasSSLSupport() {
            return false;
        }

        @Override
        public SSLSession getSSLSession() throws IllegalArgumentException {
            return null;
        }

        @Override
        public IProtocol getProtocol() {
            return null;
        }
    }
}
