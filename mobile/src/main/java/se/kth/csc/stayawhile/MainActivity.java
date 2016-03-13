package se.kth.csc.stayawhile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.JsonReader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.concurrent.ExecutionException;

import se.kth.csc.stayawhile.cookies.PersistentCookieStore;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        android.webkit.CookieManager.getInstance().setAcceptCookie(true);
        CookieManager cookieManager = new CookieManager(new PersistentCookieStore(getApplicationContext()), CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);

        new APITask(new APICallback() {
            @Override
            public void r(String result) {
                if (result.length() != 0) {
                    Intent queueList = new Intent(MainActivity.this, QueueListActivity.class);
                    startActivity(queueList);
                } else {
                    setContentView(R.layout.activity_main);
                    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                    setSupportActionBar(toolbar);
                }
            }
        }).execute("method", "userData");
    }

    public void onLogin(View view) {
        WebView webView = new WebView(this);
        setContentView(webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("https://login.kth.se/login?service=http://queue.csc.kth.se/auth");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (url.startsWith("http://queue.csc.kth.se/auth")) {
                    MainActivity.this.onTicket(url);
                    view.stopLoading();
                }
            }
        });
    }

    public void onTicket(String url) {
        try {
            new APITask().execute("url", url).get();
            new APITask(new APICallback() {
                @Override
                public void r(String result) {
                    SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences("userData", Context.MODE_PRIVATE).edit();
                    editor.putString("userData", result);
                    editor.apply();
                    Intent queueList = new Intent(MainActivity.this, QueueListActivity.class);
                    startActivity(queueList);
                }
            }).execute("method", "userData");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
