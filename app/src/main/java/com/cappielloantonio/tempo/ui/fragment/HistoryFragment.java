package com.cappielloantonio.tempo.ui.fragment;

import android.content.ComponentName;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.session.MediaBrowser;
import androidx.media3.session.SessionToken;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cappielloantonio.tempo.R;
import com.cappielloantonio.tempo.constants.HistoryConstants;
import com.cappielloantonio.tempo.databinding.FragmentHistoryBinding;
import com.cappielloantonio.tempo.interfaces.ClickCallback;
import com.cappielloantonio.tempo.model.Chronology;
import com.cappielloantonio.tempo.repository.ChronologyRepository;
import com.cappielloantonio.tempo.service.MediaManager;
import com.cappielloantonio.tempo.subsonic.models.Child;
import com.cappielloantonio.tempo.service.MediaService;
import com.cappielloantonio.tempo.ui.activity.MainActivity;
import com.cappielloantonio.tempo.ui.adapter.PlayerSongQueueAdapter;
import com.cappielloantonio.tempo.util.Constants;
import com.cappielloantonio.tempo.util.Preferences;
import com.cappielloantonio.tempo.viewmodel.HistoryViewModel;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@UnstableApi
public class HistoryFragment extends Fragment implements ClickCallback {
    private static final String TAG = "HistoryFragment";

    private FragmentHistoryBinding bind;
    private MainActivity activity;
    private HistoryViewModel historyViewModel;

    private PlayerSongQueueAdapter historyAdapter;
    private ChronologyRepository chronologyRepository;
    private ListenableFuture<MediaBrowser> mediaBrowserListenableFuture;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        bind = FragmentHistoryBinding.inflate(inflater, container, false);
        activity = (MainActivity) getActivity();

