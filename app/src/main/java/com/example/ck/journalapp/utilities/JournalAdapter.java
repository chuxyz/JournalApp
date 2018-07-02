package com.example.ck.journalapp.utilities;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.ck.journalapp.R;
import com.example.ck.journalapp.database.JournalDatabase;
import com.example.ck.journalapp.database.JournalEntry;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import static android.provider.Settings.System.DATE_FORMAT;


public class JournalAdapter extends RecyclerView.Adapter<JournalAdapter.ViewHolder>{
    private List<JournalEntry> journalEntries;
    private JournalDatabase mDb;
    private Context mContext;
    final private ItemClickListener mItemClickListener;
    private final String TAG = "Chuks";

   // private SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());

    //public JournalAdapter(Context context, List<JournalEntry> journalEntries){
    public JournalAdapter(Context context, ItemClickListener listener){
        mContext = context;
        mItemClickListener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        public TextView mTextView;
        public TextView mTitleView;
        public TextView mTimeView;

        public ViewHolder(View view) {
            super(view);
            mTitleView = view.findViewById(R.id.rv_journal_title);
            mTextView = view.findViewById(R.id.rv_journal_text);
            mTimeView = view.findViewById(R.id.rv_joournal_createdOn);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int viewId = journalEntries.get(getAdapterPosition()).getId();
            mItemClickListener.onItemClickListener(viewId);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext)
                .inflate(R.layout.rv_journal_list, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        JournalEntry journalEntry = journalEntries.get(position);
        holder.mTitleView.setText(journalEntry.getJournalTitle());
        holder.mTextView.setText(journalEntry.getJournalText());
        //String createdOn = dateFormat.format(journalEntry.getCreatedOn());
        //holder.mTimeView.setText(createdOn);
    }

    @Override
    public int getItemCount() {
        if(journalEntries != null){
            return journalEntries.size();
        }
        return 0;
    }

    public void setJournalEntries(List<JournalEntry> journalEntries) {
        this.journalEntries = journalEntries;
        notifyDataSetChanged();
    }

    public List<JournalEntry> getJournalEntries(){
        return this.journalEntries;
    }

    public interface ItemClickListener {
        void onItemClickListener(int itemId);
    }

}
