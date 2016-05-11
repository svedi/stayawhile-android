package se.kth.csc.stayawhile.api.http;

import android.net.Uri;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import se.kth.csc.stayawhile.api.Queue;

public abstract class GetQueue extends AsyncTask<Void, Void, Queue> {
    private String mQueueName;

    public GetQueue(String queueName) {
        this.mQueueName = queueName;
    }

    @Override
    protected Queue doInBackground(Void... params) {
        try {
            String raw = API.sendAPICall("queue/" + Uri.encode(mQueueName));
            return Queue.fromJSON(new JSONObject(raw));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Not sure if this works, is it important to exectute super.onPostExecute()?
    @Override
    abstract protected void onPostExecute(Queue result);
}
