package com.github.lipinskipawel.domain;

import com.github.lipinskipawel.api.Player;
import com.github.lipinskipawel.api.game.GameEnd;
import com.github.lipinskipawel.api.move.AcceptMove;
import com.github.lipinskipawel.api.move.GameMove;
import com.github.lipinskipawel.api.move.RejectMove;
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
    private static final AcceptMove ACCEPT_MOVE = new AcceptMove();
    private final DualConnection dualConnection;
    private final Parser parser;
    private GameBoardState boardState;

    private GameLifeCycle(DualConnection dualConnection, Parser parser) {
        this.dualConnection = dualConnection;
        this.parser = parser;
    }

    public static GameLifeCycle of(final Parser parser) {
        return new GameLifeCycle(new DualConnection(), parser);
    }

    public void makeMove(final GameMove gameMove, final ConnectedClient client) {
        final var move = new Move(toDirectionList(gameMove.getMove()));
        var isMade = false;
        synchronized (this) {
            isMade = boardState.makeMoveBy(move, client);
        }
        if (isMade) {
            final var jsonMove = parser.toJson(gameMove);
            dualConnection.sendMessageFrom(jsonMove, client);
            final var jsonAccept = parser.toJson(ACCEPT_MOVE);
            dualConnection.sendMessageTo(jsonAccept, client);
        } else {
            final var jsonReject = parser.toJson(new RejectMove(gameMove));
            dualConnection.sendMessageTo(jsonReject, client);
        }
        if (boardState.isGameOver()) {
            final var jsonWinner = parser.toJson(new GameEnd(Player.fromUsername(boardState.getWinner())));
            dualConnection.sendMessageTo(jsonWinner, client);
            dualConnection.sendMessageFrom(jsonWinner, client);
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
        final var bothClients = dualConnection.areBothClientsConnected();
        if (bothClients) {
            synchronized (this) {
                boardState = GameBoardState.aGameBoardState()
                        .withFirstPlayer(dualConnection.first())
                        .withSecondPlayer(dualConnection.second())
                        .build();
            }
        }
        return isAccepted;
    }
}
