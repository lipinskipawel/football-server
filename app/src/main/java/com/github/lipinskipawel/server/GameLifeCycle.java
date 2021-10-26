package com.github.lipinskipawel.server;

import com.github.lipinskipawel.api.move.GameMove;
import com.github.lipinskipawel.board.engine.Direction;
import com.github.lipinskipawel.board.engine.Move;
import com.github.lipinskipawel.domain.GameBoardState;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is responsible for game life cycle.
 * It manages objects like {@link GameBoardState} and {@link DualConnection} in order to provide game play capabilities.
 * This class ensures correctness of the user experience during playing game by synchronizing state between game logic
 * and sending messages to players.
 */
final class GameLifeCycle {
    private final DualConnection dualConnection;
    private final Parser parser;
    private GameBoardState boardState;

    public GameLifeCycle(DualConnection dualConnection, Parser parser) {
        this.dualConnection = dualConnection;
        this.parser = parser;
    }

    public void makeMove(final GameMove gameMove, final ConnectedClient client) {
        final var move = new Move(toDirectionList(gameMove.getMove()));
        final var isMade = boardState.makeMoveBy(move, client);
        if (isMade) {
            final var jsonMove = parser.toJson(gameMove);
            dualConnection.sendMessageTo(jsonMove, client);
        }
    }

    private List<Direction> toDirectionList(List<String> listOfDirections) {
        return listOfDirections.stream().map(Direction::valueOf).collect(Collectors.toList());
    }

    boolean accept(final ConnectedClient client) {
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
