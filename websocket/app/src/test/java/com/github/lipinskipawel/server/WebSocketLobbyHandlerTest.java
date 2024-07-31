package com.github.lipinskipawel.server;

import com.github.lipinskipawel.client.AuthClient;
import com.github.lipinskipawel.client.StubAuthClient;
import com.github.lipinskipawel.domain.game.ActiveGames;
import com.github.lipinskipawel.domain.lobby.Lobby;
import com.github.lipinskipawel.user.ConnectedClientFactory;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

class WebSocketLobbyHandlerTest implements WithAssertions {
    private final Lobby lobby = Lobby.of();
    private final ActiveGames activeGames = ActiveGames.of();
    private final AuthClient authClient = new StubAuthClient();
    private final ConnectedClientFactory factory = new ConnectedClientFactory(authClient);

    @Test
    void shouldPassGameMoveWithoutReleasingMessage() {
        authClient.register("me");
        final var channel = new EmbeddedChannel(new WebSocketLobbyHandler(lobby, activeGames, factory));
        factory.from(channel, "me_token");
        final var frame = new TextWebSocketFrame("""
                {move="N"}
                """);

        channel.writeInbound(frame);

        assertThat(frame.refCnt()).isEqualTo(1);
    }
}