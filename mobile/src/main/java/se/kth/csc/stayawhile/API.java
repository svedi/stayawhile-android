package se.kth.csc.stayawhile;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Interface to the Stay-A-While HTTP API.
 */
public class API {

    private static final String API_URL = "http://queue.csc.kth.se/API/";

    /**
     * Load the body of a resource from its URL.
     * Cookies will be taken from the system default,
     * so that any calls to the API will be authenticated
     * using the user credentials.
     *
     * @param url the URL of the resource to load
     * @return the resource body
     * @throws IOException if the resource could not be loaded successfully.
     */
    public static String loadURL(String url) throws IOException {
        int tries = 0;
        while (true) {
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
                // retries with exponential backoff
                if (tries == 5) {
                    Log.e(API.class.getSimpleName(), "Could not load resource: " + url, e);
                    throw e;
                }
                try {
                    Thread.sleep(50 * (1 << tries));
                } catch (InterruptedException e1) {
                }
            }
        }
    }

    /**
     * Perform an API call to the Stay-A-While HTTP API
     *
     * @param method the method to call
     * @return the body of the API call
     * @throws IOException if the resource could not be loaded successfully
     */
    public static String sendAPICall(String method) throws IOException {
        return loadURL(API_URL + method);
    }

}
