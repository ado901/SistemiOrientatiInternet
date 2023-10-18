package it.unipr.soi23.game_web_server.model;

import java.util.ArrayList;
import java.util.List;

public class WatchGameResponse extends GameDataDTO {

    private List<PlayerDTO> players;

    public WatchGameResponse() {
        this.players = new ArrayList<>();
    }

    @Override
    public WatchGameResponse teamsScore(TeamsScore teamsScore) {
        super.teamsScore(teamsScore);
        return this;
    }

    @Override
    public WatchGameResponse ballAnimation(BallAnimation ballAnimation) {
        super.ballAnimation(ballAnimation);
        return this;
    }

    public List<PlayerDTO> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerDTO> players) {
        this.players = players;
    }

    public void addPlayer(Player player) {
        final PlayerDTO playerDTO = new PlayerDTO().fromPlayer(player);
        this.players.add(playerDTO);
    }
}
