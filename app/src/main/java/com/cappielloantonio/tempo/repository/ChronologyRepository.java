package com.cappielloantonio.tempo.repository;

import androidx.lifecycle.LiveData;

import com.cappielloantonio.tempo.database.AppDatabase;
import com.cappielloantonio.tempo.database.dao.ChronologyDao;
import com.cappielloantonio.tempo.model.Chronology;

import java.util.Calendar;
import java.util.List;

public class ChronologyRepository {
    private final ChronologyDao chronologyDao = AppDatabase.getInstance().chronologyDao();

    public LiveData<List<Chronology>> getChronology(String server, long start, long end) {
        return chronologyDao.getAllFrom(start, end, server);
    }

    public LiveData<List<Chronology>> getLastPlayed(String server, int count) {
        return chronologyDao.getLastPlayed(server, count);
    }

    public void insert(Chronology item) {
        InsertThreadSafe insert = new InsertThreadSafe(chronologyDao, item);
        Thread thread = new Thread(insert);
        thread.start();
    }

    public void deleteAll(String server) {
        DeleteAllThreadSafe delete = new DeleteAllThreadSafe(chronologyDao, server);
        Thread thread = new Thread(delete);
        thread.start();
    }

    public void deleteBySongId(String server, String songId) {
        DeleteBySongIdThreadSafe delete = new DeleteBySongIdThreadSafe(chronologyDao, server, songId);
        Thread thread = new Thread(delete);
        thread.start();
    }

    private static class InsertThreadSafe implements Runnable {
        private final ChronologyDao chronologyDao;
        private final Chronology item;

        public InsertThreadSafe(ChronologyDao chronologyDao, Chronology item) {
            this.chronologyDao = chronologyDao;
            this.item = item;
        }

        @Override
        public void run() {
            chronologyDao.insert(item);
        }
    }

    private static class DeleteAllThreadSafe implements Runnable {
        private final ChronologyDao chronologyDao;
        private final String server;

        public DeleteAllThreadSafe(ChronologyDao chronologyDao, String server) {
            this.chronologyDao = chronologyDao;
            this.server = server;
        }

        @Override
        public void run() {
            chronologyDao.deleteAll(server);
        }
    }

    private static class DeleteBySongIdThreadSafe implements Runnable {
        private final ChronologyDao chronologyDao;
        private final String server;
        private final String songId;

        public DeleteBySongIdThreadSafe(ChronologyDao chronologyDao, String server, String songId) {
            this.chronologyDao = chronologyDao;
            this.server = server;
            this.songId = songId;
        }

        @Override
        public void run() {
            chronologyDao.deleteBySongId(server, songId);
        }
    }
}
