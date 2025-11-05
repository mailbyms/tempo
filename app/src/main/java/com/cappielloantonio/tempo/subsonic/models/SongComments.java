package com.cappielloantonio.tempo.subsonic.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SongComments {
    @SerializedName("songComment")
    private List<SongComment> songComments;

    @SerializedName("commentCount")
    private int commentCount;

    public SongComments() {
    }

    public List<SongComment> getSongComments() {
        return songComments;
    }

    public void setSongComments(List<SongComment> songComments) {
        this.songComments = songComments;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }
}