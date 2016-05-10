package se.kth.csc.stayawhile.api;

import org.json.JSONObject;

public class Queue {
    private JSONObject json;
    private String name;

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
}
