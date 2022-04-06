package com.github.lipinskipawel.register;

import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

final class RegisterHandlerTest implements WithAssertions {

    @Test
    void shouldRegisterUsername() {
        final var channel = new EmbeddedChannel(RegisterEntrypoint.createRegister(new Register(() -> "123")));
        final var requestToRegister = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/register");
        requestToRegister.headers().add("username", "john");
        channel.writeInbound(requestToRegister);

        final var readOut = channel.readOutbound();

        assertThat(readOut).isInstanceOf(HttpResponse.class);
        final var httpResponse = (HttpResponse) readOut;
        assertThat(httpResponse)
                .extracting(HttpResponse::status)
                .isEqualTo(HttpResponseStatus.OK);
        assertThat(httpResponse)
                .extracting(it -> it.headers().get("token"))
                .asString()
                .isEqualTo("123");
    }

    @Test
    void shouldNotRegisterUsernameThatWasAlreadyRegistered() {
        final var channel = new EmbeddedChannel(RegisterEntrypoint.createRegister(new Register(() -> "123")));
        final var requestToRegister = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/register");
        requestToRegister.headers().add("username", "john");
        channel.writeInbound(requestToRegister, requestToRegister);

        channel.readOutbound();
        final var readOut = channel.readOutbound();

        assertThat(readOut).isInstanceOf(HttpResponse.class);
        final var httpResponse = (HttpResponse) readOut;
        assertThat(httpResponse)
                .extracting(HttpResponse::status)
                .isEqualTo(HttpResponseStatus.UNAUTHORIZED);
    }
}
