package it.unipr.soi23.game_web_server.model;

public class ChangeTeamRequest {
    private String playerId;
    private String token;
    private int team;

    public String getPlayerId() {
        return playerId;
    }
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public int getTeam() {
        return team;
    }
    public void setTeam(int team) {
        this.team = team;
    }
}
