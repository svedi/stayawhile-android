package se.kth.csc.stayawhile;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import se.kth.csc.stayawhile.api.Queuee;
import se.kth.csc.stayawhile.api.User;

public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.ViewHolder> {

    private StudentActionListener mActionListener;
    private List<Queuee> mWaiting;
    private List<Queuee> mGettingHelp;
    private List<Queuee> mGettingHelpByAssistant;
    private String mUgid;

    public List<Queuee> getWaiting() {
        return mWaiting;
    }

    public List<Queuee> getHelpedByMe() {
        return mGettingHelpByAssistant;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        public View mCardView;
        public Queuee mData;

        public ViewHolder(View v) {
            super(v);
            mCardView = v;
            v.setOnCreateContextMenuListener(this);
            Button cantFind = (Button) mCardView.findViewById(R.id.queuee_action_cantfind);
            cantFind.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mActionListener.cantFind(mData);
                }
            });
        }

        public void setData(Queuee data, int position) {
            this.mData = data;
            TextView title = (TextView) mCardView.findViewById(R.id.queuee_name);
            title.setText(data.getRealname());
            TextView location = (TextView) mCardView.findViewById(R.id.queuee_location);
            if (data.getBadLocation()) {
                location.setText("???");
            } else {
                location.setText(data.getLocation());
            }
            TextView comment = (TextView) mCardView.findViewById(R.id.queuee_comment);
            comment.setText(data.getComment());
            TextView description = (TextView) mCardView.findViewById(R.id.queuee_description);
            View titleBar = mCardView.findViewById(R.id.queuee_background);
            View actions = mCardView.findViewById(R.id.queuee_actions);

            if (data.getHelp()) {
                description.setText("Help");
            } else {
                description.setText("Present");
            }
            if (QueueAdapter.this.helpedByMe(data)) {
                actions.setVisibility(View.VISIBLE);
            } else {
                actions.setVisibility(View.GONE);
            }
            if (QueueAdapter.this.helpedByMe(data) || (!data.getGettingHelp() && position == 0)) {
                title.setText("\u25B6 " + title.getText());
                title.setTypeface(Typeface.DEFAULT_BOLD);
                location.setTypeface(Typeface.DEFAULT_BOLD);
            } else {
                title.setTypeface(Typeface.DEFAULT);
                location.setTypeface(Typeface.DEFAULT);
            }
            if (data.getGettingHelp()) {
                titleBar.setBackgroundColor(mCardView.getResources().getColor(R.color.colorPrimary));
            } else if (data.getBadLocation()) {
                titleBar.setBackgroundColor(mCardView.getResources().getColor(android.R.color.holo_red_dark));
            } else {
                titleBar.setBackgroundColor(mCardView.getResources().getColor(R.color.colorAccent));
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle(((TextView) v.findViewById(R.id.queuee_name)).getText());
            mActionListener.getMenuInflater(mData).inflate(R.menu.student_context_menu, menu);

            if (mData != null) {
                MenuItem helpItem = menu.findItem(R.id.actionHelp);
                if (mData.getGettingHelp()) { // TODO: Might have broken this...
                    helpItem.setTitle("Stop Help");
                } else {
                    helpItem.setTitle("Help");
                }
            }
        }
    }

    public QueueAdapter(List<Queuee> queuees, QueueActivity activity, String ugid) {
        mActionListener = activity;
        mWaiting = new ArrayList<>();
        mGettingHelp = new ArrayList<>();
        mGettingHelpByAssistant = new ArrayList<>();
        mUgid = ugid;
        for (int i = 0; i < queuees.size(); i++) {
            Queuee obj = queuees.get(i);
            System.out.println("queue add person " + obj.getJSON());
            if (obj.getGettingHelp()) {
                System.out.println("person is getting help");
                if (obj.getJSON().has("helper")) {
                    System.out.println("with helper " + obj.getHelper());
                }
                if (obj.getJSON().has("helper") && obj.getHelper().equals(mUgid)) {
                    mGettingHelpByAssistant.add(obj);
                } else {
                    mGettingHelp.add(obj);
                }
            } else {
                mWaiting.add(obj);
            }
        }
    }

    public boolean helpedByMe(Queuee user) {
        return user.getGettingHelp() && user.getJSON().has("helper") && user.getHelper().equals(mUgid);
    }

    public int positionOf(String ugKthid) {
        for (int i = 0; i < getItemCount(); i++) {
            if (onPosition(i).getUgKthid().equals(ugKthid)) {
                return i;
            }
        }

        return -1;
    }

    public Queuee onPosition(int pos) {
        if (pos < mGettingHelpByAssistant.size()) return mGettingHelpByAssistant.get(pos);
        pos -= mGettingHelpByAssistant.size();
        if (pos < mWaiting.size()) return mWaiting.get(pos);
        pos -= mWaiting.size();
        return mGettingHelp.get(pos);
    }

    public void set(int pos, Queuee user) {
        if (pos < mGettingHelpByAssistant.size()) mGettingHelpByAssistant.set(pos, user);
        else if (pos < mGettingHelpByAssistant.size() + mWaiting.size())
            mWaiting.set(pos - mGettingHelpByAssistant.size(), user);
        else mGettingHelp.set(pos - mGettingHelpByAssistant.size() - mWaiting.size(), user);
        notifyItemChanged(pos);
    }

    public void add(Queuee person) {
        if (person.getGettingHelp()) {
            if (person.getJSON().has("helper") && person.getHelper().equals(mUgid)) {
                notifyItemInserted(insertInto(mGettingHelpByAssistant, person));
            } else {
                notifyItemInserted(mGettingHelpByAssistant.size() + mWaiting.size() + insertInto(mGettingHelp, person));
            }
        } else {
            int pos = mGettingHelpByAssistant.size() + insertInto(mWaiting, person);
            notifyItemInserted(pos);
        }
        if (getItemCount() > 1) notifyItemChanged(1);
    }

    private int insertInto(List<Queuee> queue, Queuee person) {
        int at = 0;
        if (person.getBadLocation()) {
            at = queue.size();
        } else {
            while (at < queue.size() && queue.get(at).getTime() < person.getTime())
                at++;
        }
        queue.add(at, person);
        return at;
    }

    public void removePosition(int pos) {
        if (pos < mGettingHelpByAssistant.size()) mGettingHelpByAssistant.remove(pos);
        else if (pos < mGettingHelpByAssistant.size() + mWaiting.size())
            mWaiting.remove(pos - mGettingHelpByAssistant.size());
        else mGettingHelp.remove(pos - mGettingHelpByAssistant.size() - mWaiting.size());
        notifyItemRemoved(pos);
        if (getItemCount() > 0) notifyItemChanged(0);
    }

    public boolean isWaiting(int position) {
        return mGettingHelpByAssistant.size() <= position && position < mGettingHelpByAssistant.size() + mWaiting.size();
    }

    @Override
    public int getItemCount() {
        return mGettingHelpByAssistant.size() + mWaiting.size() + mGettingHelp.size();
    }

    @Override
    public QueueAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.queuee, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Queuee queuee = onPosition(position);
        holder.setData(queuee, position);
    }

    public interface StudentActionListener {
        void cantFind(User student);

        //boolean gettingHelp(User data);

        // Not sure if this needs a Queuee or just User or whatever?
        MenuInflater getMenuInflater(Queuee data);
    }
}
