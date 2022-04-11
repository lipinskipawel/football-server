package com.github.lipinskipawel.user;

import com.github.lipinskipawel.mocks.TestRegister;
import org.assertj.core.api.WithAssertions;
import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.enums.Opcode;
import org.java_websocket.enums.ReadyState;
import org.java_websocket.framing.Framedata;
import org.java_websocket.protocols.IProtocol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLSession;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Collection;

final class ConnectedClientFactoryTest implements WithAssertions {
    private ConnectedClientFactory subject;
    private TestRegister register;

    @BeforeEach
    void setUp() {
        register = new TestRegister();
        register.register("a", "a");
        subject = new ConnectedClientFactory(register);
    }

    @Nested
    class FromStaticFactoryMethod {
        @Test
        void shouldCreateConnectedClientWhenGivenWebSocket() {
            final WebSocket webSocket = new TestWebSocket("/lobby");

            final var client = subject.from(webSocket, "a");

            assertThat(client).isNotNull();
        }

        @Test
        void shouldReturnNewConnectedClientWhenGivenWasClosed() {
            final WebSocket webSocket = new TestWebSocket("/lobby");

            final var firstClient = subject.from(webSocket, "a");
            webSocket.close();
            final var secondClient = subject.from(webSocket, "a");

            assertThat(secondClient).isNotNull();
            assertThat(firstClient)
                    .isNotNull()
                    .isNotSameAs(secondClient);
        }

        @Test
        void shouldNotAllowToHaveMoreThanOneRegistrationAtTheSameTime() {
            final WebSocket firstWebSocket = new TestWebSocket("/lobby");
            final WebSocket secondWebSocket = new TestWebSocket("/lobby");

            final var firstClient = subject.from(firstWebSocket, "a");
            final var throwable = catchThrowable(() -> subject.from(secondWebSocket, "a"));

            assertThat(firstClient).isNotNull();
            assertThat(throwable)
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Already authenticated");
        }

        @Test
        void shouldThrowExceptionWhenUsernameIsNotRegistered() {
            final WebSocket webSocket = new TestWebSocket("/lobby");

            final var throwable = catchThrowable(() -> subject.from(webSocket, "b"));

            assertThat(throwable)
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Username wasn't registered");
        }

        @Test
        void shouldThrowRuntimeWhenTheSameWebSocketTriesToPlaysWithDifferentUsername() {
            final WebSocket webSocket = new TestWebSocket("/lobby");

            final var firstClient = subject.from(webSocket, "a");
            register.register("b", "b");
            final var throwable = catchThrowable(() -> subject.from(webSocket, "b"));

            assertThat(firstClient).isNotNull();
            assertThat(throwable)
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Already authenticated");
        }
    }

    @Nested
    class FindByStaticMethod {
        @Test
        void shouldFindByWebSocket() {
            final WebSocket webSocket = new TestWebSocket("/lobby");
            subject.from(webSocket, "a");

            final var client = subject.findBy(webSocket);

            assertThat(client)
                    .get()
                    .isNotNull();
        }

        @Test
        void shouldReturnCachedConnectedClientWhenGivenTheSameWebSocket() {
            final WebSocket webSocket = new TestWebSocket("/lobby");
            subject.from(webSocket, "a");

            final var firstClient = subject.findBy(webSocket);
            final var secondClient = subject.findBy(webSocket);

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
            subject.from(webSocket, "a");

            final var client = subject.findByUsername("a");

            assertThat(client)
                    .get()
                    .isNotNull();
        }

        @Test
        void shouldReturnCachedConnectedClientWhenAskedForTheSameUsername() {
            final WebSocket webSocket = new TestWebSocket("/lobby");
            subject.from(webSocket, "a");

            final var firstClient = subject.findByUsername("a");
            final var secondClient = subject.findByUsername("a");

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
