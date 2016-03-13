package se.kth.csc.stayawhile;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class APITask extends AsyncTask<String, Void, String> {

    private APICallback callback;

    public APITask() {

    }

    public APITask(APICallback callback) {
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            if (params[0].equals("url")) {
                return API.loadURL(params[1]);
            }
            if (params[0].equals("method")) {
                return API.sendAPICall(params[1]);
            }
        } catch (IOException e) {
            //TODO: handle exception
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (this.callback != null) {
            this.callback.r(s);
        }
    }
}
