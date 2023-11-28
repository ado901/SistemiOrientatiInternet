package it.unipr.soi23.game_web_server.service.impl;

import it.unipr.soi23.game_web_server.broker.GameDataBroker;
import it.unipr.soi23.game_web_server.broker.PlayerBroker;
import it.unipr.soi23.game_web_server.model.*;
import it.unipr.soi23.game_web_server.repo.PersistenceRepo;
import it.unipr.soi23.game_web_server.service.Soi23GameWebServerService;
import org.springframework.messaging.core.MessageSendingOperations;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static it.unipr.soi23.game_web_server.utils.Soi23GameWebServerConst.*;

@Service
public class Soi23GameWebServerServiceImpl implements Soi23GameWebServerService {

    private final MessageSendingOperations<String> messageSendingOperations;
    private final PersistenceRepo persistenceRepo;

    public Soi23GameWebServerServiceImpl( //
                                          MessageSendingOperations<String> messageSendingOperations, //
                                          PersistenceRepo persistenceRepo //
    ) {
        this.messageSendingOperations = messageSendingOperations;
        this.persistenceRepo = persistenceRepo;
    }

    @Override
    public WatchGameResponse watchGame(String gameId) {
        final GameData gameData = persistenceRepo.findGameData().findById(gameId).orElse(null);
        if (gameData == null) {
            return new WatchGameResponse().message(new Message() //
                    .type(Message.Type.ERROR) //
                    .code(Message.Code.GAME_NOT_FOUND));
        }
        final Iterable<Player> players = persistenceRepo.findPlayer().findAllByGameId(gameId);

        final WatchGameResponse response = new WatchGameResponse() //
                .teamsScore(gameData.getTeamsScore()) //
                .ballAnimation(gameData.getBallAnimation());
        players.forEach(response::addPlayer);

        return response;
    }

    @Override
    public RegisterResponse register(String gameId, String playerId, String token) {
        final GameData gameDataRead = persistenceRepo.findGameData().findById(gameId).orElse(null);
        final boolean gameDataFound = gameDataRead != null;
        final GameData gameData = gameDataFound ? gameDataRead : new GameData(gameId);
        final String fullPlayerId = retrieveFullPlayerId(gameId, playerId);
        final Player playerRead = persistenceRepo.findPlayer().findById(fullPlayerId).orElse(null);
        if ((!token.equals("null")) && playerRead!=null){
            final boolean isValidToken = new PlayerBroker() //
                    .player(playerRead) //
                    .checkPlayerToken(token);
            if (playerRead.getGameId().equals(gameId) && isValidToken && playerRead!=null){
                messageSendingOperations.convertAndSend( //
                        TOPIC_GAME_PREFIX + gameId + TOPIC_GAME_PLAYERS_SUFFIX, //
                        new PlayerDTO().fromPlayer(playerRead));
                return new RegisterResponse() //
                        .teamsScore(gameData.getTeamsScore()) //
                        .ballAnimation(gameData.getBallAnimation()) //
                        .token(token);

            }
        }
        final Player player = createPlayer(gameId, playerId, gameData);
        if (player == null) {
            return new RegisterResponse().message(new Message() //
                    .type(Message.Type.ERROR) //
                    .code(Message.Code.PLAYER_ID_ALREADY_USED));
        }

        if (gameData.isPlaying()) {
            player.setReadyToStart(true);
        }
        final Iterable<Player> players = persistenceRepo.findPlayer().findAllByGameId(gameId);
        players.forEach(p -> {
            if (!p.getId().equals(player.getId())) {
                sendMessage(gameId, p.getToken(), Message.Type.INFO, Message.Code.NEW_PLAYER_JOINED);
            }
        });

        if (!gameDataFound) {
            persistenceRepo.insertGameData().gameData(gameData).apply();
        }
        messageSendingOperations.convertAndSend( //
                TOPIC_GAME_PREFIX + gameId + TOPIC_GAME_PLAYERS_SUFFIX, //
                new PlayerDTO().fromPlayer(player));
        final RegisterResponse response = new RegisterResponse() //
                .teamsScore(gameData.getTeamsScore()) //
                .ballAnimation(gameData.getBallAnimation()) //
                .token(player.getToken());
        players.forEach(response::addPlayer);

        return response;
    }

    @Override
    public GameDataDTO startGame(String gameId, StartGameRequest request) {
        final String playerId = retrieveFullPlayerId(gameId, request.getPlayerId());
        /* TODO
        Update the readyToStart property of the player and send the new Player
        to the front-end.
        Then, start the game if it can, or reset its ballAnimation to BALL_LOBBY_ANIMATION.
        Finally return the new GameData.
        NOTE: Keep in mind to persist the updates in the repository
         */
        GameData gameData = retrieveGameData(gameId, request.getToken());
        Player player = retrievePlayer(gameId, playerId, request.getToken());
        player.setReadyToStart(true);
        persistenceRepo.updatePlayer(playerId).player(player).apply();
        messageSendingOperations.convertAndSend( //
                TOPIC_GAME_PREFIX + gameId + TOPIC_GAME_PLAYERS_SUFFIX, //
                new PlayerDTO().fromPlayer(player));
        if (canGameStart(gameId)) {
            new GameDataBroker() //
                    .gameData(gameData) //
                    .startGame();
            persistenceRepo.findPlayer().findAllByGameId(gameId).forEach(p ->
                sendMessage(gameId, p.getToken(), Message.Type.INFO, Message.Code.GAME_STARTED));
        } else {
            gameData.setBallAnimation(BALL_LOBBY_ANIMATION);
        }
        persistenceRepo.updateGameData(gameId).gameData(gameData).apply();
        return new GameDataDTO() //
                .teamsScore(gameData.getTeamsScore()) //
                .ballAnimation(gameData.getBallAnimation());
    }

