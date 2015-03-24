package com.example.padim;

public class ViewPagerEvent {
    private int position;
    private String action;
    private String name;

    public ViewPagerEvent(String action, String name, int position) {
        this.action = action;
        this.name = name;
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
