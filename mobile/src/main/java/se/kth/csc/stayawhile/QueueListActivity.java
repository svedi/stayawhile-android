package se.kth.csc.stayawhile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import se.kth.csc.stayawhile.api.Queue;
import se.kth.csc.stayawhile.api.http.APICallback;
import se.kth.csc.stayawhile.api.http.APITask;
import se.kth.csc.stayawhile.api.UserData;
import se.kth.csc.stayawhile.api.http.GetQueueList;
import se.kth.csc.stayawhile.cookies.PersistentCookieStore;

public class QueueListActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private UserData mUserData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queuelist);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mUserData = UserData.fromJSON(getApplicationContext().getSharedPreferences("userData", Context.MODE_PRIVATE).getString("userData", "{}"));

        mRecyclerView = (RecyclerView) findViewById(R.id.queue_list);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        final SwipeRefreshLayout refresh = (SwipeRefreshLayout) findViewById(R.id.queue_list_refresh);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new APITask(new APICallback() {
                    @Override
                    public void r(String result) {
                        SharedPreferences.Editor e = getApplicationContext().getSharedPreferences("userData", Context.MODE_PRIVATE).edit();
                        e.putString("userData", result);
                        e.apply();
                        mUserData = UserData.fromJSON(result);
                        sendQueueList(new Runnable() {
                            @Override
                            public void run() {
                                refresh.setRefreshing(false);
                            }
                        });
                    }
                }).execute("method", "userData");
            }
        });
        sendQueueList(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sendQueueList(null);
    }

    private void sendQueueList(final Runnable runnable) {
        new GetQueueList() {
            @Override
            protected void onPostExecute(List<Queue> result) {
                QueueListActivity.this.onQueueUpdate(result);
                if (runnable != null) {
                    runnable.run();
                }
            }
        }.execute();
    }


    private void onQueueUpdate(List<Queue> queues) {
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
