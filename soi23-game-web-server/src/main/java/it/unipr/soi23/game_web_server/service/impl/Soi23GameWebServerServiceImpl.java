package it.unipr.soi23.game_web_server.service.impl;

import it.unipr.soi23.game_web_server.broker.GameDataBroker;
import it.unipr.soi23.game_web_server.broker.PlayerBroker;
import it.unipr.soi23.game_web_server.model.*;
import it.unipr.soi23.game_web_server.service.Soi23GameWebServerService;
import org.springframework.messaging.core.MessageSendingOperations;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
        final GameData gameData = gameDataMap.getOrDefault(gameId, null);
        if (gameData == null) {
            return new WatchGameResponse().message(new Message() //
                    .type(Message.Type.ERROR) //
                    .code(Message.Code.GAME_NOT_FOUND));
        }

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
            return new RegisterResponse().message(new Message() //
                    .type(Message.Type.ERROR) //
                    .code(Message.Code.PLAYER_ID_ALREADY_USED));
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
                .ballAnimation(gameData.getBallAnimation()) //
                .token(player.getToken());
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
        GameData gameData = retrieveGameData(gameId, request.getToken());
        String playerId = request.getPlayerId();
        Player player = retrievePlayer(gameData,playerId, request.getToken());
        if (player.isReadyToStart() && gameData.isPlaying())
            return new GameDataDTO().teamsScore(gameData.getTeamsScore()).ballAnimation(gameData.getBallAnimation());
        player.setReadyToStart(true);
        messageSendingOperations.convertAndSend( //
                TOPIC_GAME_PREFIX + gameId + TOPIC_GAME_PLAYERS_SUFFIX, //
                new PlayerDTO().fromPlayer(player));
        if (canGameStart(gameData)) {
            new GameDataBroker().gameData(gameData).startGame();
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
        Update the Y value of the player using PlayerBroker and return it
         */
        GameData gameData = retrieveGameData(gameId, request.getToken());
        String playerId = request.getPlayerId();
        Player player = retrievePlayer(gameData,playerId, request.getToken());
        PlayerBroker playerBroker = new PlayerBroker().player(player);
        playerBroker.moveToY(request.getY());
        return new PlayerDTO().fromPlayer(player);

    }

    @Override
    public BallAnimation animationEnd(String gameId) {
        final GameData gameData = retrieveGameData(gameId, null);
        final GameDataBroker.UpdateAnimationResult updateAnimationResult = new GameDataBroker() //
                .gameData(gameData) //
                .updateAnimation();

        /* TODO
        If updateAnimation result is SCORE or NEXT, return the new ballAnimation.
        If updateAnimation result is SCORE:
            - Reset every player's readyToStart
            - Send to the front-end every new Player
            - Send to every player the message PointScored
         */
        if (updateAnimationResult==GameDataBroker.UpdateAnimationResult.SCORE || updateAnimationResult==GameDataBroker.UpdateAnimationResult.NEXT) {
            if (updateAnimationResult== GameDataBroker.UpdateAnimationResult.SCORE){
                gameData.getPlayers().forEach(player -> player.setReadyToStart(false));
                gameData.getPlayers().forEach(player -> messageSendingOperations.convertAndSend( //
                        TOPIC_GAME_PREFIX + gameId + TOPIC_GAME_PLAYERS_SUFFIX, //
                        new PlayerDTO().fromPlayer(player)));
                gameData.getPlayers().forEach(player -> sendMessage(gameId, player.getToken(), Message.Type.INFO, Message.Code.POINT_SCORED));

            }
            return gameData.getBallAnimation();
        }
        return null;
    }

    // Private

    private GameData retrieveGameData(String gameId, String token) {
        final GameData gameData = gameDataMap.getOrDefault(gameId, null);
        if (gameData == null) {
            /* TODO
            Send a message to the player to notify the error
             */
            sendMessage(gameId, token, Message.Type.ERROR, Message.Code.GAME_NOT_FOUND);
            throw new GameWebServerException(GAME_NOT_FOUND + gameId);
        }
        return gameData;
    }

    private Player retrievePlayer(GameData gameData, String playerId, String token) {
        Player player = null;
        for (Player currPlayer : gameData.getPlayers()) {
            if (currPlayer.getId().equals(playerId)) {
                player = currPlayer;
            }
        }
        final String gameId = gameData.getId();
        if (player == null) {
            /* TODO
            Send a message to the player to notify the error
             */
            sendMessage(gameId, token, Message.Type.ERROR, Message.Code.PLAYER_NOT_FOUND);
            throw new GameWebServerException(PLAYER_NOT_FOUND + playerId);
        }
        final boolean isValidToken = new PlayerBroker() //
                .player(player) //
                .checkPlayerToken(token);
        if (!isValidToken) {
            /* TODO
            Send a message to the player to notify the error
             */
            sendMessage(gameId, token, Message.Type.ERROR, Message.Code.INVALID_PLAYER_TOKEN);

            throw new GameWebServerException(INVALID_PLAYER_TOKEN + playerId);
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

        final String token = UUID.randomUUID().toString();
        /* TODO
        Instantiate and return the new Player
         */
        return new Player().id(playerId).y(PLAYFIELD_HEIGHT / 2).team(gameData.getPlayers().size() % 2 == 0 ? Player.Team.LEFT : Player.Team.RIGHT).token(token);
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

    private void sendMessage(String gameId, String token, Message.Type type, Message.Code code) {
        final Message msg = new Message() //
                .type(type) //
                .code(code);
        messageSendingOperations.convertAndSend( //
                TOPIC_GAME_PREFIX + gameId + TOPIC_GAME_MESSAGES_SUFFIX + token, //
                msg);
    }}
