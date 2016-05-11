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
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.socket.emitter.Emitter;
import se.kth.csc.stayawhile.api.Queuee;
import se.kth.csc.stayawhile.api.User;
import se.kth.csc.stayawhile.api.Queue;
import se.kth.csc.stayawhile.api.http.GetQueue;
import se.kth.csc.stayawhile.api.websocket.Websocket;
import se.kth.csc.stayawhile.swipe.QueueTouchListener;

public class QueueActivity extends AppCompatActivity implements MessageDialogFragment.MessageListener, QueueAdapter.StudentActionListener, KickStudentDialogFragment.KickStudentDialogListener {

    private RecyclerView mRecyclerView;
    private QueueAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private PowerManager.WakeLock mWakeLock;

    private Websocket mSocket;
    private Queue mQueue;
    private String mQueueName;
    private String mUgid;
    private Queuee curContextMenuObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mQueueName = getIntent().getStringExtra("queue");
        mSocket = new Websocket(mQueueName);

        User user = User.fromJSON(getApplicationContext().getSharedPreferences("userData", Context.MODE_PRIVATE).getString("userData", "{}"));
        this.mUgid = user.getUgKthid();

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
        mRecyclerView.addOnItemTouchListener(new QueueTouchListener(mRecyclerView,
                new QueueTouchListener.QueueSwipeListener() {
                    @Override
                    public void onDismiss(RecyclerView recyclerView, int[] reverseSortedPositions) {
                        // TODO: This is kinda broken since we dont fetch all the user info in advance.
                        //       The info that one is about to remove is displayed in the popup, though.
                        for (int position : reverseSortedPositions) {
                            showKickUserDialog(position);
                        }
                    }

                    @Override
                    public void onSetHelp(RecyclerView recyclerView, int[] reverseSortedPositions) {
                        List<Queuee> users = new ArrayList<>();
                        for (int position : reverseSortedPositions) {
                            users.add(mAdapter.onPosition(position));
                        }
                        for (Queuee user : users) {
                            if (user.getGettingHelp()) {
                                mSocket.sendStopHelp(user);
                            } else {
                                mSocket.sendHelp(user);
                            }
                        }
                    }
                }));

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
                    MainActivity.wearMessageHandler.sendQueueToWear(mQueue);
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
        new GetQueue(mQueueName) {
            @Override
            public void onPostExecute(Queue queue) {
                mQueue = queue;
                QueueActivity.this.onQueueUpdate();
            }
        }.execute();
    }

    private void setStopHelp(Object... args) {
        try {
            JSONObject user = (JSONObject) args[0];
            int pos = mAdapter.positionOf(user.getString("ugKthid"));
            if (pos >= 0) {
                Queuee existing = mAdapter.onPosition(pos);
                mAdapter.removePosition(pos);
                existing.getJSON().put("gettingHelp", false);
                existing.getJSON().remove("helper");
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
                Queuee existing = mAdapter.onPosition(pos);
                mAdapter.removePosition(pos);
                existing.getJSON().put("gettingHelp", true);
                if (user.has("helper")) { // TODO: Why is .has check needed?
                    existing.getJSON().put("helper", user.get("helper"));
                }
                mAdapter.add(existing);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateUser(Object... args) {
        Queuee user = Queuee.fromJSON((JSONObject) args[0]);
        int pos = mAdapter.positionOf(user.getUgKthid());
        if (pos >= 0) {
            mAdapter.set(pos, user);
        }

        List<Queuee> queuees = mQueue.getQueuees();
        for (int i = 0; i < queuees.size(); i++) {
            if (queuees.get(i).getUgKthid().equals(user.getUgKthid())) {
                queuees.set(i, user);
                break;
            }
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onQueueUpdate();
            }
        });
    }

    private void newUser(Object... args) {
        Queuee person = Queuee.fromJSON((JSONObject) args[0]);
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

    private void onQueueUpdate() {
        mAdapter = new QueueAdapter(mQueue.getQueuees(), this, mUgid);
        mRecyclerView.setAdapter(mAdapter);
        supportInvalidateOptionsMenu();


        try {
            MainActivity.wearMessageHandler.sendQueueToWear(mQueue);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void showKickUserDialog(int position) {
        Queuee user = mAdapter.onPosition(position);
        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putString("user", user.getJSON().toString());
        KickStudentDialogFragment q = new KickStudentDialogFragment();
        q.setArguments(args);
        q.show(QueueActivity.this.getFragmentManager(), "KickStudentDialogFragment");
    }

    public void onKickStudentDialogPositiveClick(KickStudentDialogFragment dialog, String ugKthid, int position) {
        Queuee user = mAdapter.onPosition(position);
        if (!ugKthid.equals(user.getUgKthid())) {
            return;
        }
        mAdapter.removePosition(position);
        mSocket.sendKick(user);
        //mAdapter.notifyDataSetChanged();  // Seems this is called anyway in removePosition ?
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
            if (mQueue.isLocked()) {
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
                if (mQueue.isLocked()) { // TODO: No null check here
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
    public void cantFind(User student) {
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

    public MenuInflater getMenuInflater(Queuee curObj) {
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
                showKickUserDialog(mAdapter.positionOf(curContextMenuObj.getUgKthid()));
                System.out.println("kick");
                return true;
            case R.id.actionHelp:
                if (curContextMenuObj.getGettingHelp()) {
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
        args.putString("ugKthid", curContextMenuObj.getUgKthid());
        fragment.setArguments(args);
        fragment.show(getFragmentManager(), "MessageDialogFragment");
        return true;
    }
}
