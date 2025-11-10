package com.cappielloantonio.tempo.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cappielloantonio.tempo.model.Chronology;
import com.cappielloantonio.tempo.repository.ChronologyRepository;
import com.cappielloantonio.tempo.util.Preferences;

import java.util.ArrayList;
import java.util.List;

public class HistoryViewModel extends ViewModel {
    private final ChronologyRepository chronologyRepository;
    private MutableLiveData<List<Chronology>> chronologyLiveData = new MutableLiveData<>();

    public HistoryViewModel() {
        chronologyRepository = new ChronologyRepository();
    }

    public LiveData<List<Chronology>> getChronologyLiveData(int limit) {
        chronologyRepository.getLastPlayed(Preferences.getServerId(), limit).observeForever(chronologyLiveData::postValue);
        return chronologyLiveData;
    }

    public void clearHistory() {
        chronologyRepository.deleteAll(Preferences.getServerId());
        chronologyLiveData.setValue(new ArrayList<>());
    }
}