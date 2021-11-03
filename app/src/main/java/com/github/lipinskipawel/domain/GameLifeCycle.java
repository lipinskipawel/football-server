package com.github.lipinskipawel.domain;

import com.github.lipinskipawel.api.move.GameMove;
import com.github.lipinskipawel.board.engine.Direction;
import com.github.lipinskipawel.board.engine.Move;
import com.github.lipinskipawel.server.Parser;
import com.github.lipinskipawel.user.ConnectedClient;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is responsible for game life cycle.
 * It manages objects like {@link GameBoardState} and {@link DualConnection} in order to provide game play capabilities.
 * This class ensures correctness of the user experience during playing game by synchronizing state between game logic
 * and sending messages to players.
 */
public final class GameLifeCycle {
    private final DualConnection dualConnection;
    private final Parser parser;
    private GameBoardState boardState;

    GameLifeCycle(DualConnection dualConnection, Parser parser) {
        this.dualConnection = dualConnection;
        this.parser = parser;
    }

    public static GameLifeCycle of(final Parser parser) {
        return new GameLifeCycle(new DualConnection(), parser);
    }

    public void makeMove(final GameMove gameMove, final ConnectedClient client) {
        final var move = new Move(toDirectionList(gameMove.getMove()));
        final var isMade = boardState.makeMoveBy(move, client);
        if (isMade) {
            final var jsonMove = parser.toJson(gameMove);
            dualConnection.sendMessageFrom(jsonMove, client);
        }
    }

    public void dropConnectionFor(final ConnectedClient client) {
        dualConnection.dropConnectionFor(client);
    }

    private List<Direction> toDirectionList(List<String> listOfDirections) {
        return listOfDirections.stream().map(Direction::valueOf).collect(Collectors.toList());
    }

    public boolean accept(final ConnectedClient client) {
        final var isAccepted = dualConnection.accept(client);
        final var bothClients = dualConnection.getBothClients(client.getUrl());
        if (bothClients.size() == 2) {
            boardState = GameBoardState.aGameBoardState()
                    .withFirstPlayer(bothClients.get(0))
                    .withSecondPlayer(bothClients.get(1))
                    .build();
        }
        return isAccepted;
    }
}
