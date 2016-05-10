package se.kth.csc.stayawhile.api;

import org.json.JSONObject;

import java.util.List;

public class UserData extends User {
    private boolean admin;
    private List<String> teacher;
    private List<String> assistant;

    public static UserData fromJSON(JSONObject json) {
        UserData userdata = new UserData();
        User.populateFromJSON(userdata, json);
        return userdata;
    }
}
