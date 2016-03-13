package se.kth.csc.stayawhile;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class API {

    private static final String API_URL = "http://queue.csc.kth.se/API/";

    public static String loadURL(String url) throws IOException {
        int tries = 0;
        while(true) {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder body = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    body.append(line);
                }
                return body.toString();
            } catch (IOException e) {
                if (tries == 5) {
                    Log.e(API.class.getSimpleName(), "Could not load API call", e);
                    throw e;
                }
                try {
                    Thread.sleep(50 * (1 << tries));
                } catch (InterruptedException e1) {
                }
            }
        }
    }

    public static String sendAPICall(String method) throws IOException {
        return loadURL(API_URL + method);
    }

}
