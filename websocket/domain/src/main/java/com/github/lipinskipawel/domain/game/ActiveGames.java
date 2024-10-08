package com.github.lipinskipawel.domain.game;

import com.github.lipinskipawel.api.move.GameMove;
import com.github.lipinskipawel.user.ConnectedClient;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This is an entrypoint for registering and playing a football game.
 * This class holds responsibility for:
 * - creating new game by defining endpoint to connect to
 * - passing moves made by {@link ConnectedClient} to the game
 * - passing information about {@link ConnectedClient} being disconnected from the game
 * <p>
 */
public final class ActiveGames {
    private static final String BASE_GAME_URL = "/ws/game/";
    /**
     * Each game is registered under the url
     */
    private final Map<String, GameLifeCycle> gamesPerUrl;

    ActiveGames(Map<String, GameLifeCycle> gamesPerUrl) {
        this.gamesPerUrl = gamesPerUrl;
    }

    /**
     * Public factory method for creating new object of {@link ActiveGames}.
     *
     * @return activeGame object
     */
    public static ActiveGames of() {
        return new ActiveGames(new HashMap<>());
    }

    /**
     * This method is intended to create a new game for two players. Each game is registered under url. The order of
     * parameters doesn't matter in a sense of first player will not be guaranteed to move first.
     *
     * @param first  client that want to play
     * @param second client that want to play
     * @return url under the players will play
     * @implNote This method will create a new ({@link GameLifeCycle}) object representing game
     */
    public String createNewGame(final ConnectedClient first, final ConnectedClient second) {
        final var url = BASE_GAME_URL.concat(UUID.randomUUID().toString());
        this.gamesPerUrl.put(url, GameLifeCycle.of(first.getUsername(), second.getUsername()));
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
        var gameLifeCycle = this.gamesPerUrl.get(urlOfTheGame);
        if (gameLifeCycle == null) {
            return false;
        }
        return gameLifeCycle.accept(client);
    }

    /**
     * This method will attempt to make a move by the given client. Whether the move has been made or not is in power of
     * the underlying game object. Any game state communication with the clients such as
     * {@link GameMove}, {@link com.github.lipinskipawel.api.move.AcceptMove} and
     * {@link com.github.lipinskipawel.api.game.GameEnd} is handled by the underlying object.
     *
     * @param urlOfTheGame of the game
     * @param move         that is about to be made
     * @param client       that is attempting to make a move
     */
    public void registerMove(final String urlOfTheGame, final GameMove move, final ConnectedClient client) {
        this.gamesPerUrl.get(urlOfTheGame).tryMakeMove(move, client);
    }

    /**
     * This method will attempt to make a move by the given client. Whether the move has been made or not is in power of
     * the underlying game object. Any game state communication with the clients such as
     * {@link GameMove}, {@link com.github.lipinskipawel.api.move.AcceptMove} and
     * {@link com.github.lipinskipawel.api.game.GameEnd} is handled by the underlying object.
     * <p>
     * This method will throw {@link RuntimeException} every time when number of active games by the client is different
     * from 1.
     *
     * @param move   that s about to be made
     * @param client that is attempting to make a move
     * @throws RuntimeException when client is playing more than one game
     */
    public void registerMove(final GameMove move, final ConnectedClient client) {
        final var games = this.gamesPerUrl
            .values()
            .stream()
            .filter(gameLifeCycle -> gameLifeCycle.isClientAllowedToPlay(client))
            .toList();
        if (games.size() == 1) {
            games.get(0).tryMakeMove(move, client);
            return;
        }
        throw new RuntimeException("We do not support multiple games played at the same time by the user");
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
