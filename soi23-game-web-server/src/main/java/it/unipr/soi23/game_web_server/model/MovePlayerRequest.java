package it.unipr.soi23.game_web_server.model;

public class MovePlayerRequest {

    private String playerId;
    private int y;

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
