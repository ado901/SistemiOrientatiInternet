package it.unipr.soi23.game_web_server.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static it.unipr.soi23.game_web_server.utils.Soi23GameWebServerConst.BALL_LOBBY_ANIMATION;

public class GameData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String id;
    private boolean isPlaying;
    private TeamsScore teamsScore;
    private BallAnimation ballAnimation;
    private Set<Player> players;

    public GameData(String id) {
        this.id = id;
        this.isPlaying = false;
        this.teamsScore = new TeamsScore();
        this.ballAnimation = BALL_LOBBY_ANIMATION;
        this.players = new HashSet<>();
    }

    public String getId() {
        return id;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public TeamsScore getTeamsScore() {
        return teamsScore;
    }

    public void setTeamsScore(TeamsScore teamsScore) {
        this.teamsScore = teamsScore;
    }

    public BallAnimation getBallAnimation() {
        return ballAnimation;
    }

    public void setBallAnimation(BallAnimation ballAnimation) {
        this.ballAnimation = ballAnimation;
    }

    public Set<Player> getPlayers() {
        return players;
    }

    public void setPlayers(Set<Player> players) {
        this.players = players;
    }
}
