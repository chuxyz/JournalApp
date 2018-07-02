package com.example.ck.journalapp.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "journals")
public class JournalEntry {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String journalTitle;
    private String journalText;
    @ColumnInfo(name = "created_on")
    private Date createdOn;

    @Ignore
    public JournalEntry(String journalTitle, String journalText, Date createdOn){
        this.journalTitle = journalTitle;
        this.journalText = journalText;
        this.createdOn = createdOn;
    }

    public  JournalEntry(int id, String journalTitle, String journalText, Date createdOn){
        this.id = id;
        this.journalTitle = journalTitle;
        this.journalText = journalText;
        this.createdOn = createdOn;
    }

    public void setId(int id){
        this.id = id;
    }

    public void setJournalTitle(String journalTitle){ this.journalTitle = journalTitle; }

    public void setJournalText(String journalText){
        this.journalText = journalText;
    }

    public void setCreatedOn(Date createdOn){
        this.createdOn = createdOn;
    }


    public int getId(){
        return this.id;
    }

    public String getJournalTitle(){ return this.journalTitle; }

    public String getJournalText() {
        return this.journalText;
    }

    public Date getCreatedOn() {
        return this.createdOn;
    }
}
