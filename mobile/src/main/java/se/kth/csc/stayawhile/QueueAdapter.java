package se.kth.csc.stayawhile;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.ViewHolder> {

    private List<JSONObject> mWaiting;
    private List<JSONObject> mGettingHelp;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        public View mCardView;
        public JSONObject mData;

        public ViewHolder(View v) {
            super(v);
            mCardView = v;
            v.setOnCreateContextMenuListener(this);
        }

        public void setData(JSONObject data, int position) {
            this.mData = data;
            try {
                TextView title = (TextView) mCardView.findViewById(R.id.queuee_name);
                title.setText(data.getString("realname"));
                TextView location = (TextView) mCardView.findViewById(R.id.queuee_location);
                location.setText(data.getString("location"));
                TextView comment = (TextView) mCardView.findViewById(R.id.queuee_comment);
                comment.setText(data.getString("comment"));
                TextView description = (TextView) mCardView.findViewById(R.id.queuee_description);
                RelativeLayout rel = (RelativeLayout)  mCardView.findViewById(R.id.queuee_background);
                if(data.getBoolean("help")) {
                    description.setText("Help");
                } else {
                    description.setText("Present");
                }

                if (data.getBoolean("gettingHelp")) {
                    rel.setBackgroundColor(mCardView.getResources().getColor(R.color.colorPrimary));
                    title.setTypeface(Typeface.DEFAULT);
                    location.setTypeface(Typeface.DEFAULT);
                } else {
                    rel.setBackgroundColor(mCardView.getResources().getColor(R.color.colorAccent));
                    if (position == 0) {
                        title.setText("\u25B6 " + title.getText());
                        title.setTypeface(Typeface.DEFAULT_BOLD);
                        location.setTypeface(Typeface.DEFAULT_BOLD);
                    }
                }
            } catch (JSONException e) {
                //TODO
            }
        }


        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.add("Remove from queue");
            menu.add("Send message");
        }
    }

    public QueueAdapter(JSONArray myDataset, Context context) {
        mWaiting = new ArrayList<>();
        mGettingHelp = new ArrayList<>();
        for (int i = 0; i < myDataset.length(); i++) {
            try {
                JSONObject obj = myDataset.getJSONObject(i);
                if (obj.getBoolean("gettingHelp")) {
                    mGettingHelp.add(obj);
                } else {
                    mWaiting.add(obj);
                }
            } catch (JSONException je) {
            }
        }
    }

    public int positionOf(String ugKthid) {
        try {
            for (int i = 0; i < getItemCount(); i++) {
                if (onPosition(i).getString("ugKthid").equals(ugKthid)) {
                    return i;
                }
            }
        } catch (JSONException e) {
        }
        return -1;
    }

    public JSONObject onPosition(int pos) {
        if (pos < mWaiting.size()) return mWaiting.get(pos);
        return mGettingHelp.get(pos - mWaiting.size());
    }

    public void set(int pos, JSONObject user) {
        if (pos < mWaiting.size()) mWaiting.set(pos, user);
        else mGettingHelp.set(pos - mWaiting.size(), user);
        notifyItemChanged(pos);
    }

    public void add(JSONObject person) {
        try {
            if (person.getBoolean("gettingHelp")) {
                notifyItemInserted(mWaiting.size() + insertInto(mGettingHelp, person));
            } else {
                int pos = insertInto(mWaiting, person);
                notifyItemInserted(pos);
                if (pos == 0 && mWaiting.size() >= 2) {
                    notifyItemChanged(1);
                }
            }
        } catch(JSONException e){
        }
    }

    private int insertInto(List<JSONObject> queue, JSONObject person) {
        try {
            int at = 0;
            while (at < queue.size() && queue.get(at).getLong("time") < person.getLong("time"))
                at++;
            queue.add(at, person);
            return at;
        } catch (JSONException js){
        }
        return -1;
    }

    public void removePosition(int pos) {
        if (pos < mWaiting.size()) mWaiting.remove(pos);
        else mGettingHelp.remove(pos - mWaiting.size());
        notifyItemRemoved(pos);
        if (pos == 0 && mWaiting.size() > 0) {
            notifyItemChanged(0);
        }
    }


    public boolean isWaiting(int position) {
        return position < mWaiting.size();
    }

    @Override
    public int getItemCount() {
        return mWaiting.size() + mGettingHelp.size();
    }

    @Override
    public QueueAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.queuee, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        JSONObject queue = onPosition(position);
        holder.setData(queue, position);
    }


}