        return bind.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initToolbar();
        initFragment();
        initSongListView();
        initBackStackListener();
    }

    @Override
    public void onStart() {
        super.onStart();
        initializeMediaBrowser();
    }

    @Override
    public void onStop() {
        releaseMediaBrowser();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bind = null;
    }

    private void initToolbar() {
        // 简化toolbar处理，只设置标题
        if (activity != null && activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setTitle(R.string.history_title_section);
        }
    }

    private void initFragment() {
        historyViewModel = new ViewModelProvider(requireActivity()).get(HistoryViewModel.class);
        chronologyRepository = new ChronologyRepository();
        historyViewModel.getChronologyLiveData(100).observe(getViewLifecycleOwner(), chronologies -> {
            bind.loadingProgressBar.setVisibility(View.GONE);

            if (chronologies != null && !chronologies.isEmpty()) {
                bind.emptyHistoryLayout.setVisibility(View.GONE);

                // Convert Chronology to Child for the adapter
                List<com.cappielloantonio.tempo.subsonic.models.Child> childList = new ArrayList<>();
                for (Chronology chronology : chronologies) {
                    childList.add(chronology); // Chronology extends Child
                }
                historyAdapter.setItems(childList);
            } else {
                bind.emptyHistoryLayout.setVisibility(View.VISIBLE);
                if (historyAdapter != null) {
                    historyAdapter.setItems(new ArrayList<>());
                }
            }
        });
    }

    private void initSongListView() {
        historyAdapter = new PlayerSongQueueAdapter(this);
        historyAdapter.setMediaBrowserListenableFuture(mediaBrowserListenableFuture);
        bind.historySongRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        bind.historySongRecyclerView.setAdapter(historyAdapter);

        // 添加左滑删除功能
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // 历史记录不支持拖拽重排
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && historyAdapter != null) {
                    Child trackToRemove = historyAdapter.getItem(position);

                    // 从适配器列表中移除
                    List<Child> currentItems = new ArrayList<>(historyAdapter.getItems());
                    currentItems.remove(position);
                    historyAdapter.setItems(currentItems);

                    // 从数据库中删除对应的历史记录
                    chronologyRepository.deleteBySongId(Preferences.getServerId(), trackToRemove.getId());
                }
            }
        }).attachToRecyclerView(bind.historySongRecyclerView);

        // 设置清空历史按钮
        bind.historyClearButton.setOnClickListener(v -> {
            if (historyViewModel != null) {
                historyViewModel.clearHistory();
            }
        });

        // 设置随机播放按钮
        bind.historyShuffleFab.setOnClickListener(v -> {
            if (historyAdapter != null && historyAdapter.getItems() != null && !historyAdapter.getItems().isEmpty()) {
                List<com.cappielloantonio.tempo.subsonic.models.Child> shuffledList = new ArrayList<>(historyAdapter.getItems());
                Collections.shuffle(shuffledList);
                MediaManager.startQueue(mediaBrowserListenableFuture, shuffledList, 0);
            }
        });

        historyAdapter.registerAdapterDataObserver(new androidx.recyclerview.widget.RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                if (itemCount > 0) {
                    bind.historySongRecyclerView.scrollToPosition(0);
                }
            }
        });
    }

    private void initBackStackListener() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Navigation.findNavController(bind.getRoot()).popBackStack();
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
    }

    private void initializeMediaBrowser() {
        SessionToken sessionToken = new SessionToken(requireContext(), new ComponentName(requireContext(), MediaService.class));
        mediaBrowserListenableFuture = new MediaBrowser.Builder(requireContext(), sessionToken).buildAsync();
    }

    private void releaseMediaBrowser() {
        if (mediaBrowserListenableFuture != null) {
            mediaBrowserListenableFuture.cancel(true);
        }
    }

    @Override
    public void onMediaClick(Bundle bundle) {
        if (bundle != null) {
            // PlayerSongQueueAdapter传递的是整个歌曲列表和点击位置
            ArrayList<com.cappielloantonio.tempo.subsonic.models.Child> tracks = bundle.getParcelableArrayList(Constants.TRACKS_OBJECT);
            int position = bundle.getInt(Constants.ITEM_POSITION);

            MediaManager.startQueue(mediaBrowserListenableFuture, tracks, position);
        }
    }

    @Override
    public void onMediaLongClick(Bundle bundle) {
        if (bundle != null) {
            // PlayerSongQueueAdapter传递的是Child对象而不是Chronology
            com.cappielloantonio.tempo.subsonic.models.Child track = bundle.getParcelable(Constants.TRACK_OBJECT);
            View view = bind.historySongRecyclerView.findViewHolderForAdapterPosition(bundle.getInt(Constants.ITEM_POSITION)).itemView;
            PopupMenu popup = new PopupMenu(requireContext(), view);

            // Create a simple popup menu with available actions
            popup.getMenu().add(0, HistoryConstants.Menu.PLAY_NEXT, 0, HistoryConstants.MenuTitle.PLAY_NEXT);
            popup.getMenu().add(0, HistoryConstants.Menu.ADD_TO_QUEUE, 1, HistoryConstants.MenuTitle.ADD_TO_QUEUE);
            popup.getMenu().add(0, HistoryConstants.Menu.ADD_TO_PLAYLIST, 2, HistoryConstants.MenuTitle.ADD_TO_PLAYLIST);
            popup.getMenu().add(0, HistoryConstants.Menu.DOWNLOAD, 3, HistoryConstants.MenuTitle.DOWNLOAD);
            popup.getMenu().add(0, HistoryConstants.Menu.REMOVE_FROM_HISTORY, 4, HistoryConstants.MenuTitle.REMOVE_FROM_HISTORY);

            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();

                switch (itemId) {
                    case HistoryConstants.Menu.PLAY_NEXT:
                        // TODO: Implement play next functionality
                        return true;
                    case HistoryConstants.Menu.ADD_TO_QUEUE:
                        MediaManager.enqueue(mediaBrowserListenableFuture, Collections.singletonList(track), false);
                        return true;
                    case HistoryConstants.Menu.ADD_TO_PLAYLIST:
                        // TODO: Implement add to playlist functionality
                        return true;
                    case HistoryConstants.Menu.DOWNLOAD:
                        // TODO: Implement download functionality
                        return true;
                    case HistoryConstants.Menu.REMOVE_FROM_HISTORY:
                        // 从历史记录中移除选中的歌曲
                        if (historyAdapter != null && track != null) {
                            // 从适配器的列表中移除该项
                            List<Child> currentItems = new ArrayList<>(historyAdapter.getItems());
                            currentItems.remove(track);
                            historyAdapter.setItems(currentItems);

                            // 从数据库中删除该项
                            chronologyRepository.deleteBySongId(Preferences.getServerId(), track.getId());
                        }
                        return true;
                    default:
                        return false;
                }
            });

            popup.show();
        }
    }
}