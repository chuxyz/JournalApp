package com.example.ck.journalapp.utilities;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.ck.journalapp.database.JournalDatabase;
import com.example.ck.journalapp.database.JournalEntry;

import java.util.List;

public class JournalViewModel extends AndroidViewModel {
    private final String TAG = "Chuks";
    JournalDatabase journalDatabase;
    private final LiveData<List<JournalEntry>> journalEntryList;

    public JournalViewModel(@NonNull Application application) {
        super(application);
        journalDatabase = JournalDatabase.getsInstance(this.getApplication());
        Log.d(TAG, "Actively retrieving the tasks from the DataBase");
        journalEntryList = journalDatabase.journalDao().loadAllJournal();
    }

    public LiveData<List<JournalEntry>> getJournalEntryList() {
        return journalEntryList;
    }
}
