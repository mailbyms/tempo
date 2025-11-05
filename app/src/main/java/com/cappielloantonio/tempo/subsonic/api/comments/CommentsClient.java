package com.cappielloantonio.tempo.subsonic.api.comments;

import android.util.Log;

import com.cappielloantonio.tempo.subsonic.RetrofitClient;
import com.cappielloantonio.tempo.subsonic.Subsonic;
import com.cappielloantonio.tempo.subsonic.base.ApiResponse;

import java.util.Map;

import retrofit2.Call;

public class CommentsClient {
    private static final String TAG = "CommentsClient";

    private final Subsonic subsonic;
    private final CommentsService commentsService;

    public CommentsClient(Subsonic subsonic) {
        this.subsonic = subsonic;
        this.commentsService = new RetrofitClient(subsonic).getRetrofit().create(CommentsService.class);
    }

    public Call<ApiResponse> getSongComments(String songId, int pageNo, int pageSize, int sortType, String cursor) {
        Log.d(TAG, "getSongComments() - songId: " + songId + ", pageNo: " + pageNo + ", pageSize: " + pageSize + ", sortType: " + sortType);

        // 获取基础参数
        Map<String, String> params = new java.util.HashMap<>(subsonic.getParams());

        // 添加评论相关的参数
        params.put("id", songId);
        params.put("type", "0"); // 0表示歌曲
        params.put("pageNo", String.valueOf(pageNo));
        params.put("pageSize", String.valueOf(pageSize));
        params.put("sortType", String.valueOf(sortType));
        if (cursor != null) {
            params.put("cursor", cursor);
        }

        return commentsService.getSongComments(params);
    }
}