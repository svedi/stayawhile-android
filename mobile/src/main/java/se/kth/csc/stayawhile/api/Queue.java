package se.kth.csc.stayawhile.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Queue {
    private JSONObject json;
    private String name;
    private List<Queuee> queuees;

    public static Queue fromJSON(String s) {
        try {
            JSONObject json = new JSONObject(s);
            return Queue.fromJSON(json);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static Queue fromJSON(JSONObject json) {
        Queue queue = new Queue();
        Queue.populateFromJSON(queue, json);
        return queue;
    }

    protected static void populateFromJSON(Queue queue, JSONObject json) {
        queue.json = json;
    }

    public JSONObject getJSON() {
        return this.json;
    }

    public String getName() {
        try {
            return json.getString("name");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public int getLength() {
        try {
            // TODO: Or just queuees.size()?
            return json.getInt("length");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean getHiding() {
        try {
            return json.getBoolean("hiding");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isLocked() {
        try {
            return json.getBoolean("locked");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Queuee> getQueuees() {
        if (queuees != null) { return queuees; }

        try {
            queuees = new ArrayList<>();
            JSONArray list = json.getJSONArray("queue");
            for (int i = 0; i < list.length(); ++i) {
                queuees.add(Queuee.fromJSON(list.getJSONObject(i)));
            }

            return queuees;
        } catch (JSONException e) {
            queuees = null; // TODO: This is kinda ugly...
            throw new RuntimeException(e);
        }
    }
}
