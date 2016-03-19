package se.kth.csc.stayawhile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class QueueListAdapter extends RecyclerView.Adapter<QueueListAdapter.ViewHolder> {

    private Context mContext;
    private JSONArray mDataset;

    public class ViewHolder extends RecyclerView.ViewHolder {

        public View mCardView;
        public JSONObject mData;

        public ViewHolder(View v) {
            super(v);
            mCardView = v;
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, QueueActivity.class);
                    Bundle b = new Bundle();
                    try {
                        intent.putExtra("queue", mData.getString("name"));
                    } catch (JSONException e) {
                        // TODO
                    }
                    QueueListAdapter.this.mContext.startActivity(intent);
                }
            });
        }

        public void setData(JSONObject data) {
            this.mData = data;
            try {
                TextView title = (TextView) mCardView.findViewById(R.id.queue_name);
                title.setText(data.getString("name"));
                TextView length = (TextView) mCardView.findViewById(R.id.queue_people);
                length.setText(String.valueOf(data.getInt("length")));
            } catch (JSONException e) {
                //TODO
            }
        }
    }

    public QueueListAdapter(JSONArray myDataset, Context context) {
        mDataset = myDataset;
        mContext = context;
    }

    @Override
    public QueueListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.queue, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        try {
            JSONObject queue = mDataset.getJSONObject(position);
            holder.setData(queue);
        } catch (JSONException e) {
            e.printStackTrace();
            // TODO
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.length();
    }

}
