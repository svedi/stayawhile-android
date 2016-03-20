package se.kth.csc.stayawhile;

import android.content.Context;
import android.net.Uri;
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

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import se.kth.csc.stayawhile.swipe.QueueTouchListener;

public class QueueActivity extends AppCompatActivity implements BroadcastDialogFragment.BroadcastListener {

    private RecyclerView mRecyclerView;
    private QueueAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private Socket mSocket;
    private JSONObject mQueue;
    private String mQueueName;
    private String mUgid;

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
        try {
            JSONObject userData = new JSONObject(getApplicationContext().getSharedPreferences("userData", Context.MODE_PRIVATE).getString("userData", "{}"));
            this.mUgid = userData.getString("ugKthid");
        } catch (JSONException json) {
        }
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
        QueueTouchListener swipeTouchListener =
                new QueueTouchListener(mRecyclerView,
                        new QueueTouchListener.QueueSwipeListener() {

                            @Override
                            public void onDismiss(RecyclerView recyclerView, int[] reverseSortedPositions) {
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
                            public void onSetHelp(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                List<JSONObject> users = new ArrayList<>();
                                for (int position : reverseSortedPositions) {
                                    users.add(mAdapter.onPosition(position));
                                }
                                for (JSONObject user : users) {
                                    try {
                                        if (user.getBoolean("gettingHelp")) {
                                            sendStopHelp(user);
                                        } else {
                                            sendHelp(user);
                                        }
                                    } catch (JSONException e) {
                                    }
                                }
                            }
                        });

        mRecyclerView.addOnItemTouchListener(swipeTouchListener);

        registerForContextMenu(mRecyclerView);

        sendQueueUpdate();
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
        mSocket.on("msg", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("msg " + Arrays.toString(args));
            }
        });
        mSocket.connect();
        mSocket.emit("listen", mQueueName);
    }

    private void sendQueueUpdate() {
        new APITask(new APICallback() {
            @Override
            public void r(String result) {
                try {
                    mQueue = new JSONObject(result);
                    System.out.println("update " + mQueue);
                    QueueActivity.this.onQueueUpdate();
                } catch (JSONException e) {
                    //TODO
                    e.printStackTrace();
                }
            }
        }).execute("method", "queue/" + Uri.encode(mQueueName));
    }

    private void sendStopHelp(JSONObject user) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("ugKthid", user.get("ugKthid"));
            obj.put("queueName", mQueueName);
            mSocket.emit("stopHelp", obj);
        } catch (JSONException e) {
        }
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

    private void sendLock() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("queueName", mQueueName);
            mSocket.emit("lock", obj);
            sendQueueUpdate();
        } catch (JSONException e) {
        }
    }

    private void sendUnlock() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("queueName", mQueueName);
            mSocket.emit("unlock", obj);
            sendQueueUpdate();
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

    private boolean isLocked() {
        try {
            if (mQueue.getBoolean("locked")) {
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
    }

    private void onQueueUpdate() {
        try {
            mAdapter = new QueueAdapter(mQueue.getJSONArray("queue"), mUgid);
            mRecyclerView.setAdapter(mAdapter);
            supportInvalidateOptionsMenu();
        } catch (JSONException e) {
        }
    }

    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.queue_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mQueue != null) {
            MenuItem lockItem = menu.findItem(R.id.action_lock);
            if (isLocked()) {
                lockItem.setIcon(R.drawable.ic_lock_open_white_24dp);
                lockItem.setTitle("Unlock");
            } else {
                lockItem.setIcon(R.drawable.ic_lock_outline_white_24dp);
                lockItem.setTitle("Lock");
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.action_broadcast:
            case R.id.action_broadcast_faculty:
                BroadcastDialogFragment fragment = new BroadcastDialogFragment();
                Bundle args = new Bundle();
                args.putInt("target", id);
                fragment.setArguments(args);
                fragment.show(getFragmentManager(), "BroadcastDialogFragment");
                return true;
            case R.id.action_lock:
                if (isLocked()) {
                    sendUnlock();
                } else {
                    sendLock();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void broadcast(String message, int target) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("queueName", mQueueName);
            obj.put("message", message);
            if (target == R.id.action_broadcast) {
                mSocket.emit("broadcast", obj);
            } else if (target == R.id.action_broadcast_faculty) {
                mSocket.emit("broadcastFaculty", obj);
            } else {
                throw new RuntimeException("broadcast with invalid target");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

