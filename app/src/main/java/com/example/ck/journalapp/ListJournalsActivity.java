package com.example.ck.journalapp;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ck.journalapp.database.JournalDatabase;
import com.example.ck.journalapp.database.JournalEntry;
import com.example.ck.journalapp.utilities.JournalAdapter;
import com.example.ck.journalapp.utilities.JournalDividerItemDecoration;
import com.example.ck.journalapp.utilities.JournalExecutors;
import com.example.ck.journalapp.utilities.JournalViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.util.ArrayList;
import java.util.List;

import static android.support.v7.widget.DividerItemDecoration.VERTICAL;


public class ListJournalsActivity extends AppCompatActivity implements View.OnClickListener, JournalAdapter.ItemClickListener {

    private JournalDatabase mDb;
    private RecyclerView mRecyclerView;
    private JournalAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private JournalViewModel journalViewModel;
    private FloatingActionButton fab;

    private final String TAG = "chuks";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_journals);
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        //TextView mDisplayEmail = (TextView) findViewById(R.id.display_email);
        //mDisplayEmail.setText(acct.getEmail());
        Log.d(TAG, "Getting DB Instance");
        mDb = JournalDatabase.getsInstance(getApplicationContext());
        mRecyclerView = findViewById(R.id.rv_journal);
        mRecyclerView.setHasFixedSize(true);

        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), VERTICAL);
        mRecyclerView.addItemDecoration(decoration);

        mRecyclerView.addItemDecoration(new JournalDividerItemDecoration(this, LinearLayoutManager.VERTICAL, 16));

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        //mAdapter = new JournalAdapter(this, new ArrayList<JournalEntry>());
        mAdapter = new JournalAdapter(this, this);
        mRecyclerView.setAdapter(mAdapter);

        journalViewModel = ViewModelProviders.of(this).get(JournalViewModel.class);
        journalViewModel.getJournalEntryList().observe(this, new Observer<List<JournalEntry>>() {
            @Override
            public void onChanged(@Nullable List<JournalEntry> journalEntries) {
                Log.d(TAG, "Database Changed");
                mAdapter.setJournalEntries(journalEntries);
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                // Here is where you'll implement swipe to delete
                JournalExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        int position = viewHolder.getAdapterPosition();
                        List<JournalEntry> journalEntries = mAdapter.getJournalEntries();
                        mDb.journalDao().deleteJournal(journalEntries.get(position));
                    }
                });
            }
        }).attachToRecyclerView(mRecyclerView);

        fab = (FloatingActionButton) findViewById(R.id.fb_add_journal);
        fab.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int menuItemThatWasSelected = item.getItemId();
        if(menuItemThatWasSelected == R.id.create_journal){
            Context context = ListJournalsActivity.this;
            Intent intent = new Intent(context, AddJournalsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fb_add_journal:{
                Context context = ListJournalsActivity.this;
                Intent intent = new Intent(context, AddJournalsActivity.class);
                startActivity(intent);
            } break;
            default: break;
        }
    }

    @Override
    public void onItemClickListener(int itemId) {
        Intent intent = new Intent(ListJournalsActivity.this, AddJournalsActivity.class);
        intent.putExtra(AddJournalsActivity.EXTRA_JOURNAL_ID, itemId);
        startActivity(intent);
    }
}
