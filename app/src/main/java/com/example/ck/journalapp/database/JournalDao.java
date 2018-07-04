package com.example.ck.journalapp.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface JournalDao {
    @Query("SELECT * FROM journals ORDER BY created_on DESC")
    LiveData<List<JournalEntry>> loadAllJournal();

    @Query("SELECT * FROM journals WHERE id = :id")
    LiveData<JournalEntry> loadJournalById(int id);

    @Insert
    void insertJournal(JournalEntry journalEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateJournal(JournalEntry journalEntry);

    @Delete
    void deleteJournal(JournalEntry journalEntry);
}
