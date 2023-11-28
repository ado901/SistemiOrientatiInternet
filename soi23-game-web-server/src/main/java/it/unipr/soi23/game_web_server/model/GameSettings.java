package it.unipr.soi23.game_web_server.model;

public class GameSettings {
    private int rightHeight;
    private int leftHeight;
    private int defaultHeight=100;

    private int rightCount;
    private int leftCount;
    public GameSettings() {
        this.rightHeight = defaultHeight;
        this.leftHeight = defaultHeight;
        this.rightCount = 0;
        this.leftCount = 0;
    }
    public GameSettings(int rightHeight, int leftHeight) {
        this.rightHeight = rightHeight;
        this.leftHeight = leftHeight;
        this.rightCount = 0;
        this.leftCount = 0;
    }
    public int getRightHeight() {
        return rightHeight;
    }
    public void setRightHeight(int rightHeight) {
        this.rightHeight = rightHeight;
    }
    public int getLeftHeight() {
        return leftHeight;
    }
    public void setLeftHeight(int leftHeight) {
        this.leftHeight = leftHeight;
    }

    public int getRightCount() {
        return rightCount;
    }
    public void setRightCount(int rightCount) {
        this.rightCount = rightCount;
    }
    public int getLeftCount() {
        return leftCount;
    }
    public void setLeftCount(int leftCount) {
        this.leftCount = leftCount;
    }
    public GameSettings rightCount(int rightCount) {
        this.rightCount = rightCount;
        return this;
    }
    public GameSettings leftCount(int leftCount) {
        this.leftCount = leftCount;
        return this;
    }
    public GameSettings rightHeight(int rightHeight) {
        this.rightHeight = rightHeight;
        return this;
    }

    public GameSettings leftHeight(int leftHeight) {
        this.leftHeight = leftHeight;
        return this;
    }
}
