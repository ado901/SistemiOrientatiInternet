package it.unipr.soi23.game_web_server.service.impl;

import it.unipr.soi23.game_web_server.broker.BallAnimationBroker;
import it.unipr.soi23.game_web_server.broker.GameDataBroker;
import it.unipr.soi23.game_web_server.model.*;
import it.unipr.soi23.game_web_server.service.Soi23GameWebServerService;
import org.springframework.messaging.core.MessageSendingOperations;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static it.unipr.soi23.game_web_server.utils.Soi23GameWebServerConst.*;
import static java.lang.Math.abs;

@Service
public class Soi23GameWebServerServiceImpl implements Soi23GameWebServerService {

    private final MessageSendingOperations<String> messageSendingOperations;

    private final Map<String, GameData> gameDataMap;

    public Soi23GameWebServerServiceImpl(MessageSendingOperations<String> messageSendingOperations) {
        this.messageSendingOperations = messageSendingOperations;
        this.gameDataMap = new HashMap<>();
    }

    @Override
    public WatchGameResponse watchGame(String gameId) {
        final GameData gameData = retrieveGameData(gameId);

        final WatchGameResponse response = new WatchGameResponse() //
                .teamsScore(gameData.getTeamsScore()) //
                .ballAnimation(gameData.getBallAnimation());
        gameData.getPlayers().forEach(response::addPlayer);

        return response;
    }

    @Override
    public RegisterResponse register(String gameId, String playerId) {
        final GameData gameDataRead = gameDataMap.getOrDefault(gameId, null);
        final boolean gameDataFound = gameDataRead != null;
        final GameData gameData = gameDataFound ? gameDataRead : new GameData(gameId);

        final Player player = createPlayer(gameData, playerId);
        if (player == null) {
            throw new GameWebServerException(PLAYER_ID_ALREADY_USED + gameId);
        }

        if (gameData.isPlaying()) {
            player.setReadyToStart(true);
        }
        gameData.getPlayers().add(player);
        if (!gameDataFound) {
            gameDataMap.put(gameId, gameData);
        }

        messageSendingOperations.convertAndSend( //
                TOPIC_GAME_PREFIX + gameId + TOPIC_GAME_PLAYERS_SUFFIX, //
                new PlayerDTO().fromPlayer(player));

        final RegisterResponse response = new RegisterResponse() //
                .teamsScore(gameData.getTeamsScore()) //
                .ballAnimation(gameData.getBallAnimation());
        gameData.getPlayers().forEach(response::addPlayer);

        return response;
    }

    @Override
    public GameDataDTO startGame(String gameId, StartGameRequest request) {
        /* TODO
        Update the readyToStart property of the player and send the new Player
        to the front-end.
        Then, start the game if it can, or reset its ballAnimation to BALL_LOBBY_ANIMATION.
        Finally return the new GameData
         */

        GameData gameData = retrieveGameData(gameId);
        String playerId = request.getPlayerId();
        Player player = retrievePlayer(gameData, playerId);
        if (player.isReadyToStart() && gameData.isPlaying())
            return new GameDataDTO().teamsScore(gameData.getTeamsScore()).ballAnimation(gameData.getBallAnimation());
        player.setReadyToStart(true);
        messageSendingOperations.convertAndSend( //
                TOPIC_GAME_PREFIX + gameId + TOPIC_GAME_PLAYERS_SUFFIX, //
                new PlayerDTO().fromPlayer(player));
        if (canGameStart(gameData)) {
            gameData.setPlaying(true);
            gameData.setBallAnimation(new BallAnimationBroker() //
                    .x(BALL_INITIAL_X) //
                    .y(BALL_INITIAL_Y) //
                    .direction(BALL_INITIAL_DIRECTION) //
                    .build());
        }
        else {
            gameData.setBallAnimation(BALL_LOBBY_ANIMATION);
        }
        return new GameDataDTO() //
                .teamsScore(gameData.getTeamsScore()) //
                .ballAnimation(gameData.getBallAnimation());

    }

