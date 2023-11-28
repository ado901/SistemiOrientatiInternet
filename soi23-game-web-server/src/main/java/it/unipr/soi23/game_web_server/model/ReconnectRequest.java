package it.unipr.soi23.game_web_server.model;

public class ReconnectRequest {
    private String playerId;
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
    public ReconnectRequest playerId(String playerId) {
        setPlayerId(playerId);
        return this;
    }
    public ReconnectRequest token(String token) {
        setToken(token);
        return this;
    }
}
