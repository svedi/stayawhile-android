package se.kth.csc.stayawhile.api.http;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import se.kth.csc.stayawhile.api.Queue;
import se.kth.csc.stayawhile.api.QueueList;

public abstract class GetQueueList extends AsyncTask<Void, Void, QueueList> {
    /*
    @Override
    protected List<Queue> doInBackground(Void... params) {
        List<Queue> result = new ArrayList<>();
        try {
            String raw = API.sendAPICall("queueList");
            JSONArray queues = new JSONArray(raw);
            for (int i = 0; i < queues.length(); ++i) {
                JSONObject queue = queues.getJSONObject(i);
                result.add(Queue.fromJSON(queue));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
    */
    @Override
    protected QueueList doInBackground(Void... params) {
        try {
            String raw = API.sendAPICall("queueList");
            return QueueList.fromJSON(new JSONArray(raw));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    // Not sure if this works, is it important to exectute super.onPostExecute()?
    @Override
    abstract protected void onPostExecute(QueueList result);
}
