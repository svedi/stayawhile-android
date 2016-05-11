package se.kth.csc.stayawhile.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public class UserData extends User {
    /*
    private boolean admin;
    */
    private Set<String> assistant;
    private Set<String> teacher;

    protected UserData() {
    }

    public static UserData fromJSON(String s) {
        try {
            JSONObject json = new JSONObject(s);
            return UserData.fromJSON(json);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static UserData fromJSON(JSONObject json) {
        UserData userdata = new UserData();
        User.populateFromJSON(userdata, json);

        boolean valid = json.has("admin") && json.has("teacher") && json.has("assistant")
                && json.has("location");
        if (!valid) {
            throw new RuntimeException("Queuee.fromJSON(): invalid JSONObject:" + json);
        }

        return userdata;
    }

    private void rebuildAssistantTeacher() {
        try {
            JSONArray assistants = super.getJSON().getJSONArray("assistant");
            JSONArray teachers = super.getJSON().getJSONArray("teacher");

            this.assistant = new HashSet<>(assistants.length(), 1.0F);
            this.teacher = new HashSet<>(teachers.length(), 1.0F);

            for (int i = 0; i < assistants.length(); ++i) {
                this.assistant.add(assistants.getString(i));
            }
            for (int i = 0; i < teachers.length(); ++i) {
                this.teacher.add(teachers.getString(i));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isAssistant(Queue queue) {
        if (assistant == null || teacher == null) {
            this.rebuildAssistantTeacher();
        }

        return assistant.contains(queue.getName()) || teacher.contains(queue.getName());
    }
}
