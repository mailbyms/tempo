package com.cappielloantonio.tempo.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.Observer;

import com.cappielloantonio.tempo.model.Chronology;
import com.cappielloantonio.tempo.repository.ChronologyRepository;
import com.cappielloantonio.tempo.util.Preferences;

import java.util.ArrayList;
import java.util.List;

public class HistoryViewModel extends ViewModel {
    private final ChronologyRepository chronologyRepository;
    private MutableLiveData<List<Chronology>> chronologyLiveData = new MutableLiveData<>();
    private LiveData<List<Chronology>> dbLiveData;
    private Observer<List<Chronology>> dbObserver;

    public HistoryViewModel() {
        chronologyRepository = new ChronologyRepository();
    }

    public LiveData<List<Chronology>> getChronologyLiveData(int limit) {
        // 如果已经有现有的观察者，先移除它
        if (dbLiveData != null && dbObserver != null) {
            dbLiveData.removeObserver(dbObserver);
        }

        // 创建新的观察者
        dbObserver = chronologies -> {
            // 数据库变化时，更新我们的 LiveData
            chronologyLiveData.postValue(chronologies);
        };

        // 观察数据库的 LiveData
        dbLiveData = chronologyRepository.getLastPlayed(Preferences.getServerId(), limit);
        dbLiveData.observeForever(dbObserver);

        return chronologyLiveData;
    }

    public void clearHistory() {
        chronologyRepository.deleteAll(Preferences.getServerId());
        // 立即清空 LiveData，确保 UI 立即更新
        chronologyLiveData.postValue(new ArrayList<>());
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // 清理观察者，避免内存泄漏
        if (dbLiveData != null && dbObserver != null) {
            dbLiveData.removeObserver(dbObserver);
        }
    }
}