    @Override
    public PlayerDTO movePlayer(String gameId, MovePlayerRequest request) {
        /* TODO
        Update the Y value of the player and return it
         */
        GameData gameData = retrieveGameData(gameId);
        String playerId = request.getPlayerId();
        Player player = retrievePlayer(gameData, playerId);
        if (request.getY()>PLAYFIELD_HEIGHT-PLAYER_HEIGHT/2 || request.getY()<PLAYER_HEIGHT/2 || !gameData.isPlaying()
                || !player.isReadyToStart() ||  abs(player.getY()-request.getY())>PLAYER_SPEED)
            return new PlayerDTO().fromPlayer(player);
        player.setY(request.getY());
        return new PlayerDTO().fromPlayer(player);
    }

    @Override
    public BallAnimation animationEnd(String gameId) {
        final GameData gameData = retrieveGameData(gameId);
        final GameDataBroker.UpdateAnimationResult updateAnimationResult;
        // Non so se sia corretto ma sistema il problema del doppio punteggio
        synchronized (gameData) {
            updateAnimationResult = new GameDataBroker() //
                    .gameData(gameData) //
                    .updateAnimation();
        }

        /* TODO
        If updateAnimation result is SCORE or NEXT, return the new ballAnimation.
        If updateAnimation result is SCORE, reset every player's readyToStart
        and send to the front-end every new Player
         */
        if (updateAnimationResult==GameDataBroker.UpdateAnimationResult.SCORE || updateAnimationResult==GameDataBroker.UpdateAnimationResult.NEXT) {
            if (updateAnimationResult== GameDataBroker.UpdateAnimationResult.SCORE){
                gameData.getPlayers().forEach(player -> player.setReadyToStart(false));
                gameData.getPlayers().forEach(player -> messageSendingOperations.convertAndSend( //
                        TOPIC_GAME_PREFIX + gameId + TOPIC_GAME_PLAYERS_SUFFIX, //
                        new PlayerDTO().fromPlayer(player)));
            }
            return gameData.getBallAnimation();
        }
        return null;
    }

    // Private

    private GameData retrieveGameData(String gameId) {
        final GameData gameData = gameDataMap.getOrDefault(gameId, null);
        if (gameData == null) {
            throw new GameWebServerException(GAME_NOT_FOUND + gameId);
        }
        return gameData;
    }

    private Player retrievePlayer(GameData gameData, String playerId) {
        Player player = null;
        for (Player currPlayer : gameData.getPlayers()) {
            if (currPlayer.getId().equals(playerId)) {
                player = currPlayer;
            }
        }
        if (player == null) {
            throw new GameWebServerException(PLAYER_NOT_FOUND + playerId);
        }
        return player;
    }

    private Player createPlayer(GameData gameData, String playerId) {
        final boolean playerAlreadyExists = gameData.getPlayers() //
                .stream() //
                .anyMatch(player -> player.getId().equals(playerId));
        if (playerAlreadyExists) {
            return null;
        }

        /* TODO
        Instantiate and return the new Player
         */
        return new Player().id(playerId).y(PLAYFIELD_HEIGHT / 2).team(gameData.getPlayers().size() % 2 == 0 ? Player.Team.LEFT : Player.Team.RIGHT);
    }

    private boolean canGameStart(GameData gameData) {
        /* TODO
        The game can start if:
            - All players are ready to start
            - There is at least one player for each team (side)
         */
        boolean allReady = true;
        boolean leftTeam = false;
        boolean rightTeam = false;
        for (Player player : gameData.getPlayers()) {
            if (!player.isReadyToStart()) {
                allReady = false;
            }
            if (player.getTeam() == Player.Team.LEFT) {
                leftTeam = true;
            }
            if (player.getTeam() == Player.Team.RIGHT) {
                rightTeam = true;
            }
        }
        return allReady && leftTeam && rightTeam;
    }
}
