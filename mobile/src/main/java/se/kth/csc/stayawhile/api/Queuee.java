package se.kth.csc.stayawhile.api;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class Queuee extends User {
    /*
    private String location;
    private String time;
    private List<String> messages;
    private boolean gettingHelp;
    private String helper;
    private boolean help;
    private String comment;
    private boolean badLocation;
    */

    protected Queuee() { }

    public static Queuee fromJSON(String s) {
        try {
            JSONObject json = new JSONObject(s);
            return Queuee.fromJSON(json);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static Queuee fromJSON(JSONObject json) {
        Queuee queuee = new Queuee();
        User.populateFromJSON(queuee, json);

        boolean valid = json.has("location") && json.has("time") && json.has("messages")
                && json.has("gettingHelp") && json.has("helper")&& json.has("help")
                && json.has("comment")&& json.has("badLocation");
        if (!valid) { throw new RuntimeException("Queuee.fromJSON(): invalid JSONObject:" + json); }

        return queuee;
    }

    public boolean getBadLocation() {
        try {
            return super.getJSON().getBoolean("badLocation");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public String getLocation() {
        try {
            return super.getJSON().getString("location");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public String getComment() {
        try {
            return super.getJSON().getString("comment");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean getHelp() {
        try {
            return super.getJSON().getBoolean("help");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean getGettingHelp() {
        try {
            return super.getJSON().getBoolean("gettingHelp");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public String getHelper() {
        try {
            return super.getJSON().getString("helper");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public long getTime() {
        try {
            return super.getJSON().getLong("time");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
