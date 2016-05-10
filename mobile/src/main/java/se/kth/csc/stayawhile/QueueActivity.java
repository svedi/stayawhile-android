package se.kth.csc.stayawhile;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.socket.emitter.Emitter;
import se.kth.csc.stayawhile.api.http.APICallback;
import se.kth.csc.stayawhile.api.http.APITask;
import se.kth.csc.stayawhile.api.Queue;
import se.kth.csc.stayawhile.api.websocket.Websocket;
import se.kth.csc.stayawhile.swipe.QueueTouchListener;

public class QueueActivity extends AppCompatActivity implements MessageDialogFragment.MessageListener, QueueAdapter.StudentActionListener {

    private RecyclerView mRecyclerView;
    private QueueAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private Websocket mSocket;
    private Queue mQueue;
    private String mQueueName;
    private String mUgid;
    private PowerManager.WakeLock mWakeLock;
    private JSONObject curContextMenuObj;

    { mSocket = new Websocket(); }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mQueueName = getIntent().getStringExtra("queue");
        mSocket.setQueueName(mQueueName);
        try {
            JSONObject userData = new JSONObject(getApplicationContext().getSharedPreferences("userData", Context.MODE_PRIVATE).getString("userData", "{}"));
            this.mUgid = userData.getString("ugKthid");
        } catch (JSONException json) {
            throw new RuntimeException(json);
        }
        setContentView(R.layout.activity_queue);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(mQueueName);

