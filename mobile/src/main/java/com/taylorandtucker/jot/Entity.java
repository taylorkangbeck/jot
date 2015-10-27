package com.taylorandtucker.jot;

/**
 * Created by Taylor on 9/16/2015.
 */
public class Entity {
    private String name;
    private int importance;
    private long id;
    private double sentiment;

    public Entity(String name, int importance, double sentiment) {
        this.name = name;
        this.importance = importance;
        this.sentiment = sentiment;
    }

    public String getName() {
        return name;
    }

    public void setNameOn(String name) { this.name = name;}

    public int getImportance() {
        return importance;
    }

    public void setImportance(int importance) {
        this.importance = importance;
    }

    public void setId(long id ) { this.id = id; }

    public long getId() {
        return id;
    }

    public double getSentiment() {return sentiment;}

    public void setSentiment(double value) {sentiment = value;}
}
