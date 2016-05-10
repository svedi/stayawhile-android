package se.kth.csc.stayawhile.api;

import org.json.JSONException;
import org.json.JSONObject;

public class User {
    private JSONObject json;

    private String ugKthid;
    private String username;
    private String realname;

    public static User fromJSON(JSONObject json) {
        User user = new User();
        User.populateFromJSON(user, json);
        return user;
    }

    protected static void populateFromJSON(User user, JSONObject json) {
        user.json = json;
        try {
            user.ugKthid = json.getString("ugKthid");
            user.username = json.getString("username");
            user.realname = json.getString("realname");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public JSONObject getJSON() {
        return this.json;
    }
}