    @Override
    public PlayerDTO movePlayer(String gameId, MovePlayerRequest request) {
        final String playerId = retrieveFullPlayerId(gameId, request.getPlayerId());
        /* TODO
        Update the Y value of the player using PlayerBroker and return it.
        NOTE: Keep in mind to persist the updates in the repository
         */
        Player player = retrievePlayer(gameId, playerId, request.getToken());
        System.out.println("SERVICE");
        System.out.println(player.getPlayerSpeed());
        System.out.println(player.getPlayerSpeed());
        new PlayerBroker().player(player).moveToY(request.getY());
        persistenceRepo.updatePlayer(playerId).player(player).apply();
        return new PlayerDTO().fromPlayer(player);
    }

    @Override
    public BallAnimation animationEnd(String gameId) {
        final GameData gameData = retrieveGameData(gameId, null);
        final Iterable<Player> players = persistenceRepo.findPlayer().findAllByGameId(gameId);
        final GameDataBroker.UpdateAnimationResult updateAnimationResult = new GameDataBroker() //
                .players(players) //
                .gameData(gameData) //
                .updateAnimation();

        /* TODO
        If updateAnimation result is SCORE or NEXT, return the new ballAnimation.
        If updateAnimation result is SCORE:
            - Reset every player's readyToStart
            - Send to the front-end every new Player
            - Send to every player the message PointScored
        NOTE: Keep in mind to persist the updates in the repository
         */
        if (updateAnimationResult == GameDataBroker.UpdateAnimationResult.SCORE || updateAnimationResult == GameDataBroker.UpdateAnimationResult.NEXT) {
            persistenceRepo.updateGameData(gameId).gameData(gameData).apply();
            if (updateAnimationResult == GameDataBroker.UpdateAnimationResult.SCORE) {
                players.forEach(player -> {
                    player.setReadyToStart(false);
                    persistenceRepo.updatePlayer(player.getId()).player(player).apply();
                    messageSendingOperations.convertAndSend( //
                            TOPIC_GAME_PREFIX + gameId + TOPIC_GAME_PLAYERS_SUFFIX, //
                            new PlayerDTO().fromPlayer(player));
                    sendMessage(gameId, player.getToken(), Message.Type.INFO, Message.Code.POINT_SCORED);
                });
                messageSendingOperations.convertAndSend( //
                        TOPIC_GAME_PREFIX + gameId + TOPIC_GAME_SCORE_SUFFIX, //
                        gameData.getTeamsScore());
            }
            return gameData.getBallAnimation();
        }
        return null;
    }

    // Private

    private String retrieveFullPlayerId(String gameId, String playerId) {
        return playerId.concat(PLAYER_ID_SEPARATOR).concat(gameId);
    }

    private GameData retrieveGameData(String gameId, String token) {
        final GameData gameData = persistenceRepo.findGameData().findById(gameId).orElse(null);
        if (gameData == null) {
            /* TODO
            Send a message to the player to notify the error
             */
            sendMessage(gameId, token, Message.Type.ERROR, Message.Code.GAME_NOT_FOUND);
            throw new GameWebServerException(GAME_NOT_FOUND + gameId);
        }
        return gameData;
    }
    private void changeTeamHeight(Player.Team team, String gameId, GameData gameData){
        Iterable<Player> players = persistenceRepo.findPlayer().findAllByGameId(gameId);
        int counter;
        if (team == Player.Team.LEFT)
            counter = gameData.getGameSettings().getLeftCount();
        else
            counter = gameData.getGameSettings().getRightCount();
        if (counter<=0){
            return;
        }
        else if (counter>4){
            counter=4;
        }
        int value = PLAYER_HEIGHT/counter;
        if (team == Player.Team.LEFT)
            gameData.getGameSettings().setLeftHeight(value);
        else
            gameData.getGameSettings().setRightHeight(value);
        persistenceRepo.updateGameData(gameId).gameData(gameData).apply();
        players.forEach(player -> {
            if (player.getTeam() == team) {
            player.setPlayerheight(value);
            persistenceRepo.updatePlayer(player.getId()).player(player).apply();
            messageSendingOperations.convertAndSend( //
                    TOPIC_GAME_PREFIX + gameId + TOPIC_GAME_PLAYERS_SUFFIX, //
                    new PlayerDTO().fromPlayer(player));
        }});


    }

