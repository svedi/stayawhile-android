package se.kth.csc.stayawhile.api;

import org.json.JSONException;
import org.json.JSONObject;

public class User {
    /*
    private String ugKthid;
    private String username;
    private String realname;
    */
    private JSONObject json;

    protected User() { }

    public static User fromJSON(String s) {
        try {
            JSONObject json = new JSONObject(s);
            return User.fromJSON(json);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static User fromJSON(JSONObject json) {
        User user = new User();
        User.populateFromJSON(user, json);

        boolean valid = json.has("ugKthid") && json.has("username") && json.has("realname");
        if (!valid) { throw new RuntimeException("User.fromJSON(): invalid JSONObject: " + json); }

        return user;
    }

    protected static void populateFromJSON(User user, JSONObject json) {
        user.json = json;
    }

    public JSONObject getJSON() {
        return this.json;
    }

    public String getUgKthid() {
        try {
            return json.getString("ugKthid");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public String getUsername() {
        try {
            return json.getString("username");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public String getRealname() {
        try {
            return json.getString("realname");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
