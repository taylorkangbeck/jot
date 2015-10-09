package com.taylorandtucker.jot;

import java.util.Date;

/**
 * Created by Taylor on 9/16/2015.
 */
public class Entry {
    private Date createdOn;
    private String body;
    private String id;
    private double sentiment;

    public Entry(String body) {
        this(new Date(), body);
    }

    public Entry(Date createdOn, String body) {
        this.createdOn = createdOn;
        this.body = body;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getId() {
        return id;
    }

    public double getSentiment() {return sentiment;}

    public void setSentiment(double value) {sentiment = value;}
}
