package com.cappielloantonio.tempo.subsonic.api.comments;

import com.cappielloantonio.tempo.subsonic.base.ApiResponse;

import java.util.Map;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

public interface CommentsService {
    @GET("/rest/getSongComments")
    Call<ApiResponse> getSongComments(@QueryMap Map<String, String> params);
}