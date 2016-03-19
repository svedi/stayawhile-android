package se.kth.csc.stayawhile;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import se.kth.csc.stayawhile.swipe.SwipeableRecyclerViewTouchListener;

public class QueueActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private QueueAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private Socket mSocket;
    private JSONObject mQueue;
    private String mQueueName;

    {
        try {
            mSocket = IO.socket("http://queue.csc.kth.se/");
        } catch (URISyntaxException e) {
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mQueueName = getIntent().getStringExtra("queue");
        setContentView(R.layout.activity_queue);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(mQueueName);

        mRecyclerView = (RecyclerView) findViewById(R.id.queue_people);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        SwipeableRecyclerViewTouchListener swipeTouchListener =
                new SwipeableRecyclerViewTouchListener(mRecyclerView,
                        new SwipeableRecyclerViewTouchListener.SwipeListener() {
                            @Override
                            public boolean canSwipeLeft(int position) {
                                return true;
                            }

                            public boolean canSwipeRight(int position) {
                                return mAdapter.isWaiting(position);
                            }

                            @Override
                            public void onDismissedBySwipeLeft(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                List<JSONObject> users = new ArrayList<>();
                                for (int position : reverseSortedPositions) {
                                    users.add(mAdapter.onPosition(position));
                                    mAdapter.removePosition(position);
                                }
                                mAdapter.notifyDataSetChanged();
                                for (JSONObject user : users) {
                                    sendKick(user);
                                }
                            }

                            @Override
                            public void onDismissedBySwipeRight(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                List<JSONObject> users = new ArrayList<>();
                                for (int position : reverseSortedPositions) {
                                    users.add(mAdapter.onPosition(position));
                                    mAdapter.removePosition(position);
                                }
                                mAdapter.notifyDataSetChanged();
                                for (JSONObject user : users) {
                                    sendHelp(user);
                                }
                            }
                        });

        mRecyclerView.addOnItemTouchListener(swipeTouchListener);

        registerForContextMenu(mRecyclerView);

        new APITask(new APICallback() {
            @Override
            public void r(String result) {
                try {
                    mQueue = new JSONObject(result);
                    QueueActivity.this.onQueueUpdate();
                } catch (JSONException e) {
                    //TODO
                    e.printStackTrace();
                }
            }
        }).execute("method", "queue/" + Uri.encode(mQueueName));
        mSocket.on("join", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("join " + Arrays.toString(args));
                newUser(args);
            }
        });
        mSocket.on("leave", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("leave " + Arrays.toString(args));
                removeUser(args);
            }
        });
        mSocket.on("update", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("update " + Arrays.toString(args));
                updateUser(args);
            }
        });
        mSocket.on("help", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("help " + Arrays.toString(args));
                setHelp(args);
            }
        });
        mSocket.on("stopHelp", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("stopHelp " + Arrays.toString(args));
                setStopHelp(args);
            }
        });
        mSocket.connect();
        mSocket.emit("listen", mQueueName);
    }

    private void sendHelp(JSONObject user) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("ugKthid", user.get("ugKthid"));
            obj.put("queueName", mQueueName);
            mSocket.emit("help", obj);
        } catch (JSONException e) {
        }
    }

    private void sendKick(JSONObject user) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("user", user);
            obj.put("queueName", mQueueName);
            mSocket.emit("kick", obj);
        } catch (JSONException e) {
        }
    }

    private void setStopHelp(Object... args) {
        try {
            JSONObject user = (JSONObject) args[0];
            int pos = mAdapter.positionOf(user.getString("ugKthid"));
            if (pos >= 0) {
                JSONObject existing = mAdapter.onPosition(pos);
                mAdapter.removePosition(pos);
                existing.put("gettingHelp", false);
                existing.remove("helper");
                mAdapter.add(existing);
            }
        } catch (JSONException e) {
        }
    }

    private void setHelp(Object... args) {
        try {
            JSONObject user = (JSONObject) args[0];
            int pos = mAdapter.positionOf(user.getString("ugKthid"));
            System.out.println("found user at " + mAdapter);
            if (pos >= 0) {
                JSONObject existing = mAdapter.onPosition(pos);
                mAdapter.removePosition(pos);
                existing.put("gettingHelp", true);
                if (user.has("helper")) {
                    existing.put("helper", user.get("helper"));
                }
                mAdapter.add(existing);
            }
        } catch (JSONException e) {
        }
    }

    private void updateUser(Object... args) {
        try {
            JSONObject user = (JSONObject) args[0];
            int pos = mAdapter.positionOf(user.getString("ugKthid"));
            if (pos >= 0) {
                mAdapter.set(pos, user);
            }
        } catch (JSONException e) {
        }

        try {
            JSONObject user = (JSONObject) args[0];
            JSONArray queuees = mQueue.getJSONArray("queue");
            for (int i = 0; i < queuees.length(); i++) {
                if (queuees.getJSONObject(i).getString("ugKthid").equals(user.getString("ugKthid"))) {
                    queuees.put(i, user);
                    break;
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onQueueUpdate();
                }
            });
        } catch (JSONException e) {
        }
    }

    private void newUser(Object... args) {
        JSONObject person = (JSONObject) args[0];
        mAdapter.add(person);
    }

    private void removeUser(Object... args) {
        try {
            String id = ((JSONObject) args[0]).getString("ugKthid");
            int pos = mAdapter.positionOf(id);
            if (pos >= 0) {
                mAdapter.removePosition(pos);
            }
        } catch (JSONException e) {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
    }

    private void onQueueUpdate() {
        try {
            mAdapter = new QueueAdapter(mQueue.getJSONArray("queue"), this);
            mRecyclerView.setAdapter(mAdapter);
        } catch (JSONException e) {
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onSupportNavigateUp() {
        onBackPressed();
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
