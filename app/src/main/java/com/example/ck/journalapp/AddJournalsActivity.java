package com.example.ck.journalapp;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ck.journalapp.database.JournalDatabase;
import com.example.ck.journalapp.database.JournalEntry;
import com.example.ck.journalapp.utilities.JournalExecutors;
import com.example.ck.journalapp.utilities.JournalUpdateViewModel;
import com.example.ck.journalapp.utilities.LinedEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddJournalsActivity extends AppCompatActivity {

    public static final String EXTRA_JOURNAL_ID = "extraJournalId";
    public static final int DEFAULT_JOURNAL_ID = -1;
    private int mJournalId = DEFAULT_JOURNAL_ID;

    private JournalDatabase mDb;
    private LinearLayout mTimeWrapper;
    private TextView mTimeTextView;
    private LinedEditText mJournalText;
    private EditText mJournalTitle;
    private FloatingActionButton mFabCreate;
    private boolean mShowMenu;

    private SimpleDateFormat mDateFormat = new SimpleDateFormat("dd/MM/yy 'at'  hh:mm a", Locale.getDefault());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_journals);
        mShowMenu = true;
        mTimeWrapper = (LinearLayout) findViewById(R.id.time_wrapper);
        mTimeTextView = (TextView) findViewById(R.id.time_display);
        mJournalTitle = (EditText) findViewById(R.id.journal_title);
        mJournalText = (LinedEditText) findViewById(R.id.journal_text);
        mFabCreate = (FloatingActionButton) findViewById(R.id.fb_create_journal);
        mJournalText.setScroller(new Scroller(getApplicationContext()));
        mJournalText.setVerticalScrollBarEnabled(true);


        mDb = JournalDatabase.getsInstance(getApplicationContext());

        Intent intent = getIntent();
        if(intent != null && intent.hasExtra(EXTRA_JOURNAL_ID)){
            mShowMenu = false;
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

        mFabCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mJournalTitle.setEnabled(true);
                mJournalText.setEnabled(true);
                mTimeWrapper.setVisibility(View.GONE);
                mFabCreate.setVisibility(View.GONE);
                invalidateOptionsMenu();
                mShowMenu = true;
            }
        });

        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add_journal, menu);
        if(!mShowMenu) {
            MenuItem saveItem = menu.findItem(R.id.add_journal);
            saveItem.setVisible(false);
        }
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

        mJournalTitle.setEnabled(false);
        mJournalText.setEnabled(false);
        mTimeWrapper.setVisibility(View.VISIBLE);
        mFabCreate.setVisibility(View.VISIBLE);
        mTimeTextView.setText(mDateFormat.format(journalEntry.getCreatedOn()));
        mJournalTitle.setText(journalEntry.getJournalTitle());
        mJournalText.setText(journalEntry.getJournalText());
    }
}
