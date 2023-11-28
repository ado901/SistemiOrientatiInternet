package it.unipr.soi23.game_web_server.model;

import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serial;
import java.io.Serializable;

import static it.unipr.soi23.game_web_server.utils.Soi23GameWebServerConst.*;
import static it.unipr.soi23.game_web_server.utils.Soi23GameWebServerConst.PLAYFIELD_HEIGHT;

@RedisHash("Player")
public class Player implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public enum Team {
        LEFT, //
        RIGHT;

        @JsonValue
        public int toValue() {
            return ordinal();
        }
    }
    public Player() {
        this.playerHeight = PLAYER_HEIGHT;
        this.playerSpeed = PLAYER_SPEED;
        setPlayerheight(this.playerHeight);
        setPlayerSpeed(this.playerSpeed);
        this.playerwidth = PLAYER_WIDTH;
        this.playerradius=Math.min(this.playerwidth, playerHeight) / 2;
        this.playerstep = (int) (playerSpeed / FPS);
        this.playerminy = playerHeight / 2;
        this.playermaxy = PLAYFIELD_HEIGHT - playerHeight / 2;
        this.playerleftx = PLAYFIELD_WIDTH / 20;
        this.playerrightx = PLAYFIELD_WIDTH - this.playerleftx;
        this.minx = this.playerleftx + this.playerradius;
        this.maxx = this.playerrightx - this.playerradius;
        this.miny = this.playerradius;
        this.maxy = PLAYFIELD_HEIGHT - this.playerradius;


    }

    public Player(int playerHeight, double playerSpeed) {
        this.playerHeight = playerHeight;
        this.playerSpeed = playerSpeed;
        setPlayerheight(this.playerHeight);
        setPlayerSpeed(this.playerSpeed);
        this.playerwidth = PLAYER_WIDTH;
        this.playerradius=Math.min(this.playerwidth, playerHeight) / 2;
        this.playerstep = (int) (playerSpeed / FPS);
        this.playerminy = playerHeight / 2;
        this.playermaxy = PLAYFIELD_HEIGHT - playerHeight / 2;
        this.playerleftx = PLAYFIELD_WIDTH / 20;
        this.playerrightx = PLAYFIELD_WIDTH - this.playerleftx;
        this.minx = this.playerleftx + this.playerradius;
        this.maxx = this.playerrightx - this.playerradius;
        this.miny = this.playerradius;
        this.maxy = PLAYFIELD_HEIGHT - this.playerradius;

    }
    @Id
    private String id;
    @Indexed
    private String gameId;

    private int playerwidth;
    private int playerradius;
    private int playerstep;
    private int playerminy;
    private int playermaxy ;
    private int playerleftx;
    private int playerrightx;
    private int minx;
    private int maxx;
    private int miny;
    private int maxy;


    public void setPlayerheight(int playerheight) {
        this.playerHeight = playerheight;
        this.playerradius=Math.min(this.playerwidth, playerheight) / 2;
        this.playerminy = playerheight / 2;
        this.playermaxy = PLAYFIELD_HEIGHT - playerheight / 2;
        this.minx = this.playerleftx + this.playerradius;
        this.maxx = this.playerrightx - this.playerradius;
    }
    public Player playerheight(int playerheight) {
        this.playerHeight = playerheight;
        this.playerradius=Math.min(this.playerwidth, playerheight) / 2;
        this.playerminy = playerheight / 2;
        this.playermaxy = PLAYFIELD_HEIGHT - playerheight / 2;
        this.minx = this.playerleftx + this.playerradius;
        this.maxx = this.playerrightx - this.playerradius;
        return this;
    }
    public Player playerSpeed(int playerSpeed) {
        this.playerSpeed = playerSpeed;
        this.playerstep = (playerSpeed / 60);
        return this;
    }
    public int getPlayerwidth() {
        return playerwidth;
    }

    public void setPlayerwidth(int playerwidth) {
        this.playerwidth = playerwidth;
    }

    public int getPlayerradius() {
        return playerradius;
    }

    public void setPlayerradius(int playerradius) {
        this.playerradius = playerradius;
    }

    public int getPlayerstep() {
        return playerstep;
    }

    public void setPlayerstep(int playerstep) {
        this.playerstep = playerstep;
    }

    public int getPlayerminy() {
        return playerminy;
    }

    public void setPlayerminy(int playerminy) {
        this.playerminy = playerminy;
    }

    public int getPlayermaxy() {
        return playermaxy;
    }

    public void setPlayermaxy(int playermaxy) {
        this.playermaxy = playermaxy;
    }

    public int getPlayerleftx() {
        return playerleftx;
    }

    public void setPlayerleftx(int playerleftx) {
        this.playerleftx = playerleftx;
    }

    public int getPlayerrightx() {
        return playerrightx;
    }

    public void setPlayerrightx(int playerrightx) {
        this.playerrightx = playerrightx;
    }

    public int getMinx() {
        return minx;
    }

    public void setMinx(int minx) {
        this.minx = minx;
    }

    public int getMaxx() {
        return maxx;
    }

    public void setMaxx(int maxx) {
        this.maxx = maxx;
    }

    public int getMiny() {
        return miny;
    }

    public void setMiny(int miny) {
        this.miny = miny;
    }

    public int getMaxy() {
        return maxy;
    }

    public void setMaxy(int maxy) {
        this.maxy = maxy;
    }

    private String token;
    private Team team;
    private int y;
    private boolean readyToStart;
    private int playerHeight;
    private double playerSpeed;
    private long lastMovementTimestamp;

    public int getPlayerHeight() {
        return playerHeight;
    }

    public double getPlayerSpeed() {
        return playerSpeed;
    }

    public void setPlayerSpeed(double playerSpeed) {
        this.playerSpeed = playerSpeed;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Player id(String id) {
        setId(id);
        return this;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public Player gameId(String gameId) {
        setGameId(gameId);
        return this;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Player token(String token) {
        setToken(token);
        return this;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public Player team(Team team) {
        setTeam(team);
        return this;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Player y(int y) {
        setY(y);
        return this;
    }

    public boolean isReadyToStart() {
        return readyToStart;
    }

    public void setReadyToStart(boolean readyToStart) {
        this.readyToStart = readyToStart;
    }

    public Player readyToStart(boolean readyToStart) {
        setReadyToStart(readyToStart);
        return this;
    }

    public long getLastMovementTimestamp() {
        return lastMovementTimestamp;
    }

    public void setLastMovementTimestamp(long lastMovementTimestamp) {
        this.lastMovementTimestamp = lastMovementTimestamp;
    }

    public Player lastMovementTimestamp(long lastMovementTimestamp) {
        setLastMovementTimestamp(lastMovementTimestamp);
        return this;
    }
}
