package it.unipr.soi23.game_web_server.rest;

import it.unipr.soi23.game_web_server.model.*;
import it.unipr.soi23.game_web_server.service.Soi23GameWebServerService;
import it.unipr.soi23.game_web_server.utils.Soi23GameWebServerConst;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

@Controller
public class Soi23GameWebServerController {

    private final Soi23GameWebServerService service;

    public Soi23GameWebServerController(Soi23GameWebServerService service) {
        this.service = service;
    }

    @SubscribeMapping("game.{gameId}")
    public WatchGameResponse watchGame(@DestinationVariable String gameId) {
        return service.watchGame(gameId);
    }

    @SubscribeMapping("game.{gameId}.player.{playerId}.token.{token}")
    public RegisterResponse register(@DestinationVariable String gameId, @DestinationVariable String playerId, @DestinationVariable String token) {
        return service.register(gameId, playerId, token);
    }

    @MessageMapping("game.{gameId}.start")
    @SendTo(Soi23GameWebServerConst.TOPIC_GAME_PREFIX + "{gameId}")
    public GameDataDTO startGame(@DestinationVariable String gameId, StartGameRequest request) {
        return service.startGame(gameId, request);
    }

    @MessageMapping("game.{gameId}.position")
    @SendTo(Soi23GameWebServerConst.TOPIC_GAME_PREFIX + "{gameId}.players")
    public PlayerDTO movePlayer(@DestinationVariable String gameId, MovePlayerRequest request) {
        return service.movePlayer(gameId, request);
    }

    @MessageMapping("game.{gameId}.animation")
    @SendTo(Soi23GameWebServerConst.TOPIC_GAME_PREFIX + "{gameId}.ball")
    public BallAnimation animationEnd(@DestinationVariable String gameId) {
        return service.animationEnd(gameId);
    }

    @MessageMapping("game.{gameId}.team")
    @SendTo(Soi23GameWebServerConst.TOPIC_GAME_PREFIX + "{gameId}.team")
    public GameDataDTO changeTeam(@DestinationVariable String gameId, ChangeTeamRequest request) {
        return service.changeTeam(gameId, request);
    }

    @MessageMapping("game.{gameId}.player.{playerId}.settings")
    @SendTo(Soi23GameWebServerConst.TOPIC_GAME_PREFIX + "{gameId}.player.{playerId}.settings")
    public GameDataDTO changeSettings(@DestinationVariable String gameId, @DestinationVariable String playerId, ChangeSettingsRequest request) {
        return service.changeSettings(gameId, playerId, request);
    }

}
