package se.kth.csc.stayawhile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import se.kth.csc.stayawhile.cookies.PersistentCookieStore;

public class QueueListActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private JSONObject mUserData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queuelist);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        try {
            mUserData = new JSONObject(getApplicationContext().getSharedPreferences("userData", Context.MODE_PRIVATE).getString("userData", "{}"));
        } catch (JSONException json) {
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.queue_list);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        new APITask(new APICallback() {
            @Override
            public void r(String result) {
                try {
                    JSONArray queues = new JSONArray(result);
                    QueueListActivity.this.onQueueUpdate(queues);
                } catch (JSONException e) {
                    //TODO
                    e.printStackTrace();
                }
            }
        }).execute("method", "queueList");
    }

    private void onQueueUpdate(JSONArray queues) {
        mAdapter = new QueueListAdapter(queues, this, mUserData);
        mRecyclerView.setAdapter(mAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            new PersistentCookieStore(getApplicationContext()).removeAll();
            Intent queueList = new Intent(this, MainActivity.class);
            queueList.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(queueList);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