    private Player retrievePlayer(String gameId, String playerId, String token) {
        final Player player = persistenceRepo.findPlayer().findById(playerId).orElse(null);
        System.out.println("RETRIEVE PLAYER 1");
        System.out.println(player.getPlayerSpeed());
        System.out.println(player.getPlayerHeight());
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

    private Player createPlayer(String gameId, String playerId, GameData gameData) {
        final String fullPlayerId = retrieveFullPlayerId(gameId, playerId);
        final boolean playerAlreadyExists = persistenceRepo.findPlayer().findById(fullPlayerId).isPresent();
        if (playerAlreadyExists) {
            return null;
        }

        final String token = UUID.randomUUID().toString();
        /* TODO
        Instantiate and return the new Player.
        NOTE: Use fullPlayerId as id
         */
        Iterable<Player> players = persistenceRepo.findPlayer().findAllByGameId(gameId);
        Player.Team choice;
        if (players.spliterator().estimateSize() % 2 == 0) {
            gameData.getGameSettings().setLeftCount(gameData.getGameSettings().getLeftCount() + 1);
            choice = Player.Team.LEFT;
        } else {
            gameData.getGameSettings().setRightCount(gameData.getGameSettings().getRightCount() + 1);
            choice = Player.Team.RIGHT;
        }
        Player player = new Player().token(token).id(fullPlayerId).gameId(gameId).y(PLAYFIELD_HEIGHT / 2).team(choice);
        persistenceRepo.insertPlayer().player(player).apply();
        changeTeamHeight(choice, gameId, gameData);
        return player;
    }

    private boolean canGameStart(String gameId) {
        /* TODO
        The game can start if:
            - All players are ready to start
            - There is at least one player for each team (side)
         */
        boolean leftTeam = false;
        boolean rightTeam = false;
        Iterable<Player> players = persistenceRepo.findPlayer().findAllByGameId(gameId);
        for (Player player : players) {
            if (!player.isReadyToStart()) {
                return false;
            }
            if (player.getTeam() == Player.Team.LEFT) {
                leftTeam = true;
            } else {
                rightTeam = true;
            }
        }
        return leftTeam && rightTeam;
    }

    private void sendMessage(String gameId, String token, Message.Type type, Message.Code code) {
        final Message msg = new Message() //
                .type(type) //
                .code(code);
        messageSendingOperations.convertAndSend( //
                TOPIC_GAME_PREFIX + gameId + TOPIC_GAME_MESSAGES_SUFFIX + token, //
                msg);
    }
    public GameDataDTO changeTeam(String gameId, ChangeTeamRequest request) {
        /**
         * 1. controlla se la richiesta è valida e che la squadra scelta non sia quella attuale.
         * 2. poi cambia la squadra e aggiorna il counter per la size dei player per squadra
         * 3. aggiorna l'altezza dei player per squadra
         */
        final String playerId = retrieveFullPlayerId(gameId, request.getPlayerId());
        GameData gameData = retrieveGameData(gameId, request.getToken());
        Player player = retrievePlayer(gameId, playerId, request.getToken());
        Player.Team team = player.getTeam();
        if (request.getTeam()==team.ordinal())
            return new GameDataDTO() //
                    .teamsScore(gameData.getTeamsScore()) //
                    .ballAnimation(gameData.getBallAnimation());
        else if (request.getTeam() == 0 && team == Player.Team.RIGHT) {
            gameData.getGameSettings().setLeftCount(gameData.getGameSettings().getLeftCount() + 1);
            gameData.getGameSettings().setRightCount(gameData.getGameSettings().getRightCount() - 1);
            team = Player.Team.LEFT;

        } else if (request.getTeam()==1 && team == Player.Team.LEFT) {
            gameData.getGameSettings().setLeftCount(gameData.getGameSettings().getLeftCount() - 1);
            gameData.getGameSettings().setRightCount(gameData.getGameSettings().getRightCount() + 1);
            team = Player.Team.RIGHT;
        }
        Player.Team oldTeam = player.getTeam();
        new PlayerBroker().player(player).changeTeam(team);
        persistenceRepo.updatePlayer(playerId).player(player).apply();
        changeTeamHeight(team, gameId, gameData);
        changeTeamHeight(oldTeam, gameId, gameData);

        return new GameDataDTO() //
                .teamsScore(gameData.getTeamsScore()) //
                .ballAnimation(gameData.getBallAnimation());
    }

    public GameDataDTO changeSettings(String gameid, String playerid, ChangeSettingsRequest request){
        final String playerId = retrieveFullPlayerId(gameid, playerid);
        Player player = retrievePlayer(gameid, playerId, request.getToken());
        GameData gameData = retrieveGameData(gameid, request.getToken());
        new PlayerBroker().player(player).changeSettings(request.getPlayerSpeed(), request.getPlayerSize());
        messageSendingOperations.convertAndSend( //
                TOPIC_GAME_PREFIX + gameid + TOPIC_GAME_PLAYERS_SUFFIX, //
                new PlayerDTO().fromPlayer(player));
        persistenceRepo.updatePlayer(playerId).player(player).apply();
        return new GameDataDTO() //
                .teamsScore(gameData.getTeamsScore()) //
                .ballAnimation(gameData.getBallAnimation());
    }
}
