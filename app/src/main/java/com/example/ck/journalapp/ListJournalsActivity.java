package com.example.ck.journalapp;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ck.journalapp.database.JournalDatabase;
import com.example.ck.journalapp.database.JournalEntry;
import com.example.ck.journalapp.utilities.JournalAdapter;
import com.example.ck.journalapp.utilities.JournalExecutors;
import com.example.ck.journalapp.utilities.JournalViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import java.util.List;


public class ListJournalsActivity extends AppCompatActivity implements View.OnClickListener, JournalAdapter.ItemClickListener {

    private JournalDatabase mDb;
    private DrawerLayout mDrawerLayout;
    private FrameLayout mRecyclerViewWrapper;
    private LinearLayout mGoogleSigninWrapper;
    private RecyclerView mRecyclerView;
    private JournalAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private JournalViewModel mJournalViewModel;
    private FloatingActionButton mFab;

    private static int RC_SIGN_IN = 100;
    //private String TAG = "Report";
    private FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;

    private boolean mIsLoggedOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_journals);

        mIsLoggedOut = true;

        mRecyclerViewWrapper = (FrameLayout) findViewById(R.id.rv_wrapper);
        mGoogleSigninWrapper = (LinearLayout) findViewById(R.id.signIn_wrapper);

        SignInButton signInButton = findViewById(R.id.google_sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setColorScheme(SignInButton.COLOR_DARK);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()){
                    case R.id.google_sign_in_button:
                        signIn();
                        break;
                }
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();
        //Log.d(TAG, "Getting DB Instance");
        mDb = JournalDatabase.getsInstance(getApplicationContext());
        mRecyclerView = findViewById(R.id.rv_journal);
        mRecyclerView.setHasFixedSize(true);

        //DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), VERTICAL);
        //mRecyclerView.addItemDecoration(decoration);

        //mRecyclerView.addItemDecoration(new JournalDividerItemDecoration(this, LinearLayoutManager.VERTICAL, 16));

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new JournalAdapter(this, this);
        mRecyclerView.setAdapter(mAdapter);

        mJournalViewModel = ViewModelProviders.of(this).get(JournalViewModel.class);
        mJournalViewModel.getJournalEntryList().observe(this, new Observer<List<JournalEntry>>() {
            @Override
            public void onChanged(@Nullable List<JournalEntry> journalEntries) {
                //Log.d(TAG, "Database Changed");
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

        mFab = (FloatingActionButton) findViewById(R.id.fb_add_journal);
        mFab.setOnClickListener(this);

        invalidateOptionsMenu();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        MenuItem userAccount = menu.findItem(R.id.user);
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if(acct != null) userAccount.setTitle(acct.getEmail());
        return true;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        if(mIsLoggedOut){
            MenuItem item1 = menu.findItem(R.id.user);
            item1.setVisible(false);
            MenuItem item2 = menu.findItem(R.id.logout);
            item2.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int menuItemThatWasSelected = item.getItemId();
        if(menuItemThatWasSelected == R.id.logout){
            FirebaseAuth.getInstance().signOut();
            signOut();
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

    private void signIn(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
                //Log.w(TAG, "Google sign was successful");
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                //Log.w(TAG, "Google sign in failed", e);
                // ...
            }

        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        //Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "signInWithCredential:failure", task.getException());
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private  void updateUI(FirebaseUser user){
        if(user != null) {
            mIsLoggedOut = false;
            mGoogleSigninWrapper.setVisibility(View.GONE);
            mRecyclerViewWrapper.setVisibility(View.VISIBLE);
        }else{
            mIsLoggedOut = true;
            mRecyclerViewWrapper.setVisibility(View.GONE);
            mGoogleSigninWrapper.setVisibility(View.VISIBLE);
        }
        invalidateOptionsMenu();
    }

    private void signOut(){
        revokeAccess();
    }

    private void revokeAccess() {
        mGoogleSignInClient.revokeAccess()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    }
                });
    }

}
