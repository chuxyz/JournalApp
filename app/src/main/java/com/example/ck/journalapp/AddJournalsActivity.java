package com.example.ck.journalapp;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Scroller;
import android.widget.Toast;

import com.example.ck.journalapp.database.JournalDatabase;
import com.example.ck.journalapp.database.JournalEntry;
import com.example.ck.journalapp.utilities.JournalExecutors;
import com.example.ck.journalapp.utilities.JournalUpdateViewModel;

import java.util.Date;

public class AddJournalsActivity extends AppCompatActivity {

    public static final String EXTRA_JOURNAL_ID = "extraJournalId";
    public static final int DEFAULT_JOURNAL_ID = -1;
    private int mJournalId = DEFAULT_JOURNAL_ID;

    private JournalDatabase mDb;
    private EditText mJournalText;
    private EditText mJournalTitle;
    private final String TAG = "Chuks";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_journals);
        mJournalTitle = (EditText) findViewById(R.id.journal_title);
        mJournalText = (EditText) findViewById(R.id.journal_text);
        mJournalText.setScroller(new Scroller(getApplicationContext()));
        mJournalText.setVerticalScrollBarEnabled(true);

        mDb = JournalDatabase.getsInstance(getApplicationContext());

        Intent intent = getIntent();
        if(intent != null && intent.hasExtra(EXTRA_JOURNAL_ID)){
            mJournalId = intent.getIntExtra(EXTRA_JOURNAL_ID, DEFAULT_JOURNAL_ID);
            JournalUpdateViewModel.setJournalId(mJournalId);
            final JournalUpdateViewModel journalUpdateViewModel = ViewModelProviders.of(this)
                    .get(JournalUpdateViewModel.class);
            journalUpdateViewModel.getJournalById().observe(this, new Observer<JournalEntry>() {
                @Override
                public void onChanged(@Nullable JournalEntry journalEntry) {
                    journalUpdateViewModel.getJournalById().removeObserver(this);
                    populateForm(journalEntry);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add_journal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int menuItemThatWasSelected = item.getItemId();
        if(menuItemThatWasSelected == R.id.add_journal){
            onSaveClicked();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onSaveClicked(){
        String titleValue = mJournalTitle.getText().toString();
        String textValue = mJournalText.getText().toString();
        Date date = new Date();

        if(textValue != null && !textValue.isEmpty() && !textValue.equals("null") && titleValue != null && !titleValue.isEmpty() && !titleValue.equals("null")){
                final JournalEntry journalEntry = new JournalEntry(titleValue, textValue, date);
            JournalExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    if(mJournalId == DEFAULT_JOURNAL_ID) {
                        mDb.journalDao().insertJournal(journalEntry);
                    }else{
                        journalEntry.setId(mJournalId);
                        mDb.journalDao().updateJournal(journalEntry);
                    }
                    finish();
                }
            });
        }else{
            Context context = AddJournalsActivity.this;
            String message = "Text field is empty!";
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }

    private void populateForm(JournalEntry journalEntry) {
        if (journalEntry == null) {
            return;
        }

        mJournalTitle.setText(journalEntry.getJournalTitle());
        mJournalText.setText(journalEntry.getJournalText());
    }
}
