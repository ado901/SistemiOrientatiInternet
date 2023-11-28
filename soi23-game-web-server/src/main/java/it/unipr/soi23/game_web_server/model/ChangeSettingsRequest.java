package it.unipr.soi23.game_web_server.model;

public class ChangeSettingsRequest {
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getPlayerSpeed() {
        return playerSpeed;
    }

    public void setPlayerSpeed(int playerSpeed) {
        this.playerSpeed = playerSpeed;
    }

    public int getPlayerSize() {
        return playerSize;
    }

    public void setPlayerSize(int playerSize) {
        this.playerSize = playerSize;
    }
    public ChangeSettingsRequest token(String token) {
        this.token = token;
        return this;
    }
    public ChangeSettingsRequest playerSpeed(int playerSpeed) {
        this.playerSpeed = playerSpeed;
        return this;
    }
    public ChangeSettingsRequest playerSize(int playerSize) {
        this.playerSize = playerSize;
        return this;
    }

    private String token;
    private int playerSpeed;
    private int playerSize;

}
