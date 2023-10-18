package it.unipr.soi23.game_web_server.model;

public class RegisterResponse extends WatchGameResponse {

    @Override
    public RegisterResponse teamsScore(TeamsScore teamsScore) {
        super.teamsScore(teamsScore);
        return this;
    }

    @Override
    public RegisterResponse ballAnimation(BallAnimation ballAnimation) {
        super.ballAnimation(ballAnimation);
        return this;
    }
}
