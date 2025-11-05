package com.cappielloantonio.tempo.subsonic.models;

import com.google.gson.annotations.SerializedName;

public class SongComment {
    @SerializedName("id")
    private String id;

    @SerializedName("user")
    private String user;

    @SerializedName("avatarUrl")
    private String avatarUrl;

    @SerializedName("content")
    private String content;

    @SerializedName("timestamp")
    private long timestamp;

    @SerializedName("likedCount")
    private int likedCount;

    public SongComment() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getLikedCount() {
        return likedCount;
    }

    public void setLikedCount(int likedCount) {
        this.likedCount = likedCount;
    }
}