package it.unipr.soi23.game_web_server.model;

import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serial;
import java.io.Serializable;

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

    private String id;
    private Team team;
    private int y;
    private boolean readyToStart;

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
}
