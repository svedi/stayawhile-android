package se.kth.csc.stayawhile;

import android.os.AsyncTask;

import java.io.IOException;

public class APITask extends AsyncTask<String, Void, String> {

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
        }
        return null;

    }
}
