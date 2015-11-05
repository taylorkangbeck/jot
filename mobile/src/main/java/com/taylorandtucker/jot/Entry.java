package com.taylorandtucker.jot;

import org.joda.time.Days;
import org.joda.time.MutableDateTime;

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
        this.body = removeFakeSentTags(this.body);
    }

    private String removeFakeSentTags(String body){
        return body.replaceAll("\\[\\[-?\\d\\.?\\d?\\]\\]", "");
    }
    public Date getCreatedOn() {
        return createdOn;
    }

    public int createdDaysAfterEpoch(){
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0); //Set to Epoch time
        MutableDateTime now = new MutableDateTime();
        now.setDate(getCreatedOn().getTime());

        return Days.daysBetween(epoch, now).getDays();
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
