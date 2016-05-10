package se.kth.csc.stayawhile.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * This should probably be removed and replaced by List<Queue>, but keep it for now
 * as a wrapper so we can keep the JSONArray object too.
 */
public class QueueList {
    private JSONArray json;
    private List<Queue> queues;

    public static QueueList fromJSON(JSONArray json) {
        QueueList queueList = new QueueList();

        queueList.json = json;

        List<Queue> queues = new ArrayList<>();
        try {
            for (int i = 0; i < json.length(); ++i) {
                JSONObject queue = json.getJSONObject(i);
                queues.add(Queue.fromJSON(queue));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        queueList.queues = queues;

        return queueList;
    }

    public List<Queue> getQueues() {
        return queues;
    }

    public JSONArray getJSON() {
        return json;
    }
}
