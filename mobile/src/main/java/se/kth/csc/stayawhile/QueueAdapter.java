package se.kth.csc.stayawhile;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Queue;

public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.ViewHolder> {

    private Context mContext;
    private JSONArray mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View mCardView;
        public JSONObject mData;

        public ViewHolder(View v) {
            super(v);
            mCardView = v;
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, QueueActivity.class);
                    QueueAdapter.this.mContext.startActivity(intent);
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

    // Provide a suitable constructor (depends on the kind of dataset)
    public QueueAdapter(JSONArray myDataset, Context context) {
        mDataset = myDataset;
        mContext = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public QueueAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
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

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.length();
    }

}