        this.mRecyclerView = (RecyclerView) findViewById(R.id.queue_people);
        this.mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        this.mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addOnItemTouchListener(
                new QueueTouchListener(
                        mRecyclerView,
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
                                    mSocket.sendKick(user);
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
                                            mSocket.sendStopHelp(user);
                                        } else {
                                            mSocket.sendHelp(user);
                                        }
                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                        }
                )
        );

        registerForContextMenu(mRecyclerView);

        sendQueueUpdate();
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "QueueActivity");
        mWakeLock.acquire();
        setSocketListeners();
        setupNotifications();
    }

    private void setSocketListeners() {
        final Handler h = new Handler();
        mSocket.onJoin(new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                newUser(args);
            }
        });
        mSocket.onLeave(new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                removeUser(args);
            }
        });
        mSocket.onUpdate(new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                updateUser(args);
            }
        });
        mSocket.onHelp(new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                setHelp(args);
                try {
                    MainActivity.wearMessageHandler.sendQueueToWear(mQueue.getJSON());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        mSocket.onStopHelp(new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        setStopHelp(args);
                    }
                });
            }
        });
        mSocket.onMsg(new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("msg " + Arrays.toString(args));
            }
        });
        mSocket.onConnect(new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                sendQueueUpdate();
            }
        });
        mSocket.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
        mWakeLock.release();
    }

    private void sendQueueUpdate() {
        new APITask(new APICallback() {
            @Override
            public void r(String result) {
                try {
                    mQueue = Queue.fromJSON(new JSONObject(result));
                    QueueActivity.this.onQueueUpdate();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }).execute("method", "queue/" + Uri.encode(mQueueName));
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
            throw new RuntimeException(e);
        }
    }

    private void setHelp(Object... args) {
        try {
            JSONObject user = (JSONObject) args[0];
            int pos = mAdapter.positionOf(user.getString("ugKthid"));
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
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
        }

        try {
            JSONObject user = (JSONObject) args[0];
            JSONArray queuees = mQueue.getJSON().getJSONArray("queue");
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
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
        }
    }

    private boolean isLocked() {
        try {
            return mQueue.getJSON().getBoolean("locked");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void onQueueUpdate() {
        try {
            mAdapter = new QueueAdapter(mQueue.getJSON().getJSONArray("queue"), this, mUgid);
            mRecyclerView.setAdapter(mAdapter);
            supportInvalidateOptionsMenu();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


        try {
            MainActivity.wearMessageHandler.sendQueueToWear(mQueue.getJSON());
        } catch (JSONException e) {
            e.printStackTrace();
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
                MessageDialogFragment fragment = new MessageDialogFragment();
                fragment.setTitle("Send message");
                Bundle args = new Bundle();
                args.putInt("target", id == R.id.action_broadcast ? BROADCAST_ALL : BROADCAST_FACULTY);
                fragment.setArguments(args);
                fragment.show(getFragmentManager(), "MessageDialogFragment");
                return true;
            case R.id.action_lock:
                if (isLocked()) {
                    mSocket.sendUnlock();
                    sendQueueUpdate(); // TODO: Not sure if this really works reliably
                } else {
                    mSocket.sendLock();
                    sendQueueUpdate(); // TODO: Not sure if this really works reliably
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void message(String message, Bundle arguments) {
        int target = arguments.getInt("target");
        if (target == BROADCAST_ALL) {
            mSocket.sendBroadcast(message);
        } else if (target == BROADCAST_FACULTY) {
            mSocket.sendBroadcastFaculty(message);
        } else if (target == PRIVATE_MESSAGE) {
            mSocket.sendMessageUser(message, arguments.getString("ugKthid"));
        } else if (target == COMMENT) {
            mSocket.sendFlag(message, arguments.getString("ugKthid"));
        } else if (target == COMPLETION) {
            mSocket.sendCompletion(message, arguments.getString("ugKthid"));
        } else {
            throw new RuntimeException("message with invalid target");
        }
    }

    @Override
    public void cantFind(JSONObject student) {
        mSocket.sendBadLocation(student);
        mSocket.sendStopHelp(student);
    }

    private void setupNotifications() {
        mSocket.onJoin(new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (mAdapter.getWaiting().size() == 0 && mAdapter.getHelpedByMe().size() == 0)
                    return;
                Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplication())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Stay-A-While")
                        .setContentText("Someone just joined the queue " + mQueueName)
                        .setSound(alarmSound);
                Intent notificationIntent = getIntent();
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                PendingIntent contentIntent = PendingIntent.getActivity(getApplication(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(contentIntent);
                builder.setAutoCancel(true);
                builder.setLights(Color.BLUE, 500, 500);
                builder.setVibrate(new long[]{200, 200, 200});
                builder.setStyle(new NotificationCompat.InboxStyle());
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                manager.notify(mQueueName, 1, builder.build());
            }
        });
    }

    public MenuInflater getMenuInflater(JSONObject curObj) {
        this.curContextMenuObj = curObj;
        return super.getMenuInflater();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int target = -1;
        String title = "";
        final int id = item.getItemId();
        switch (id) {
            case R.id.actionRemove:
                mSocket.sendKick(curContextMenuObj);
                System.out.println("kick");
                return true;
            case R.id.actionHelp:
                if (gettingHelp(curContextMenuObj)) {
                    mSocket.sendStopHelp(curContextMenuObj);
                } else {
                    mSocket.sendHelp(curContextMenuObj);
                    System.out.println("help");
                }
                return true;
            case R.id.actioBadLocation:
                cantFind(curContextMenuObj);
                System.out.println("bad location");
                return true;
            case R.id.actionMessage:
                target = PRIVATE_MESSAGE;
                title = "Send message";
                break;
            case R.id.actionComment:
                target = COMMENT;
                title = "Add comment";
                break;
            case R.id.actionCompletion:
                target = COMPLETION;
                title = "Add Task";
                break;
        }

        MessageDialogFragment fragment = new MessageDialogFragment();
        fragment.setTitle(title);
        Bundle args = new Bundle();

        args.putInt("target", target);
        try {
            args.putString("ugKthid", curContextMenuObj.getString("ugKthid"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        fragment.setArguments(args);
        fragment.show(getFragmentManager(), "MessageDialogFragment");
        return true;
    }

    public boolean gettingHelp(JSONObject student) {
        try {
            return student.getBoolean("gettingHelp");
        } catch (JSONException e) {
        }
        return false;
    }
}
