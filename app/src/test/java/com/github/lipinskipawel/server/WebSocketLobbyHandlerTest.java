package com.github.lipinskipawel.server;

import com.github.lipinskipawel.domain.game.ActiveGames;
import com.github.lipinskipawel.domain.lobby.Lobby;
import com.github.lipinskipawel.server.mocks.TestRegister;
import com.github.lipinskipawel.user.ConnectedClientFactory;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

class WebSocketLobbyHandlerTest implements WithAssertions {
    private final Lobby lobby = Lobby.of(Object::toString);
    private final ActiveGames activeGames = ActiveGames.of(Object::toString);
    private final TestRegister register = new TestRegister();
    private final ConnectedClientFactory factory = new ConnectedClientFactory(register);

    @Test
    void shouldPassGameMoveWithoutReleasingMessage() {
        register.register("token");
        final var channel = new EmbeddedChannel(new WebSocketLobbyHandler(lobby, activeGames, factory));
        factory.from(channel, "token");
        final var frame = new TextWebSocketFrame("""
                {move="N"}
                """);

        channel.writeInbound(frame);

        assertThat(frame.refCnt()).isEqualTo(1);
    }
}