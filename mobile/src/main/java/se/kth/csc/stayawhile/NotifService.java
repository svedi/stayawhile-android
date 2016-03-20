package se.kth.csc.stayawhile;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class NotifService extends IntentService {

    private Socket mSocket;
    private String mQueueName;
    private String mUgid;

    {
        try {
            mSocket = IO.socket("http://queue.csc.kth.se/");
        } catch (URISyntaxException e) {
        }
    }

    public NotifService() {
        super("QueueNotifyService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mSocket.on("join", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                new APITask(new APICallback() {
                    @Override
                    public void r(String result) {
                        try {
                            int people = 0;
                            JSONArray queue = new JSONObject(result).getJSONArray("queue");
                            for (int i = 0; i < queue.length(); i++) {
                                JSONObject person = queue.getJSONObject(i);
                                if (!person.getBoolean("gettingHelp") || person.getString("helper").equals(mUgid)) people++;
                            }
                            if (people > 1) return;
                            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplication())
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setContentTitle("Stay-A-While")
                                    .setContentText("Someone just joined the queue " + mQueueName)
                                    .setSound(alarmSound);
                            Intent notificationIntent = new Intent(getApplication(), QueueActivity.class);
                            notificationIntent.putExtra("queue", mQueueName);
                            PendingIntent contentIntent = PendingIntent.getActivity(getApplication(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                            builder.setContentIntent(contentIntent);
                            builder.setAutoCancel(true);
                            builder.setLights(Color.BLUE, 500, 500);
                            long[] pattern = {200, 200};
                            builder.setVibrate(pattern);
                            builder.setStyle(new NotificationCompat.InboxStyle());
                            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            manager.notify(1, builder.build());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).execute("method", "queue/" + Uri.encode(mQueueName));
            }
        });
        mSocket.connect();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mQueueName = intent.getData().getQueryParameter("queue");
        mUgid = intent.getData().getQueryParameter("ugid");
        mSocket.emit("listen", mQueueName);
    }
}
