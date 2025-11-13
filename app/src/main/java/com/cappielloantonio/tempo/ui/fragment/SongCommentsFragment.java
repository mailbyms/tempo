package com.cappielloantonio.tempo.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cappielloantonio.tempo.R;
import com.cappielloantonio.tempo.databinding.FragmentSongCommentsBinding;
import com.cappielloantonio.tempo.ui.adapter.SongCommentAdapter;
import com.cappielloantonio.tempo.viewmodel.SongCommentsViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class SongCommentsFragment extends BottomSheetDialogFragment {
    private static final String TAG = "SongCommentsFragment";

    private static final String ARG_SONG_ID = "song_id";
    private static final String ARG_SONG_TITLE = "song_title";

    private FragmentSongCommentsBinding bind;
    private SongCommentsViewModel songCommentsViewModel;
    private SongCommentAdapter commentAdapter;

    private String songId;
    private String songTitle;
    private int currentSortType = 2; // 默认热度排序
    private int currentPage = 1;
    private static final int PAGE_SIZE = 10;

    public SongCommentsFragment() {
        // Required empty public constructor
    }

    public static SongCommentsFragment newInstance(String songId, String songTitle) {
        SongCommentsFragment fragment = new SongCommentsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SONG_ID, songId);
        args.putString(ARG_SONG_TITLE, songTitle);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.TransparentBottomSheetDialog);

        if (getArguments() != null) {
            songId = getArguments().getString(ARG_SONG_ID);
            songTitle = getArguments().getString(ARG_SONG_TITLE);
        }

        songCommentsViewModel = new ViewModelProvider(this).get(SongCommentsViewModel.class);
        commentAdapter = new SongCommentAdapter(new ArrayList<>());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        bind = FragmentSongCommentsBinding.inflate(inflater, container, false);
        return bind.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupUI();
        setupRecyclerView();
        setupObservers();
        setupClickListeners();

        // 初始加载评论
        loadComments();
    }

    private void setupUI() {
        // 设置默认排序选项
        updateSortUI();
    }

    private void setupRecyclerView() {
        bind.commentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        bind.commentsRecyclerView.setAdapter(commentAdapter);

        // 设置滚动监听用于无限滚动
        bind.commentsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if (!songCommentsViewModel.isLoading() &&
                        (visibleItemCount + firstVisibleItemPosition) >= totalItemCount &&
                        firstVisibleItemPosition >= 0) {
                        // 滚动到底部，加载下一页
                        loadNextPage();
                    }
                }
            }
        });
    }

    private void setupObservers() {
        songCommentsViewModel.getComments().observe(getViewLifecycleOwner(), comments -> {
            if (comments != null) {
                commentAdapter.updateComments(comments);
                bind.loadingProgress.setVisibility(View.GONE);
            }
        });

        songCommentsViewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                bind.loadingProgress.setVisibility(View.VISIBLE);
            } else {
                bind.loadingProgress.setVisibility(View.GONE);
            }
        });
    }

    private void setupClickListeners() {
        bind.buttonBack.setOnClickListener(v -> {
            dismiss();
        });

        bind.sortRecommend.setOnClickListener(v -> {
            if (currentSortType != 1) {
                currentSortType = 1;
                resetAndLoadComments();
            }
        });

        bind.sortHot.setOnClickListener(v -> {
            if (currentSortType != 2) {
                currentSortType = 2;
                resetAndLoadComments();
            }
        });

        bind.sortTime.setOnClickListener(v -> {
            if (currentSortType != 3) {
                currentSortType = 3;
                resetAndLoadComments();
            }
        });
    }

    private void loadComments() {
        if (songId != null) {
            songCommentsViewModel.loadComments(songId, currentPage, PAGE_SIZE, currentSortType, null);
        }
    }

    private void loadNextPage() {
        if (songId != null && songCommentsViewModel.hasNextPage()) {
            currentPage++;
            String cursor = songCommentsViewModel.getLastCommentTimestamp();
            songCommentsViewModel.loadComments(songId, currentPage, PAGE_SIZE, currentSortType, cursor);
        }
    }

    private void resetAndLoadComments() {
        currentPage = 1;
        updateSortUI();
        loadComments();
    }

    private void updateSortUI() {
        // 重置所有排序选项的文字样式为白色，非粗体
        bind.sortRecommend.setTextColor(getResources().getColor(android.R.color.white));
        bind.sortHot.setTextColor(getResources().getColor(android.R.color.white));
        bind.sortTime.setTextColor(getResources().getColor(android.R.color.white));
        bind.sortRecommend.setTypeface(null, android.graphics.Typeface.NORMAL);
        bind.sortHot.setTypeface(null, android.graphics.Typeface.NORMAL);
        bind.sortTime.setTypeface(null, android.graphics.Typeface.NORMAL);

        // 设置当前选中的排序选项文字为绿色且加粗
        switch (currentSortType) {
            case 1:
                bind.sortRecommend.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                bind.sortRecommend.setTypeface(null, android.graphics.Typeface.BOLD);
                break;
            case 2:
                bind.sortHot.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                bind.sortHot.setTypeface(null, android.graphics.Typeface.BOLD);
                break;
            case 3:
                bind.sortTime.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                bind.sortTime.setTypeface(null, android.graphics.Typeface.BOLD);
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bind = null;
    }
}