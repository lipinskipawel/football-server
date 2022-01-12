package com.github.lipinskipawel.domain;

import com.github.lipinskipawel.api.move.GameMove;
import com.github.lipinskipawel.user.ConnectedClient;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

/**
 * This is an entrypoint for registering and playing a football game.
 * This class holds responsibility for:
 * - creating new game by defining endpoint to connect to
 * - passing moves made by {@link ConnectedClient} to the game
 * - passing information about {@link ConnectedClient} being disconnected from the game
 * <p>
 */
public final class ActiveGames {
    /**
     * Each game is registered under the unique url
     */
    private final Map<String, GameLifeCycle> gamesPerUrl;
    private final RedirectEndpoint redirect;

    ActiveGames(Map<String, GameLifeCycle> gamesPerUrl) {
        this.gamesPerUrl = gamesPerUrl;
        this.redirect = new RedirectEndpoint();
    }

    public static ActiveGames of() {
        return new ActiveGames(new HashMap<>());
    }

    /**
     * This method is intended to create a new game for two players. Each game is registered under unique url. The order
     * of parameters doesn't matter in a sense of first player will not be guaranteed to move first.
     *
     * @param first  client that want to play
     * @param second client that want to play
     * @return url under the players will play
     * @implNote This method will create a new ({@link GameLifeCycle}) object representing game
     */
    public String createNewGame(final ConnectedClient first, final ConnectedClient second) {
        final var url = this.redirect.createNewRedirectEndpoint(first.getUsername(), second.getUsername());
        this.gamesPerUrl.put(url, GameLifeCycle.of(new Gson()::toJson));
        return url;
    }

    /**
     * This method will allow given {@link ConnectedClient} client to connect to the game. This method will not allow
     * connecting to the game which was not yet created via
     * {@link ActiveGames#createNewGame(ConnectedClient, ConnectedClient)}.
     *
     * @param urlOfTheGame that client is trying to connect to
     * @param client       that tries to connect to the game
     * @return true or false, true on successful connection otherwise false
     */
    public boolean accept(final String urlOfTheGame, final ConnectedClient client) {
        final var canPlay = this.redirect.canJoin(client.getUsername(), urlOfTheGame);
        if (!canPlay) {
            return false;
        }
        var gameLifeCycle = this.gamesPerUrl.get(urlOfTheGame);
        if (gameLifeCycle == null) {
            return false;
        }
        return gameLifeCycle.accept(client);
    }

    /**
     * This method will attempt to make a move by the given client. Whether the move has been made or not is in power of
     * the underlying game object. Any game state communication with the clients such as
     * {@link com.github.lipinskipawel.api.move.GameMove}, {@link com.github.lipinskipawel.api.move.AcceptMove} and
     * {@link com.github.lipinskipawel.api.game.GameEnd} is handled by the underlying object.
     *
     * @param urlOfTheGame of the game
     * @param move         that is about to be made
     * @param client       that is attempting to make a move
     */
    public void registerMove(final String urlOfTheGame, final GameMove move, final ConnectedClient client) {
        this.gamesPerUrl.get(urlOfTheGame).makeMove(move, client);
    }

    /**
     * This method will mark given client as disconnected from the game. Any game state communication with the client
     * around that fact will be handled by the underlying object.
     *
     * @param urlOfTheGame that the client was connected to
     * @param client       that has been disconnected
     */
    public void dropConnectionFor(final String urlOfTheGame, final ConnectedClient client) {
        final var gameLifeCycle = this.gamesPerUrl.get(urlOfTheGame);
        if (gameLifeCycle != null) {
            gameLifeCycle.dropConnectionFor(client);
        }
    }
}
