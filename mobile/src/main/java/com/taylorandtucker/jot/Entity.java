package com.taylorandtucker.jot;

/**
 * Created by Taylor on 9/16/2015.
 */
public class Entity {
    private String name;
    private double importance;
    private String id;
    private double sentiment;

    public Entity(String name, double importance, double sentiment) {
        this.name = name;
        this.importance = importance;
        this.sentiment = sentiment;
    }

    public String getName() {
        return name;
    }

    public void setNameOn(String name) { this.name = name;}

    public double getImportance() {
        return importance;
    }

    public void setImportance(double importance) {
        this.importance = importance;
    }

    public String getId() {
        return id;
    }

    public double getSentiment() {return sentiment;}

    public void setSentiment(double value) {sentiment = value;}
}
