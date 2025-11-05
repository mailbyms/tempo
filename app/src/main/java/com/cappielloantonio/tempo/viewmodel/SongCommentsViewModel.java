package com.cappielloantonio.tempo.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cappielloantonio.tempo.subsonic.api.comments.CommentsClient;
import com.cappielloantonio.tempo.subsonic.base.ApiResponse;
import com.cappielloantonio.tempo.subsonic.models.SongComment;
import com.cappielloantonio.tempo.subsonic.models.SongComments;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SongCommentsViewModel extends ViewModel {
    private static final String TAG = "SongCommentsViewModel";

    private final CommentsClient commentsClient;

    private final MutableLiveData<List<SongComment>> comments = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> error = new MutableLiveData<>(false);

    private List<SongComment> allComments = new ArrayList<>();
    private boolean isLoadingMore = false;
    private String lastCommentTimestamp = null;
    private boolean hasMoreComments = true;

    public SongCommentsViewModel() {
        this.commentsClient = new CommentsClient(com.cappielloantonio.tempo.App.getSubsonicClientInstance(false));
    }

    public LiveData<List<SongComment>> getComments() {
        return comments;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<Boolean> getError() {
        return error;
    }

    public void loadComments(String songId, int page, int pageSize, int sortType, String cursor) {
        if (isLoadingMore && page > 1) {
            return;
        }

        loading.setValue(true);
        error.setValue(false);

        if (page == 1) {
            // 第一页，清空现有评论
            allComments.clear();
            isLoadingMore = false;
        } else {
            // 加载更多页面
            isLoadingMore = true;
        }

        commentsClient.getSongComments(songId, page, pageSize, sortType, cursor)
                .enqueue(new Callback<ApiResponse>() {
                    @Override
                    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                        loading.setValue(false);
                        isLoadingMore = false;

                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                // 解析评论数据
                                ApiResponse apiResponse = response.body();
                                if (apiResponse != null &&
                                    apiResponse.subsonicResponse.getSongComments() != null) {

                                    SongComments songComments = apiResponse.subsonicResponse.getSongComments();
                                    List<SongComment> newComments = songComments.getSongComments();

                                    if (newComments != null) {
                                        if (page == 1) {
                                            // 第一页，替换所有评论
                                            allComments = new ArrayList<>(newComments);
                                        } else {
                                            // 追加评论到现有列表
                                            allComments.addAll(newComments);
                                        }

                                        // 更新最后一条评论的时间戳（用于分页）
                                        if (!allComments.isEmpty()) {
                                            SongComment lastComment = allComments.get(allComments.size() - 1);
                                            lastCommentTimestamp = String.valueOf(lastComment.getTimestamp());
                                        }

                                        // 检查是否还有更多评论
                                        hasMoreComments = newComments.size() >= pageSize;

                                        comments.postValue(new ArrayList<>(allComments));
                                    }
                                }
                            } catch (Exception e) {
                                android.util.Log.e(TAG, "Error parsing comments", e);
                                error.setValue(true);
                            }
                        } else {
                            android.util.Log.e(TAG, "API call failed: " + response.message());
                            error.setValue(true);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse> call, Throwable t) {
                        loading.setValue(false);
                        isLoadingMore = false;
                        android.util.Log.e(TAG, "Network error", t);
                        error.setValue(true);
                    }
                });
    }

    public boolean hasNextPage() {
        return hasMoreComments && !isLoadingMore;
    }

    public String getLastCommentTimestamp() {
        return lastCommentTimestamp;
    }

    public boolean isLoading() {
        return Boolean.TRUE.equals(loading.getValue());
    }

    public void clearComments() {
        allComments.clear();
        lastCommentTimestamp = null;
        hasMoreComments = true;
        isLoadingMore = false;
        comments.postValue(new ArrayList<>());
    }
}