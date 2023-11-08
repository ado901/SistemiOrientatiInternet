package it.unipr.soi23.game_web_server.broker;

import it.unipr.soi23.game_web_server.model.Player;

import static it.unipr.soi23.game_web_server.utils.Soi23GameWebServerConst.*;

public class PlayerBroker {

    private final long nowTimestamp;

    private Player player;

    public PlayerBroker() {
        this.nowTimestamp = System.currentTimeMillis();
    }

    public PlayerBroker player(Player player) {
        this.player = player;
        return this;
    }

    /**
     * Compares the provided token with the originally saved one
     *
     * @param  token Token to compare
     * @return       true if the token is correct, false otherwise
     */
    public boolean checkPlayerToken(String token) {
        /* TODO
        Return true if the token is correct (and not null), false otherwise
         */
        return token!=null && token.equals(player.getToken());
    }

    /**
     * Moves the player applying validation, blocking cheaters. Allows the player to
     * move only for at most his defined step every frame.
     *
     * @param y The new position along the Y axis
     */
    public void moveToY(int y) {
        /* TODO
        Set the new y and lastMovementTimestamp of the player only if:
        - the new value is between PLAYER_MIN_Y and PLAYER_MAX_Y
        - the distance between old and new y values is less or equal to elapsedFrames * PLAYER_STEP
         */
        double elapsedFrames= (nowTimestamp-player.getLastMovementTimestamp())/MS_PER_FRAME;
        if(y>=PLAYER_MIN_Y && y<=PLAYER_MAX_Y && Math.abs(y-player.getY())<=PLAYER_STEP*elapsedFrames) {
            player.setY(y);
            player.setLastMovementTimestamp(nowTimestamp);
        }
    }
}
