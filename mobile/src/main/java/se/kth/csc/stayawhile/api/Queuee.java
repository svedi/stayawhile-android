package se.kth.csc.stayawhile.api;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class Queuee extends User {
    private String location;
    private String time;
    private List<String> messages;
    private boolean gettingHelp;
    private String helper;
    private boolean help;
    private String comment;
    private boolean badLocation;

    public static Queuee fromJSON(JSONObject json) {
        Queuee queuee = new Queuee();
        User.populateFromJSON(queuee, json);

        try {
            queuee.location = json.getString("location");
            queuee.time = json.getString("time");
            queuee.comment = json.getString("comment");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return queuee;
    }
}
