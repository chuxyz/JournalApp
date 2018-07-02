package com.example.ck.journalapp.utilities;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.ck.journalapp.database.JournalDatabase;
import com.example.ck.journalapp.database.JournalEntry;

import java.util.List;

public class JournalUpdateViewModel extends AndroidViewModel {
    private final String TAG = "Chuks";
    private static int mJournalId;
    JournalDatabase journalDatabase;
    private final LiveData<JournalEntry> journalEntryById;

    public JournalUpdateViewModel(@NonNull Application application) {
        super(application);
        journalDatabase = JournalDatabase.getsInstance(this.getApplication());
        Log.d(TAG, "Actively retrieving the tasks from the DataBase");
        journalEntryById = journalDatabase.journalDao().loadJournalById(getJournalId());
    }

    public int getJournalId() {
        return mJournalId;
    }

    public static void setJournalId(int journalId){
        mJournalId = journalId;
    }
    public LiveData<JournalEntry> getJournalById() {
        return journalEntryById;
    }
}