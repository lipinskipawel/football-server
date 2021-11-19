package com.github.lipinskipawel.user;

import org.assertj.core.api.WithAssertions;
import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.enums.Opcode;
import org.java_websocket.enums.ReadyState;
import org.java_websocket.framing.Framedata;
import org.java_websocket.protocols.IProtocol;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLSession;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Collection;

final class MinimalisticClientContextTest implements WithAssertions {

    @Nested
    class FromStaticFactoryMethod {
        @Test
        void shouldCreateConnectedClientWhenGivenWebSocket() {
            final WebSocket webSocket = new TestWebSocket("/lobby");

            final var client = ConnectedClient.from(webSocket, "a");

            assertThat(client)
                    .get()
                    .isNotNull();
        }

        @Test
        void shouldReturnNewConnectedClientWhenGivenWasClosed() {
            final WebSocket webSocket = new TestWebSocket("/lobby");

            final var firstClient = ConnectedClient.from(webSocket, "a");
            webSocket.close();
            final var secondClient = ConnectedClient.from(webSocket, "a");

            assertThat(secondClient).get().isNotNull();
            assertThat(firstClient)
                    .get()
                    .isNotNull()
                    .isNotSameAs(secondClient);
        }

        @Test
        void shouldReturnDifferentConnectedClientsWhenTheSameUsernameButDifferentWebSocket() {
            final WebSocket firstWebSocket = new TestWebSocket("/lobby");
            final WebSocket secondWebSocket = new TestWebSocket("/lobby");

            final var firstClient = ConnectedClient.from(firstWebSocket, "a");
            final var secondClient = ConnectedClient.from(secondWebSocket, "a");

            assertThat(secondClient).isNotNull();
            assertThat(firstClient)
                    .get()
                    .isNotNull()
                    .isNotSameAs(secondClient);
        }

        @Test
        void shouldReturnEmptyOptionalOfConnectedClientWhenTheSameWebSocketTriesToRegisteredWithDifferentUsername() {
            final WebSocket webSocket = new TestWebSocket("/lobby");

            final var firstClient = ConnectedClient.from(webSocket, "a");
            final var secondClient = ConnectedClient.from(webSocket, "b");

            assertThat(secondClient).isEmpty();
            assertThat(firstClient)
                    .get()
                    .isNotNull()
                    .isNotSameAs(secondClient);
        }
    }

    @Nested
    class FindByStaticMethod {
        @Test
        void shouldFindByWebSocket() {
            final WebSocket webSocket = new TestWebSocket("/lobby");
            ConnectedClient.from(webSocket, "a");

            final var client = ConnectedClient.findBy(webSocket);

            assertThat(client)
                    .get()
                    .isNotNull();
        }

        @Test
        void shouldReturnCachedConnectedClientWhenGivenTheSameWebSocket() {
            final WebSocket webSocket = new TestWebSocket("/lobby");
            ConnectedClient.from(webSocket, "a");

            final var firstClient = ConnectedClient.findBy(webSocket);
            final var secondClient = ConnectedClient.findBy(webSocket);

            assertThat(secondClient).get().isNotNull();
            assertThat(firstClient)
                    .get()
                    .isNotNull()
                    .isSameAs(secondClient.get());
        }
    }

    @Nested
    class FindByUsernameStaticMethod {
        @Test
        void shouldFindByUsername() {
            final WebSocket webSocket = new TestWebSocket("/lobby");
            ConnectedClient.from(webSocket, "a");

            final var client = ConnectedClient.findByUsername("a");

            assertThat(client)
                    .get()
                    .isNotNull();
        }

        @Test
        void shouldReturnCachedConnectedClientWhenAskedForTheSameUsername() {
            final WebSocket webSocket = new TestWebSocket("/lobby");
            ConnectedClient.from(webSocket, "a");

            final var firstClient = ConnectedClient.findByUsername("a");
            final var secondClient = ConnectedClient.findByUsername("a");

            assertThat(secondClient).get().isNotNull();
            assertThat(firstClient)
                    .get()
                    .isNotNull()
                    .isSameAs(secondClient.get());
        }
    }

    private static class TestWebSocket implements WebSocket {
        private final String resourceDescriptor;
        private boolean isClosed = false;

        public TestWebSocket(String resourceDescriptor) {
            this.resourceDescriptor = resourceDescriptor;
        }

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
            return resourceDescriptor;
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
