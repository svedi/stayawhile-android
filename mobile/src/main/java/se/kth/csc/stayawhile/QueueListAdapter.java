package se.kth.csc.stayawhile;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import se.kth.csc.stayawhile.api.http.APICallback;
import se.kth.csc.stayawhile.api.http.APITask;
import se.kth.csc.stayawhile.api.Queue;
import se.kth.csc.stayawhile.api.UserData;

public class QueueListAdapter extends RecyclerView.Adapter<QueueListAdapter.ViewHolder> {

    private UserData mUserData;
    private QueueListActivity activity;
    private List<Queue> mAssistantQueue;
    private List<Queue> mOtherQueues;

    public class ViewHolder extends RecyclerView.ViewHolder {

        public View mCardView;
        public Queue mData;

        public ViewHolder(View v) {
            super(v);
            mCardView = v;
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mUserData.isAssistant(mData)) {
                        Intent intent = new Intent(activity, QueueActivity.class);
                        intent.putExtra("queue", mData.getName());
                        QueueListAdapter.this.activity.startActivity(intent);
                    } else {
                        new APITask(new APICallback() {
                            @Override
                            public void r(String result) {
                                Bundle args = new Bundle();
                                args.putString("queue", result);
                                QueueAccessRequestDialogFragment q = new QueueAccessRequestDialogFragment();
                                q.setArguments(args);
                                q.show(activity.getFragmentManager(), "QueueAccessRequestDialogFragment");
                            }
                        }).execute("method", "queue/" + Uri.encode(mData.getName()));
                    }
                }
            });
        }

        public void setData(Queue data) {
            this.mData = data;
            TextView title = (TextView) mCardView.findViewById(R.id.queue_name);
            title.setText(data.getName());
            TextView length = (TextView) mCardView.findViewById(R.id.queue_people);
            length.setText(String.valueOf(data.getLength()));
            View titlebar = mCardView.findViewById(R.id.queue_titlebar);
            ImageView icon = (ImageView) mCardView.findViewById(R.id.queue_icon);
            if (mUserData.isAssistant(data)) {
                titlebar.setBackgroundColor(activity.getResources().getColor(R.color.colorPrimary));
                title.setTypeface(Typeface.DEFAULT_BOLD);
            } else {
                titlebar.setBackgroundColor(activity.getResources().getColor(android.R.color.darker_gray));
                title.setTypeface(Typeface.DEFAULT);
            }
            if (data.isLocked()) {
                icon.setImageResource(R.drawable.ic_lock_white_18dp);
            } else {
                icon.setImageResource(R.drawable.ic_people_outline_white_18dp);
            }
        }
    }

    public QueueListAdapter(List<Queue> queues, final QueueListActivity context, UserData userData) {
        mUserData = userData;
        mAssistantQueue = new ArrayList<>();
        mOtherQueues = new ArrayList<>();
        for (int i = 0; i < queues.size(); i++) {
            Queue queue = queues.get(i);
            if (queue.getHiding()) continue;
            if (mUserData.isAssistant(queue)) {
                mAssistantQueue.add(queue);
            } else {
                mOtherQueues.add(queue);
            }
        }
        final Comparator<Queue> queueComparator = new Comparator<Queue>() {
            @Override
            public int compare(Queue lhs, Queue rhs) {
                if (lhs.isLocked() && !rhs.isLocked()) {
                    return 1;
                } else if (!lhs.isLocked() && rhs.isLocked()) {
                    return -1;
                }
                return lhs.getName().compareTo(rhs.getName());
            }
        };
        Collections.sort(mAssistantQueue, queueComparator);
        Collections.sort(mOtherQueues, queueComparator);
        activity = context;
    }

    public Queue onPosition(int position) {
        if (position < mAssistantQueue.size()) {
            return mAssistantQueue.get(position);
        }
        return mOtherQueues.get(position - mAssistantQueue.size());
    }

    @Override
    public QueueListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.queue, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Queue queue = onPosition(position);
        holder.setData(queue);
    }

    @Override
    public int getItemCount() {
        return mAssistantQueue.size() + mOtherQueues.size();
    }
}
