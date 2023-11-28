package it.unipr.soi23.game_web_server.service;

import it.unipr.soi23.game_web_server.model.*;

public interface Soi23GameWebServerService {

    WatchGameResponse watchGame(String gameId);

    RegisterResponse register(String gameId, String playerId, String token);

    GameDataDTO startGame(String gameId, StartGameRequest request);

    PlayerDTO movePlayer(String gameId, MovePlayerRequest request);

    BallAnimation animationEnd(String gameId);
    GameDataDTO changeTeam(String gameId, ChangeTeamRequest request);

    GameDataDTO changeSettings(String gameId, String playerId, ChangeSettingsRequest request);
}
