package com.example.fake_twitter_android;


import java.sql.Timestamp;

import java.util.Date;

public class TweetCard {
    private String id;
    private String username;
    private String tweet;
    private int pfp;

    Date currentTime;


    public TweetCard(String username, String tweet, int pfp, Date date) {
        this.username = username;
        this.tweet = tweet;
        this.pfp = pfp;
        this.currentTime = date;

    }

    public TweetCard() {

    }

    public String getUsername() {
        return username;
    }

    public String getTweet() {
        return tweet;
    }

    public int getPfp() {
        return pfp;
    }

    public Date getCurrentTime() {
        return currentTime;
    }


    public void setId(String id) {
        this.id = id;
    }
    public String _getId() {
        return id;
    }
}